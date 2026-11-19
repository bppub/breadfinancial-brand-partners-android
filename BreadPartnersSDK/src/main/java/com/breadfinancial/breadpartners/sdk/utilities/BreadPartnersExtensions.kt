//------------------------------------------------------------------------------
//  File:          BreadPartnersExtensions.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.utilities

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

/**
 * Object provides reusable extension methods that can be used across apps,
 * integrating the Bread Partners SDK.
 */
object BreadPartnersExtensions {
    /**
     * Replaces an existing TextView within a ViewGroup
     * with a new TextView, maintaining its layout position.
     */
    fun ViewGroup.replaceTextView(
        oldTextView: TextView?,
        newTextView: TextView,
    ) {
        val index = this.indexOfChild(oldTextView)
        if (index != -1 && oldTextView != null) {
            removeViewAt(index)

            newTextView.apply {
                id = oldTextView.id

                this.layoutParams.apply {
                    width = oldTextView.layoutParams.width
                    height = oldTextView.layoutParams.height

                    if (this is ViewGroup.MarginLayoutParams) {
                        val oldMargins = oldTextView.layoutParams as? ViewGroup.MarginLayoutParams
                        oldMargins?.let {
                            setMargins(
                                it.leftMargin,
                                it.topMargin,
                                it.rightMargin,
                                it.bottomMargin,
                            )
                        }
                    }
                }

                gravity = (oldTextView as? AppCompatTextView)?.gravity ?: Gravity.START
                textAlignment = oldTextView.textAlignment

                setPadding(
                    oldTextView.paddingLeft,
                    oldTextView.paddingTop,
                    oldTextView.paddingRight,
                    oldTextView.paddingBottom,
                )
                background = oldTextView.background
                visibility = oldTextView.visibility
            }

            addView(newTextView, index)
        }
    }

    /**
     * Replaces an existing Button within a ViewGroup
     * with a new Button, preserving layout position.
     */
    fun ViewGroup.replaceButton(
        oldButton: Button?,
        newButton: Button,
    ) {
        val index = this.indexOfChild(oldButton)
        if (index != -1 && oldButton != null) {
            when (val oldLayoutParams = oldButton.layoutParams) {
                is LinearLayout.LayoutParams -> {
                    val newLayoutParams = LinearLayout.LayoutParams(oldLayoutParams)
                    newLayoutParams.gravity = oldLayoutParams.gravity
                    newButton.layoutParams = newLayoutParams
                }

                is FrameLayout.LayoutParams -> {
                    val newLayoutParams = FrameLayout.LayoutParams(oldLayoutParams)
                    newLayoutParams.gravity = oldLayoutParams.gravity
                    newButton.layoutParams = newLayoutParams
                }

                else -> {
                    newButton.layoutParams = oldLayoutParams
                }
            }

            newButton.layoutParams.apply {
                width = oldButton.layoutParams.width
                height = oldButton.layoutParams.height
            }

            removeViewAt(index)

            newButton.setPadding(
                newButton.paddingLeft,
                newButton.paddingTop,
                newButton.paddingRight,
                newButton.paddingBottom,
            )
            addView(newButton, index)
        }
    }

    fun String?.takeIfNotEmpty(): String? {
        return this?.trim()?.takeIf { it.isNotEmpty() }
    }
}
