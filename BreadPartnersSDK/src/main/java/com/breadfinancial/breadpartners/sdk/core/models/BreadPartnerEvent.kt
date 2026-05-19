//------------------------------------------------------------------------------
//  File:          BreadPartnerEvent.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.models

import android.text.Spannable
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

/// Enum representing different events supported by BreadPartnerSDK.
sealed class BreadPartnerEvent {

    override fun toString(): String {
        return when (this) {
            is SdkError -> "${this::class.simpleName}(error=${error.localizedMessage})"
            else -> this::class.simpleName ?: "UnknownEvent"
        }
    }
    /// Renders a text view containing a clickable hyperlink.
    /// - Parameter spannableText: A Spannable object containing the text with a clickable link.
    data class RenderTextViewWithLink(val spannableText: Spannable) : BreadPartnerEvent()

    /// Renders text and a button separately on the screen.
    /// - Parameters:
    ///   - textView: A TextView for displaying text.
    ///   - button: A Button for user interactions.
    data class RenderSeparateTextAndButton(val textView: TextView, val button: Button) : BreadPartnerEvent()

    /// Displays a popup interface on the screen.
    /// - Parameter dialogFragment: A DialogFragment that presents the popup.
    data class RenderPopupView(val dialogFragment: DialogFragment) : BreadPartnerEvent()

    /// Detects when a text element is clicked.
    /// This allows brand partners to trigger any specific action.
    object TextClicked : BreadPartnerEvent()

    /// Detects when an action button inside a popup is tapped.
    /// This provides a callback for brand partners to handle the button tap.
    object ActionButtonTapped : BreadPartnerEvent()

    /// Provides a callback for tracking screen names, typically for analytics.
    /// - Parameter name: The name of the current screen.
    data class ScreenName(val name: String) : BreadPartnerEvent()

    /// Provides a success result from the web view, such as approval confirmation.
    /// - Parameter result: The result object returned on success.
    data class WebViewSuccess(val result: Any) : BreadPartnerEvent()

    /// Provides a result with offers from the UPQ flow.
    /// - Parameter result: The result object returned.
    data class UnifiedOffersReceived(val result: Any) : BreadPartnerEvent()

    /// Provides a result from the UPQ flow after applying for credit card.
    /// - Parameter result: The result object returned.
    data class ReceivePrequalApplicationResult(val result: Any) : BreadPartnerEvent()

    /// Provides a result from the UPQ flow after applying for installment product.
    /// - Parameter result: The result object returned.
    data class ReceiveUnifiedCheckoutApplicationResult(val result: Any) : BreadPartnerEvent()

    /// Detects when application for credit card was submitted from UPQ flow.
    object SubmitPrequalApplication : BreadPartnerEvent()

    /// Provides an error result from the web view, such as a failure response.
    /// - Parameter error: The error object detailing the issue.
    data class WebViewFailure(val error: Throwable) : BreadPartnerEvent()

    /// Detects when the popup is closed at any point and provides a callback.
    object PopupClosed : BreadPartnerEvent()

    /// Provides information about any SDK-related errors.
    /// - Parameter error: The error object detailing the issue.
    data class SdkError(val error: Throwable) : BreadPartnerEvent()

    /// Provides information about any Card-related status.
    /// - Parameter status: object detailing the status.
    data class CardApplicationStatus(val status: Any) : BreadPartnerEvent()

    /// Logs requests, responses, errors, and successes.
    data class OnSDKEventLog(val log: Any) : BreadPartnerEvent()

    /// Provides account information in account lookup call (ECO).
    data class ReceiveAccountExists(val result: Any) : BreadPartnerEvent()

    /// Fires when application have been submitted in different flows.
    object ApplicationCompleted : BreadPartnerEvent()

    /// Provides the offer response from the WebView OFFER_RESPONSE message.
    /// - Parameter offerResponse: The OfferResponse enum value corresponding to the payload.
    data class OfferResponse(val response: com.breadfinancial.breadpartners.sdk.core.models.OfferResponse) : BreadPartnerEvent()
}
