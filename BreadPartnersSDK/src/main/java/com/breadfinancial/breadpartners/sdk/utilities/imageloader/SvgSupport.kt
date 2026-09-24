//------------------------------------------------------------------------------
//  File:          SvgSupport.kt
//  Author(s):     Bread Financial
//  Date:          21 September 2026
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  Bundles the SVG support components used by the Glide image pipeline:
//    - SvgDecoder            : InputStream -> SVG
//    - SvgDrawableTranscoder : SVG -> PictureDrawable
//    - SvgModule             : registers the above two with Glide
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.utilities.imageloader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Picture
import android.graphics.RectF
import android.graphics.drawable.PictureDrawable
import android.util.Xml
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.Target
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

// =============================================================================
// Document model
// =============================================================================

/**
 * A single drawable shape: an [android.graphics.Path] paired with the resolved
 * [SvgStyle] used to paint it.
 */
internal class SvgShape(private val path: Path, private val style: SvgStyle) {

    fun draw(canvas: Canvas, fillPaint: Paint, strokePaint: Paint) {
        if (style.opacity <= 0f) return

        (style.fill as? SvgPaint.Solid)?.let { fill ->
            path.fillType = style.fillType
            fillPaint.color = fill.color
            fillPaint.alpha = (fill.alpha * style.opacity * 255f).roundToInt().coerceIn(0, 255)
            canvas.drawPath(path, fillPaint)
        }

        (style.stroke as? SvgPaint.Solid)?.let { stroke ->
            if (style.strokeWidth > 0f) {
                strokePaint.color = stroke.color
                strokePaint.alpha =
                    (stroke.alpha * style.opacity * 255f).roundToInt().coerceIn(0, 255)
                strokePaint.strokeWidth = style.strokeWidth
                canvas.drawPath(path, strokePaint)
            }
        }
    }
}

/** A paint source: either "none" or a solid color with its own alpha. */
internal sealed class SvgPaint {
    object None : SvgPaint()
    data class Solid(val color: Int, val alpha: Float) : SvgPaint()
}

/** Resolved presentation attributes for an element (with CSS-cascade merging). */
internal data class SvgStyle(
    val fill: SvgPaint = SvgPaint.Solid(Color.BLACK, 1f),
    val stroke: SvgPaint = SvgPaint.None,
    val strokeWidth: Float = 1f,
    val opacity: Float = 1f,
    val fillType: Path.FillType = Path.FillType.WINDING
) {
    /** Merges attributes found on a child element on top of inherited style. */
    fun merging(attributes: Map<String, String>): SvgStyle {
        var fill = this.fill
        var stroke = this.stroke
        var strokeWidth = this.strokeWidth
        var opacity = this.opacity
        var fillType = this.fillType

        attributes["fill"]?.let { fill = parsePaint(it, fill) }
        attributes["stroke"]?.let { stroke = parsePaint(it, stroke) }
        attributes["stroke-width"]?.let { value ->
            SvgLengthParser.length(value)?.let { strokeWidth = it }
        }
        attributes["opacity"]?.let { value ->
            value.toFloatOrNull()?.let { opacity *= it.coerceIn(0f, 1f) }
        }
        attributes["fill-opacity"]?.let { value ->
            value.toFloatOrNull()?.let { fo ->
                (fill as? SvgPaint.Solid)?.let { fill = it.copy(alpha = fo.coerceIn(0f, 1f)) }
            }
        }
        attributes["fill-rule"]?.let {
            fillType = if (it == "evenodd") Path.FillType.EVEN_ODD else Path.FillType.WINDING
        }

        // Bare "style" attribute, e.g. style="fill:#fff;stroke:none"
        attributes["style"]?.let { inline ->
            for (declaration in inline.split(";")) {
                val parts = declaration.split(":", limit = 2)
                if (parts.size != 2) continue
                val key = parts[0].trim()
                val value = parts[1].trim()
                when (key) {
                    "fill" -> fill = parsePaint(value, fill)
                    "stroke" -> stroke = parsePaint(value, stroke)
                    "opacity" -> value.toFloatOrNull()?.let { opacity *= it.coerceIn(0f, 1f) }
                }
            }
        }

        return SvgStyle(fill, stroke, strokeWidth, opacity, fillType)
    }

    private fun parsePaint(value: String, fallback: SvgPaint): SvgPaint {
        val trimmed = value.trim()
        if (trimmed == "none") return SvgPaint.None
        if (trimmed == "currentColor" || trimmed.isEmpty()) return fallback
        val color = SvgColorParser.color(trimmed) ?: return fallback
        return SvgPaint.Solid(color, 1f)
    }
}

/** Parsed SVG document: drawable shapes plus intrinsic sizing information. */
internal class SvgDocument {
    val shapes: MutableList<SvgShape> = mutableListOf()
    var viewBoxOriginX: Float = 0f
    var viewBoxOriginY: Float = 0f
    var viewBoxWidth: Float = 0f
    var viewBoxHeight: Float = 0f
    var widthAttr: Float = 0f
    var heightAttr: Float = 0f

    /** Target size requested by Glide (0 when SIZE_ORIGINAL). */
    var targetWidth: Float = 0f
    var targetHeight: Float = 0f

    val intrinsicWidth: Float
        get() = when {
            viewBoxWidth > 0f -> viewBoxWidth
            widthAttr > 0f -> widthAttr
            else -> DEFAULT_SIZE
        }

    val intrinsicHeight: Float
        get() = when {
            viewBoxHeight > 0f -> viewBoxHeight
            heightAttr > 0f -> heightAttr
            else -> DEFAULT_SIZE
        }

    private companion object {
        const val DEFAULT_SIZE = 200f
    }
}

// =============================================================================
// Glide decoder: InputStream -> SvgDocument
// =============================================================================

/**
 * Decodes an [InputStream] into an [SvgDocument] by parsing the SVG XML with a
 * pull parser. Records the target size Glide requested so the transcoder can
 * rasterize the artwork at the correct scale.
 */
internal class SvgDecoder : ResourceDecoder<InputStream, SvgDocument> {

    override fun handles(source: InputStream, options: Options): Boolean = true

    @Throws(IOException::class)
    override fun decode(
        source: InputStream, width: Int, height: Int, options: Options
    ): Resource<SvgDocument> {
        val bytes = source.readBytes()

        // Parse simple class-selector CSS from any <style> blocks first, so that
        // Illustrator-style `class="st0"` fills resolve correctly.
        val styleSheet = SvgStyleSheetParser.parse(bytes)

        val document = SvgDocumentParser(styleSheet).parse(bytes)
            ?: throw IOException("Cannot parse SVG from stream")

        if (document.shapes.isEmpty()) {
            throw IOException("SVG contained no drawable shapes")
        }

        if (width != Target.SIZE_ORIGINAL) document.targetWidth = width.toFloat()
        if (height != Target.SIZE_ORIGINAL) document.targetHeight = height.toFloat()

        return SimpleResource(document)
    }
}

// =============================================================================
// Glide transcoder: SvgDocument -> PictureDrawable
// =============================================================================

/**
 * Rasterizes an [SvgDocument] into a [PictureDrawable]. The picture is sized to
 * the SVG's OWN aspect ratio at the requested target height, so the artwork
 * FILLS the picture with no internal centering. Combined with the left-aligned
 * matrix applied by [ImageLoaderUtil], the logo fills the full height with its
 * width scaled automatically to preserve aspect ratio.
 *
 * The resulting [PictureDrawable] must be rendered on a software layer.
 */
internal class SvgDrawableTranscoder : ResourceTranscoder<SvgDocument, PictureDrawable> {

    override fun transcode(
        toTranscode: Resource<SvgDocument>, options: Options
    ): Resource<PictureDrawable> {
        val document = toTranscode.get()

        val sourceWidth = document.intrinsicWidth
        val sourceHeight = document.intrinsicHeight

        val targetHeight = if (document.targetHeight > 0f) document.targetHeight else sourceHeight
        val aspect = if (sourceHeight > 0f) sourceWidth / sourceHeight else 1f
        val outputHeight = targetHeight
        val outputWidth = targetHeight * aspect

        val scaleX = if (sourceWidth > 0f) outputWidth / sourceWidth else 1f
        val scaleY = if (sourceHeight > 0f) outputHeight / sourceHeight else 1f

        val picture = Picture()
        val canvas = picture.beginRecording(
            outputWidth.roundToInt().coerceAtLeast(1),
            outputHeight.roundToInt().coerceAtLeast(1)
        )
        canvas.scale(scaleX, scaleY)
        canvas.translate(-document.viewBoxOriginX, -document.viewBoxOriginY)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.BUTT
            strokeJoin = Paint.Join.MITER
        }

        for (shape in document.shapes) {
            shape.draw(canvas, fillPaint, strokePaint)
        }

        picture.endRecording()
        return SimpleResource(PictureDrawable(picture))
    }
}

// =============================================================================
// XML parsing
// =============================================================================

/**
 * Walks the SVG XML with an [XmlPullParser], maintaining inherited style,
 * transform, and "suppressed" (non-rendering) state via parallel stacks.
 */
internal class SvgDocumentParser(private val styleSheet: Map<String, Map<String, String>>) {

    /**
     * Elements whose content is a *definition* (referenced elsewhere via
     * "url(#id)" or <use>, neither of which this minimal parser supports)
     * rather than something drawn in place. Without this, shapes declared only
     * for reuse would incorrectly render as visible solid squares/paths.
     */
    private val nonRenderingElements = setOf(
        "defs", "clippath", "symbol", "mask", "pattern",
        "lineargradient", "radialgradient", "style", "title", "desc"
    )

    fun parse(bytes: ByteArray): SvgDocument? {
        val document = SvgDocument()
        val styleStack = ArrayDeque<SvgStyle>().apply { addLast(SvgStyle()) }
        val transformStack = ArrayDeque<Matrix>().apply { addLast(Matrix()) }
        val suppressStack = ArrayDeque<Boolean>().apply { addLast(false) }

        return try {
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(bytes.inputStream(), null)
            }

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val elementName = parser.name
                        val attributes = resolvedAttributes(readAttributes(parser))
                        val currentStyle = styleStack.last()
                        val mergedStyle = currentStyle.merging(attributes)

                        val localTransform = SvgTransformParser.transform(attributes["transform"])
                        val combined = Matrix(transformStack.last())
                        combined.preConcat(localTransform)

                        val parentSuppressed = suppressStack.last()
                        val suppressed = parentSuppressed ||
                                nonRenderingElements.contains(elementName.lowercase())

                        if (elementName == "svg") {
                            parseSvgRoot(attributes, document)
                        }

                        if (!suppressed) {
                            SvgShapeFactory.path(elementName, attributes)?.let { path ->
                                val transformed = Path()
                                path.transform(combined, transformed)
                                document.shapes.add(SvgShape(transformed, mergedStyle))
                            }
                        }

                        styleStack.addLast(mergedStyle)
                        transformStack.addLast(combined)
                        suppressStack.addLast(suppressed)
                    }

                    XmlPullParser.END_TAG -> {
                        if (styleStack.size > 1) styleStack.removeLast()
                        if (transformStack.size > 1) transformStack.removeLast()
                        if (suppressStack.size > 1) suppressStack.removeLast()
                    }
                }
                event = parser.next()
            }
            document
        } catch (_: Exception) {
            null
        }
    }

    private fun readAttributes(parser: XmlPullParser): Map<String, String> {
        val map = HashMap<String, String>(parser.attributeCount)
        for (i in 0 until parser.attributeCount) {
            map[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        }
        return map
    }

    /**
     * Merges CSS-class-derived properties (lower priority) underneath this
     * element's own direct attributes (higher priority), matching basic CSS
     * cascade order: inline attributes/style win over classes.
     */
    private fun resolvedAttributes(attributes: Map<String, String>): Map<String, String> {
        val classAttribute = attributes["class"]
        if (classAttribute.isNullOrEmpty() || styleSheet.isEmpty()) return attributes

        val classDerived = HashMap<String, String>()
        for (className in classAttribute.split(" ")) {
            styleSheet[className]?.let { classDerived.putAll(it) }
        }
        if (classDerived.isEmpty()) return attributes

        // Element's own attributes override class-derived ones.
        classDerived.putAll(attributes)
        return classDerived
    }

    private fun parseSvgRoot(attributes: Map<String, String>, document: SvgDocument) {
        attributes["viewBox"]?.let { value ->
            val parts = value.split(Regex("[\\s,]+")).mapNotNull { it.toFloatOrNull() }
            if (parts.size == 4) {
                document.viewBoxOriginX = parts[0]
                document.viewBoxOriginY = parts[1]
                document.viewBoxWidth = parts[2]
                document.viewBoxHeight = parts[3]
            }
        }
        val width = SvgLengthParser.length(attributes["width"])
        val height = SvgLengthParser.length(attributes["height"])
        if (width != null && height != null) {
            document.widthAttr = width
            document.heightAttr = height
        }
    }
}

// =============================================================================
// Shape construction
// =============================================================================

internal object SvgShapeFactory {

    fun path(elementName: String, attributes: Map<String, String>): Path? {
        return when (elementName) {
            "path" -> attributes["d"]?.let { SvgPathDataParser.path(it) }

            "rect" -> {
                val x = SvgLengthParser.length(attributes["x"]) ?: 0f
                val y = SvgLengthParser.length(attributes["y"]) ?: 0f
                val width = SvgLengthParser.length(attributes["width"]) ?: return null
                val height = SvgLengthParser.length(attributes["height"]) ?: return null
                val rx = SvgLengthParser.length(attributes["rx"])
                val ry = SvgLengthParser.length(attributes["ry"])
                val corner = rx ?: ry ?: 0f
                val rect = RectF(x, y, x + width, y + height)
                Path().apply {
                    if (corner > 0f) addRoundRect(rect, corner, corner, Path.Direction.CW)
                    else addRect(rect, Path.Direction.CW)
                }
            }

            "circle" -> {
                val cx = SvgLengthParser.length(attributes["cx"]) ?: return null
                val cy = SvgLengthParser.length(attributes["cy"]) ?: return null
                val r = SvgLengthParser.length(attributes["r"]) ?: return null
                Path().apply {
                    addOval(RectF(cx - r, cy - r, cx + r, cy + r), Path.Direction.CW)
                }
            }

            "ellipse" -> {
                val cx = SvgLengthParser.length(attributes["cx"]) ?: return null
                val cy = SvgLengthParser.length(attributes["cy"]) ?: return null
                val rx = SvgLengthParser.length(attributes["rx"]) ?: return null
                val ry = SvgLengthParser.length(attributes["ry"]) ?: return null
                Path().apply {
                    addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
                }
            }

            "line" -> {
                val x1 = SvgLengthParser.length(attributes["x1"]) ?: return null
                val y1 = SvgLengthParser.length(attributes["y1"]) ?: return null
                val x2 = SvgLengthParser.length(attributes["x2"]) ?: return null
                val y2 = SvgLengthParser.length(attributes["y2"]) ?: return null
                Path().apply {
                    moveTo(x1, y1)
                    lineTo(x2, y2)
                }
            }

            "polygon", "polyline" -> {
                val points = attributes["points"] ?: return null
                val coords = points.split(Regex("[\\s,]+")).mapNotNull { it.toFloatOrNull() }
                if (coords.size < 4) return null
                Path().apply {
                    moveTo(coords[0], coords[1])
                    var i = 2
                    while (i + 1 < coords.size) {
                        lineTo(coords[i], coords[i + 1])
                        i += 2
                    }
                    if (elementName == "polygon") close()
                }
            }

            else -> null
        }
    }
}

// =============================================================================
// Path data ("d" attribute) tokenizer
// =============================================================================

internal object SvgPathDataParser {

    fun path(data: String): Path? {
        val scanner = PathDataScanner(data)
        val path = Path()
        var currentX = 0f
        var currentY = 0f
        var startX = 0f
        var startY = 0f
        var lastControlX: Float? = null
        var lastControlY: Float? = null
        var lastCommand: Char? = null
        var hasContent = false

        while (true) {
            val command = scanner.nextCommand(lastCommand) ?: break
            val isRelative = command.isLowerCase()
            when (command.lowercaseChar()) {
                'm' -> {
                    val p = scanner.nextPoint() ?: break
                    currentX = if (isRelative) currentX + p[0] else p[0]
                    currentY = if (isRelative) currentY + p[1] else p[1]
                    path.moveTo(currentX, currentY)
                    startX = currentX
                    startY = currentY
                    lastControlX = null; lastControlY = null
                    hasContent = true
                    // Subsequent coordinate pairs after 'M' are implicit 'L'.
                    while (scanner.peekPoint() != null) {
                        val next = scanner.nextPoint()!!
                        currentX = if (isRelative) currentX + next[0] else next[0]
                        currentY = if (isRelative) currentY + next[1] else next[1]
                        path.lineTo(currentX, currentY)
                    }
                }
                'l' -> {
                    var p = scanner.nextPoint()
                    while (p != null) {
                        currentX = if (isRelative) currentX + p[0] else p[0]
                        currentY = if (isRelative) currentY + p[1] else p[1]
                        path.lineTo(currentX, currentY)
                        hasContent = true
                        p = scanner.nextPoint()
                    }
                    lastControlX = null; lastControlY = null
                }
                'h' -> {
                    var v = scanner.nextNumber()
                    while (v != null) {
                        currentX = if (isRelative) currentX + v else v
                        path.lineTo(currentX, currentY)
                        hasContent = true
                        v = scanner.nextNumber()
                    }
                    lastControlX = null; lastControlY = null
                }
                'v' -> {
                    var v = scanner.nextNumber()
                    while (v != null) {
                        currentY = if (isRelative) currentY + v else v
                        path.lineTo(currentX, currentY)
                        hasContent = true
                        v = scanner.nextNumber()
                    }
                    lastControlX = null; lastControlY = null
                }
                'c' -> {
                    while (true) {
                        val c1 = scanner.nextPoint() ?: break
                        val c2 = scanner.nextPoint() ?: break
                        val end = scanner.nextPoint() ?: break
                        val c1x = if (isRelative) currentX + c1[0] else c1[0]
                        val c1y = if (isRelative) currentY + c1[1] else c1[1]
                        val c2x = if (isRelative) currentX + c2[0] else c2[0]
                        val c2y = if (isRelative) currentY + c2[1] else c2[1]
                        val ex = if (isRelative) currentX + end[0] else end[0]
                        val ey = if (isRelative) currentY + end[1] else end[1]
                        path.cubicTo(c1x, c1y, c2x, c2y, ex, ey)
                        lastControlX = c2x; lastControlY = c2y
                        currentX = ex; currentY = ey
                        hasContent = true
                    }
                }
                's' -> {
                    while (true) {
                        val c2 = scanner.nextPoint() ?: break
                        val end = scanner.nextPoint() ?: break
                        val c1x = if (lastControlX != null) 2 * currentX - lastControlX else currentX
                        val c1y = if (lastControlY != null) 2 * currentY - lastControlY else currentY
                        val c2x = if (isRelative) currentX + c2[0] else c2[0]
                        val c2y = if (isRelative) currentY + c2[1] else c2[1]
                        val ex = if (isRelative) currentX + end[0] else end[0]
                        val ey = if (isRelative) currentY + end[1] else end[1]
                        path.cubicTo(c1x, c1y, c2x, c2y, ex, ey)
                        lastControlX = c2x; lastControlY = c2y
                        currentX = ex; currentY = ey
                        hasContent = true
                    }
                }
                'q' -> {
                    while (true) {
                        val c = scanner.nextPoint() ?: break
                        val end = scanner.nextPoint() ?: break
                        val cx = if (isRelative) currentX + c[0] else c[0]
                        val cy = if (isRelative) currentY + c[1] else c[1]
                        val ex = if (isRelative) currentX + end[0] else end[0]
                        val ey = if (isRelative) currentY + end[1] else end[1]
                        path.quadTo(cx, cy, ex, ey)
                        lastControlX = cx; lastControlY = cy
                        currentX = ex; currentY = ey
                        hasContent = true
                    }
                }
                't' -> {
                    while (true) {
                        val end = scanner.nextPoint() ?: break
                        val cx = if (lastControlX != null) 2 * currentX - lastControlX else currentX
                        val cy = if (lastControlY != null) 2 * currentY - lastControlY else currentY
                        val ex = if (isRelative) currentX + end[0] else end[0]
                        val ey = if (isRelative) currentY + end[1] else end[1]
                        path.quadTo(cx, cy, ex, ey)
                        lastControlX = cx; lastControlY = cy
                        currentX = ex; currentY = ey
                        hasContent = true
                    }
                }
                'a' -> {
                    while (true) {
                        val radii = scanner.nextPoint() ?: break
                        val rotation = scanner.nextNumber() ?: break
                        val largeArc = scanner.nextFlag() ?: break
                        val sweep = scanner.nextFlag() ?: break
                        val end = scanner.nextPoint() ?: break
                        val ex = if (isRelative) currentX + end[0] else end[0]
                        val ey = if (isRelative) currentY + end[1] else end[1]
                        SvgArcConverter.addArc(
                            path, currentX, currentY, ex, ey,
                            radii[0].toDouble(), radii[1].toDouble(),
                            rotation.toDouble(), largeArc, sweep
                        )
                        currentX = ex; currentY = ey
                        lastControlX = null; lastControlY = null
                        hasContent = true
                    }
                }
                'z' -> {
                    path.close()
                    currentX = startX; currentY = startY
                    lastControlX = null; lastControlY = null
                }
                else -> break
            }
            lastCommand = command
        }

        return if (hasContent) path else null
    }
}

/** Scans the SVG path "d" grammar: commands, numbers, flags, and points. */
internal class PathDataScanner(string: String) {
    private val chars = string.toCharArray()
    private var index = 0

    fun nextCommand(previous: Char?): Char? {
        skipSeparators()
        if (index >= chars.size) return null
        val c = chars[index]
        if (c.isLetter()) {
            index++
            return c
        }
        // Implicit repetition of the previous command (e.g. "L10 10 20 20").
        return previous
    }

    fun nextNumber(): Float? {
        skipSeparators()
        if (index >= chars.size) return null
        var end = index
        var seenDot = false
        var seenExponent = false
        if (chars[end] == '+' || chars[end] == '-') end++
        val start = end
        while (end < chars.size) {
            val c = chars[end]
            when {
                c.isDigit() -> end++
                c == '.' && !seenDot -> { seenDot = true; end++ }
                (c == 'e' || c == 'E') && !seenExponent -> {
                    seenExponent = true; end++
                    if (end < chars.size && (chars[end] == '+' || chars[end] == '-')) end++
                }
                else -> break
            }
        }
        if (end <= start) return null
        val substring = String(chars, index, end - index)
        index = end
        return substring.toFloatOrNull()
    }

    fun nextFlag(): Boolean? {
        skipSeparators()
        if (index >= chars.size) return null
        val c = chars[index]
        if (c != '0' && c != '1') return null
        index++
        return c == '1'
    }

    /** Returns [x, y] or null (restoring position on partial read). */
    fun nextPoint(): FloatArray? {
        val saved = index
        val x = nextNumber()
        val y = nextNumber()
        if (x == null || y == null) {
            index = saved
            return null
        }
        return floatArrayOf(x, y)
    }

    fun peekPoint(): FloatArray? {
        val saved = index
        val result = nextPoint()
        index = saved
        return result
    }

    private fun skipSeparators() {
        while (index < chars.size &&
            (chars[index] == ' ' || chars[index] == ',' ||
                    chars[index] == '\n' || chars[index] == '\t' || chars[index] == '\r')
        ) index++
    }
}

// =============================================================================
// Elliptical arc ("A") to bezier conversion (SVG 1.1 spec, F.6)
// =============================================================================

internal object SvgArcConverter {

    fun addArc(
        path: Path,
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        radiusX: Double, radiusY: Double,
        xAxisRotationDegrees: Double,
        largeArcFlag: Boolean, sweepFlag: Boolean
    ) {
        var rx = abs(radiusX)
        var ry = abs(radiusY)
        if (rx == 0.0 || ry == 0.0 || (startX == endX && startY == endY)) {
            path.lineTo(endX, endY)
            return
        }

        val phi = xAxisRotationDegrees * Math.PI / 180.0
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)

        val dx2 = (startX - endX) / 2.0
        val dy2 = (startY - endY) / 2.0
        val x1p = cosPhi * dx2 + sinPhi * dy2
        val y1p = -sinPhi * dx2 + cosPhi * dy2

        val lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
        if (lambda > 1.0) {
            val scale = sqrt(lambda)
            rx *= scale
            ry *= scale
        }

        val sign = if (largeArcFlag != sweepFlag) 1.0 else -1.0
        val num = maxOf(
            0.0,
            (rx * rx * ry * ry) - (rx * rx * y1p * y1p) - (ry * ry * x1p * x1p)
        )
        val den = (rx * rx * y1p * y1p) + (ry * ry * x1p * x1p)
        val coefficient = if (den == 0.0) 0.0 else sign * sqrt(num / den)

        val cxp = coefficient * (rx * y1p / ry)
        val cyp = coefficient * -(ry * x1p / rx)

        val cx = cosPhi * cxp - sinPhi * cyp + (startX + endX) / 2.0
        val cy = sinPhi * cxp + cosPhi * cyp + (startY + endY) / 2.0

        fun angle(ux: Double, uy: Double, vx: Double, vy: Double): Double {
            val dot = ux * vx + uy * vy
            val len = sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
            var result = if (len == 0.0) 0.0 else acos((dot / len).coerceIn(-1.0, 1.0))
            if ((ux * vy - uy * vx) < 0) result = -result
            return result
        }

        val theta1 = angle(1.0, 0.0, (x1p - cxp) / rx, (y1p - cyp) / ry)
        var deltaTheta = angle(
            (x1p - cxp) / rx, (y1p - cyp) / ry,
            (-x1p - cxp) / rx, (-y1p - cyp) / ry
        )
        if (!sweepFlag && deltaTheta > 0) deltaTheta -= 2 * Math.PI
        if (sweepFlag && deltaTheta < 0) deltaTheta += 2 * Math.PI

        // Split into segments of at most 90 degrees for a good approximation.
        val segmentCount = maxOf(1, ceil(abs(deltaTheta) / (Math.PI / 2)).toInt())
        val segmentDelta = deltaTheta / segmentCount
        val alpha = sin(segmentDelta) *
                (sqrt(4 + 3 * tan(segmentDelta / 2).pow(2)) - 1) / 3

        var theta = theta1
        var currentX = startX.toDouble()
        var currentY = startY.toDouble()
        repeat(segmentCount) {
            val nextTheta = theta + segmentDelta
            val cosTheta = cos(theta); val sinTheta = sin(theta)
            val cosNext = cos(nextTheta); val sinNext = sin(nextTheta)

            fun ellipsePointX(cosT: Double, sinT: Double) = cosPhi * rx * cosT - sinPhi * ry * sinT + cx
            fun ellipsePointY(cosT: Double, sinT: Double) = sinPhi * rx * cosT + cosPhi * ry * sinT + cy
            fun tangentX(cosT: Double, sinT: Double) = -cosPhi * rx * sinT - sinPhi * ry * cosT
            fun tangentY(cosT: Double, sinT: Double) = -sinPhi * rx * sinT + cosPhi * ry * cosT

            val p2x = ellipsePointX(cosNext, sinNext)
            val p2y = ellipsePointY(cosNext, sinNext)
            val t1x = tangentX(cosTheta, sinTheta)
            val t1y = tangentY(cosTheta, sinTheta)
            val t2x = tangentX(cosNext, sinNext)
            val t2y = tangentY(cosNext, sinNext)

            val control1X = currentX + alpha * t1x
            val control1Y = currentY + alpha * t1y
            val control2X = p2x - alpha * t2x
            val control2Y = p2y - alpha * t2y

            path.cubicTo(
                control1X.toFloat(), control1Y.toFloat(),
                control2X.toFloat(), control2Y.toFloat(),
                p2x.toFloat(), p2y.toFloat()
            )
            currentX = p2x; currentY = p2y
            theta = nextTheta
        }
    }
}

// =============================================================================
// Transform parsing ("transform" attribute)
// =============================================================================

internal object SvgTransformParser {

    fun transform(value: String?): Matrix {
        val result = Matrix()
        if (value == null) return result
        val scanner = TransformScanner(value)
        while (true) {
            val function = scanner.nextFunction() ?: break
            val (name, args) = function
            val m = Matrix()
            when (name) {
                "translate" -> m.setTranslate(
                    args.getOrElse(0) { 0f },
                    args.getOrElse(1) { 0f }
                )
                "scale" -> {
                    val sx = args.getOrElse(0) { 1f }
                    val sy = args.getOrElse(1) { sx }
                    m.setScale(sx, sy)
                }
                "rotate" -> {
                    val degrees = args.getOrElse(0) { 0f }
                    if (args.size >= 3) m.setRotate(degrees, args[1], args[2])
                    else m.setRotate(degrees)
                }
                "skewX" -> {
                    val radians = args.getOrElse(0) { 0f } * Math.PI.toFloat() / 180f
                    m.setValues(floatArrayOf(1f, tan(radians), 0f, 0f, 1f, 0f, 0f, 0f, 1f))
                }
                "skewY" -> {
                    val radians = args.getOrElse(0) { 0f } * Math.PI.toFloat() / 180f
                    m.setValues(floatArrayOf(1f, 0f, 0f, tan(radians), 1f, 0f, 0f, 0f, 1f))
                }
                "matrix" -> {
                    if (args.size != 6) continue
                    // SVG matrix(a b c d e f) maps to Android [a c e / b d f / 0 0 1].
                    m.setValues(
                        floatArrayOf(
                            args[0], args[2], args[4],
                            args[1], args[3], args[5],
                            0f, 0f, 1f
                        )
                    )
                }
                else -> continue
            }
            // Compose left-to-right: total = total * m.
            result.preConcat(m)
        }
        return result
    }
}

/** Scans a transform list into (functionName, args) tuples. */
internal class TransformScanner(string: String) {
    private val chars = string.toCharArray()
    private var index = 0

    fun nextFunction(): Pair<String, List<Float>>? {
        skip { it == ' ' || it == ',' }
        if (index >= chars.size) return null
        val name = StringBuilder()
        while (index < chars.size && chars[index].isLetter()) {
            name.append(chars[index]); index++
        }
        if (name.isEmpty()) return null
        skip { it == ' ' }
        if (index >= chars.size || chars[index] != '(') return null
        index++
        val argsString = StringBuilder()
        while (index < chars.size && chars[index] != ')') {
            argsString.append(chars[index]); index++
        }
        if (index < chars.size) index++ // skip ')'
        val args = argsString.toString()
            .split(Regex("[\\s,]+"))
            .mapNotNull { it.toFloatOrNull() }
        return name.toString() to args
    }

    private inline fun skip(condition: (Char) -> Boolean) {
        while (index < chars.size && condition(chars[index])) index++
    }
}

// =============================================================================
// Length / color parsing helpers
// =============================================================================

internal object SvgLengthParser {
    /**
     * Parses a length value, stripping common unit suffixes. Percentage values
     * are unsupported and return null.
     */
    fun length(value: String?): Float? {
        var trimmed = value?.trim() ?: return null
        if (trimmed.isEmpty()) return null
        if (trimmed.endsWith("%")) return null
        for (unit in listOf("px", "pt", "pc", "mm", "cm", "in")) {
            if (trimmed.endsWith(unit)) {
                trimmed = trimmed.dropLast(unit.length)
                break
            }
        }
        return trimmed.toFloatOrNull()
    }
}

internal object SvgColorParser {

    fun color(value: String): Int? {
        val trimmed = value.trim().lowercase()
        return when {
            trimmed.startsWith("#") -> hexColor(trimmed)
            trimmed.startsWith("rgb") -> functionalColor(trimmed)
            else -> namedColors[trimmed]
        }
    }

    private fun hexColor(value: String): Int? {
        var hex = value.drop(1)
        when (hex.length) {
            3 -> hex = hex.map { "$it$it" }.joinToString("")
            6, 8 -> {}
            else -> return null
        }
        val intValue = hex.toLongOrNull(16) ?: return null
        val hasAlpha = hex.length == 8
        val red = ((intValue shr (if (hasAlpha) 24 else 16)) and 0xFF).toInt()
        val green = ((intValue shr (if (hasAlpha) 16 else 8)) and 0xFF).toInt()
        val blue = ((intValue shr (if (hasAlpha) 8 else 0)) and 0xFF).toInt()
        val alpha = if (hasAlpha) (intValue and 0xFF).toInt() else 255
        return Color.argb(alpha, red, green, blue)
    }

    private fun functionalColor(value: String): Int? {
        val open = value.indexOf('(')
        val close = value.indexOf(')')
        if (open < 0 || close < 0 || close < open) return null
        val components = value.substring(open + 1, close)
            .split(",")
            .map { it.trim() }
        if (components.size < 3) return null

        fun component(text: String): Int {
            return if (text.endsWith("%")) {
                val percent = text.dropLast(1).toFloatOrNull() ?: 0f
                (percent / 100f * 255f).roundToInt().coerceIn(0, 255)
            } else {
                (text.toFloatOrNull() ?: 0f).roundToInt().coerceIn(0, 255)
            }
        }

        val red = component(components[0])
        val green = component(components[1])
        val blue = component(components[2])
        val alpha = if (components.size > 3) {
            ((components[3].toFloatOrNull() ?: 1f) * 255f).roundToInt().coerceIn(0, 255)
        } else 255
        return Color.argb(alpha, red, green, blue)
    }

    /** A small set of CSS named colors commonly seen in brand logos. */
    private val namedColors: Map<String, Int> = mapOf(
        "black" to Color.BLACK,
        "white" to Color.WHITE,
        "red" to Color.RED,
        "green" to Color.rgb(0, 128, 0),
        "blue" to Color.BLUE,
        "yellow" to Color.YELLOW,
        "orange" to Color.rgb(255, 165, 0),
        "purple" to Color.rgb(128, 0, 128),
        "gray" to Color.GRAY,
        "grey" to Color.GRAY,
        "transparent" to Color.TRANSPARENT
    )
}

// =============================================================================
// Minimal CSS "<style>" block parsing (class selectors only)
// =============================================================================

/**
 * Parses simple class-selector CSS rules out of any <style> blocks, e.g. Adobe
 * Illustrator's common export pattern:
 * ```
 * <style type="text/css">.st0{fill:#CF202F;}</style>
 * ...
 * <path class="st0" d="..."/>
 * ```
 * Only flat class selectors are supported — no combinators, media queries,
 * pseudo-classes, or specificity rules. This covers the vast majority of vector
 * logo exports, which use <style> purely to avoid repeating fill="#..." on
 * every path.
 */
internal object SvgStyleSheetParser {

    fun parse(bytes: ByteArray): Map<String, Map<String, String>> {
        val text = String(bytes, Charsets.UTF_8)
        val result = HashMap<String, MutableMap<String, String>>()

        var searchStart = 0
        while (true) {
            val styleTag = text.indexOf("<style", searchStart, ignoreCase = true)
            if (styleTag < 0) break
            val tagClose = text.indexOf('>', styleTag)
            if (tagClose < 0) break
            val styleEnd = text.indexOf("</style>", tagClose, ignoreCase = true)
            if (styleEnd < 0) break
            val css = text.substring(tagClose + 1, styleEnd)
            parseRules(css, result)
            searchStart = styleEnd + "</style>".length
        }
        return result
    }

    private fun parseRules(css: String, result: HashMap<String, MutableMap<String, String>>) {
        for (block in css.split("}")) {
            val braceIndex = block.indexOf('{')
            if (braceIndex < 0) continue
            val selectorsText = block.substring(0, braceIndex)
            val declarationsText = block.substring(braceIndex + 1)

            val declarations = HashMap<String, String>()
            for (declaration in declarationsText.split(";")) {
                val parts = declaration.split(":", limit = 2)
                if (parts.size != 2) continue
                declarations[parts[0].trim()] = parts[1].trim()
            }
            if (declarations.isEmpty()) continue

            for (rawSelector in selectorsText.split(",")) {
                val selector = rawSelector.trim()
                if (!selector.startsWith(".")) continue
                val className = selector.drop(1)
                result.getOrPut(className) { HashMap() }.putAll(declarations)
            }
        }
    }
}

// =============================================================================
// Glide module registration
// =============================================================================

/**
 * Registers the self-contained SVG decoding pipeline with Glide so both raster
 * (PNG/JPG/WebP) and vector (SVG) images load through the same Glide pipeline —
 * without any third-party SVG dependency.
 */
@GlideModule
internal class SvgModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry
            .register(SvgDocument::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SvgDocument::class.java, SvgDecoder())
    }

    override fun isManifestParsingEnabled(): Boolean = false
}

