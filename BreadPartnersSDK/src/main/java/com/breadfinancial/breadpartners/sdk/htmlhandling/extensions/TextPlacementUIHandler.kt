//------------------------------------------------------------------------------
//  File:          TextPlacementUIHandler.kt
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

import android.widget.Button
import android.widget.TextView
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentParser
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentRenderer
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.InteractiveText
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementActionType
import com.breadfinancial.breadpartners.sdk.utilities.Constants

/**
 * Renders both text and a button from the HTMLContent
 */
fun HTMLContentRenderer.renderTextAndButton() {
    val plainTextView = createPlainTextView()
    val actionButton = createActionButton()

    callback(BreadPartnerEvent.RenderSeparateTextAndButton(plainTextView, actionButton))
}

/**
 * Renders a TextView from the HTMLContent with clickable link.
 */
fun HTMLContentRenderer.renderTextViewWithLink() {
    val interactiveText = InteractiveText(thisContext!!)
    val spannable = interactiveText.configure(textPlacementModel!!) {
        handleLinkInteraction()
    }
    callback(BreadPartnerEvent.RenderTextViewWithLink(spannableText = spannable))
}

/**
 * Renders a composite view with image and text as a button.
 * Uses RenderSeparateTextAndButton event with image URL stored in button tag.
 */
fun HTMLContentRenderer.renderImageButton() {
    val context = thisContext!!

    // Create a TextView for any additional text (optional, can be null)
    val textView = TextView(context).apply {
        text = "" // Empty for now, merchant can use this if needed
        visibility = android.view.View.GONE
    }

    // Create a Button that will display both image and text
    val button = Button(context).apply {
        text = textPlacementModel?.contentText ?: "Pay Over Time"
        contentDescription = textPlacementModel?.contentText ?: "Bread Financial Button"

        // Store image URL in tag so merchant app can load it
        tag = textPlacementModel?.imageUrl ?: ""

        // Set click handler
        setOnClickListener { handleLinkInteraction() }
    }

    callback(BreadPartnerEvent.RenderSeparateTextAndButton(textView, button))
}

/**
 * Handles tap events on buttons rendered.
 */
fun HTMLContentRenderer.handleButtonTap(sender: Button) {
    sender.contentDescription ?: return
    handleLinkInteraction()
}

/**
 * Handles user interactions with links rendered.
 */
fun HTMLContentRenderer.handleLinkInteraction() {
    val actionType = textPlacementModel?.actionType?.let { HTMLContentParser().handleActionType(it) }
    when (actionType) {
        PlacementActionType.SHOW_OVERLAY -> {
            handlePopupPlacement(textPlacementModel!!, responseModel!!)
        }

        PlacementActionType.NO_ACTION -> {
            callback(BreadPartnerEvent.TextClicked)
        }

        else -> {
            showAlert(Constants.nativeSDKAlertTitle(), Constants.missingTextPlacementError)
        }
    }
}


