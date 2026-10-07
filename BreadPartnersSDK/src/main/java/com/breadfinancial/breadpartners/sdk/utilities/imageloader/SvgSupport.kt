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
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.utilities.imageloader

import android.content.Context
import android.graphics.drawable.PictureDrawable
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
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

/**
 * Decodes an [InputStream] into an [SVG] instance using AndroidSVG so that
 * Glide can render vector artwork (`.svg`) in addition to raster formats.
 */
internal class SvgDecoder : ResourceDecoder<InputStream, SVG> {

    override fun handles(source: InputStream, options: Options): Boolean = true

    @Throws(IOException::class)
    override fun decode(
        source: InputStream, width: Int, height: Int, options: Options
    ): Resource<SVG> {
        return try {
            val svg = SVG.getFromInputStream(source)
            // SVGs that only declare a viewBox (no intrinsic width/height) would
            // otherwise render at zero size. Constrain the document to the size
            // Glide requested for the target so the artwork is actually drawn.
            if (width != Target.SIZE_ORIGINAL) {
                svg.documentWidth = width.toFloat()
            }
            if (height != Target.SIZE_ORIGINAL) {
                svg.documentHeight = height.toFloat()
            }

            SimpleResource(svg)
        } catch (ex: SVGParseException) {
            throw IOException("Cannot load SVG from stream", ex)
        }
    }
}

/**
 * Converts a decoded [SVG] into a [PictureDrawable] sized to the SVG's own
 * aspect ratio at the target height, so the artwork fills the picture (no
 * internal centering). Must be rendered on a software layer.
 */
internal class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {

    override fun transcode(
        toTranscode: Resource<SVG>, options: Options
    ): Resource<PictureDrawable> {
        val svg = toTranscode.get()

        val height = svg.documentHeight
        val viewBox = svg.documentViewBox

        if (height > 0f && viewBox != null && viewBox.height() > 0f) {
            // Aspect-correct width so the artwork fills the picture at full height.
            val aspect = viewBox.width() / viewBox.height()
            svg.documentWidth = height * aspect
        }

        val picture = svg.renderToPicture()
        return SimpleResource(PictureDrawable(picture))
    }
}

/**
 * Registers SVG decoding support with Glide so that both raster (PNG/JPG/WebP)
 * and vector (SVG) images can be loaded through the same Glide pipeline.
 */
@GlideModule
internal class SvgModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    override fun isManifestParsingEnabled(): Boolean = false
}

