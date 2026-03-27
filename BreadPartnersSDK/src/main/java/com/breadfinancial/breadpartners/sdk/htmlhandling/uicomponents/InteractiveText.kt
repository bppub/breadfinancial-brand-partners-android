//------------------------------------------------------------------------------
//  File:          InteractiveText.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("unused")

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementActionType
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.TextPlacementModel

@SuppressLint("AppCompatCustomView")
class InteractiveText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private var tapHandler: ((String) -> Unit)? = null
    private var normalText = ""
    private var clickableText = ""
    private var actionType = ""
    private var htmlContent = ""

    init {
        isClickable = true
        movementMethod = LinkMovementMethod.getInstance()
        isFocusableInTouchMode = true
    }

    fun configure(
        textPlacementModel: TextPlacementModel, link: ((String) -> Unit)? = null
    ): Spannable {
        this.tapHandler = link
        actionType = textPlacementModel.actionType ?: ""

        normalText = textPlacementModel.contentText ?: ""
        clickableText = textPlacementModel.actionLink ?: ""
        htmlContent = textPlacementModel.htmlContent ?: ""

        if (clickableText.isEmpty()) {
            clickableText = normalText
            normalText = ""
        }

        val spannableContent: Spannable = if (actionType == PlacementActionType.NO_ACTION.value) {
            createSpannableNoActionText(htmlContent)
        } else {
            createSpannableText("$normalText ", clickableText)
        }

        text = spannableContent
        return spannableContent
    }

    private fun createSpannableNoActionText(
        htmlContent: String
    ): Spannable {
        val htmlSpanned = HtmlCompat.fromHtml(
            htmlContent,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
        )

        val spannableString = SpannableString(htmlSpanned)

        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    tapHandler?.invoke("")
                    widget.clearFocus()
                    widget.invalidate()
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }
            },
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    private fun createSpannableText(
        normalText: String, clickableText: String
    ): Spannable {

        textSize = 16.0F

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = layoutParams

        val spannableString = SpannableString(normalText + clickableText)

        val normalTextEndIndex = normalText.length
        spannableString.setSpan(
            StyleSpan(Typeface.NORMAL), 0, normalTextEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableTextEndIndex = normalTextEndIndex + clickableText.length

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            normalTextEndIndex,
            clickableTextEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    tapHandler?.invoke(clickableText)
                    widget.clearFocus()
                    widget.invalidate()
                }
            },
            normalTextEndIndex,
            clickableTextEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    fun setCustomFont(fontResId: Int) {
        try {
            typeface = ResourcesCompat.getFont(context, fontResId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

