//------------------------------------------------------------------------------
//  File:          TextPlacementUICreator.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.extensions

import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentRenderer

/**
 * Creates a plain TextView from the given HTMLContentRenderer
 */
fun HTMLContentRenderer.createPlainTextView(): TextView {
    val textView = TextView(thisContext).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        setBackgroundColor(Color.TRANSPARENT)
        text = textPlacementModel!!.contentText ?: ""
    }
    return textView
}

/**
 * Creates a Button from the given HTMLContentRenderer
 */
fun HTMLContentRenderer.createActionButton(): Button {
    val normalText = textPlacementModel?.contentText ?: ""
    var clickableText = textPlacementModel?.actionLink ?: ""

    if (clickableText.isEmpty()) {
        clickableText = normalText
    }

    val button = Button(thisContext).apply {
        text = clickableText
        contentDescription = clickableText
        setOnClickListener { handleButtonTap(this) }
    }

    return button
}
