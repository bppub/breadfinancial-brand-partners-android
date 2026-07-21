//------------------------------------------------------------------------------
//  File:          PopupButtonActionExtension.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.extensions

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.PopupDialog
import com.breadfinancial.breadpartners.sdk.utilities.Constants

/**
 * Dismisses the popup, notifies via callback, and cleans up the web view.
 * Called when the close button is tapped, the dialog is cancelled, or the web view triggers a close event.
 */
fun PopupDialog.dismissPopup() {
    callback(BreadPartnerEvent.PopupClosed)
    dismiss()
    webViewManager.destroyWebView()
    return
}

/**
 * Handles the action button tap inside the popup.
 */
fun PopupDialog.onActionButtonTapped() {
    callback(BreadPartnerEvent.ActionButtonTapped)
    if (isWebViewPlacementModelInitialized()) {
        displayEmbeddedOverlay(webViewPlacementModel)
    } else {
        callback(
            BreadPartnerEvent.SdkError(
                error = Exception(Constants.somethingWentWrong)
            )
        )
    }
}