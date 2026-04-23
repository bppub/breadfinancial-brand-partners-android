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

import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
 * Renders a composite view with button overlaying the image.
 * Button is stacked on top of the image.
 */
fun HTMLContentRenderer.renderImageButton() {
    val context = thisContext!!

    // Create a FrameLayout container for stacking (image below, button on top)
    val container = FrameLayout(context).apply {
        isClickable = true
        isFocusable = true
        setPadding(16, 16, 16, 16)
    }

    val imageUrl = textPlacementModel?.imageUrl ?: ""
    val buttonText = textPlacementModel?.contentText

    callback(BreadPartnerEvent.RenderImageButton(
        containerView = container,
        imageUrl = imageUrl,
        buttonText = buttonText,
        onClick = { handleLinkInteraction() }
    ))
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


