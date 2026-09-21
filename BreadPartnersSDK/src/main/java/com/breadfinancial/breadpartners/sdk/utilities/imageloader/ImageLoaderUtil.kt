//------------------------------------------------------------------------------
//  File:          ImageLoaderUtil.kt
//  Author(s):     Bread Financial
//  Date:          18 September 2026
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.utilities.imageloader

import android.graphics.Matrix
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget

/**
 * Centralized helper for loading brand imagery into an [ImageView].
 *
 * Supports both raster images (PNG, JPG, WebP, GIF) and vector [SVG] images.
 * The image type is inferred from the URL's file extension: URLs ending in
 * `.svg` are decoded through the AndroidSVG-backed Glide pipeline (registered
 * by [SvgModule]) and rendered as a [PictureDrawable], while all other URLs use
 * Glide's default decoders.
 */
internal object ImageLoaderUtil {

    /**
     * Loads [url] into [imageView]. No-op when [url] is null or blank.
     *
     * @param fragment  Fragment used to scope the Glide request lifecycle.
     * @param url       Remote image URL (PNG/JPG/WebP/GIF or SVG).
     * @param imageView Target view to display the image.
     */
    fun loadImage(fragment: Fragment, url: String?, imageView: ImageView) {
        if (url.isNullOrBlank()) return

        if (isSvgUrl(url)) {
            // PictureDrawable can only be rendered with a software layer.
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            // Left-aligned horizontally + vertically centered requires a custom
            // matrix (no single ImageView scaleType does both), applied once the
            // drawable is set.
            Glide.with(fragment)
                .`as`(PictureDrawable::class.java)
                .load(url)
                .into(object : ImageViewTarget<PictureDrawable>(imageView) {
                    override fun setResource(resource: PictureDrawable?) {
                        view.setImageDrawable(resource)
                        view.applyLeftAlignedVerticalCenter()
                    }
                })
        } else {
            imageView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(fragment)
                .load(url)
                .into(imageView)
        }
    }

    /**
     * Positions the current drawable so it is aspect-preserved, flush against the
     * LEFT edge, and CENTERED VERTICALLY within the ImageView. Uses
     * [ImageView.ScaleType.MATRIX] because no built-in scale type combines
     * left-horizontal with center-vertical alignment.
     *
     * If the view has not been measured yet, re-applies after the next layout.
     */
    private fun ImageView.applyLeftAlignedVerticalCenter() {
        val drawable = drawable ?: return
        val viewWidth = (width - paddingLeft - paddingRight).toFloat()
        val viewHeight = (height - paddingTop - paddingBottom).toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        if (viewWidth <= 0f || viewHeight <= 0f ||
            drawableWidth <= 0f || drawableHeight <= 0f
        ) {
            // View not laid out yet — retry after it's measured.
            post { applyLeftAlignedVerticalCenter() }
            return
        }

        scaleType = ImageView.ScaleType.MATRIX
        // Fit within the box while preserving aspect ratio.
        val scale = minOf(viewWidth / drawableWidth, viewHeight / drawableHeight)
        val dx = paddingLeft.toFloat()                                   // left aligned
        val dy = paddingTop + (viewHeight - drawableHeight * scale) / 2f // vertical center
        imageMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
    }

    /**
     * Returns true when [url] points to an SVG asset, ignoring any query string
     * or fragment identifier appended to the path.
     */
    private fun isSvgUrl(url: String): Boolean {
        val path = url.substringBefore('?').substringBefore('#')
        return path.endsWith(".svg", ignoreCase = true)
    }
}

