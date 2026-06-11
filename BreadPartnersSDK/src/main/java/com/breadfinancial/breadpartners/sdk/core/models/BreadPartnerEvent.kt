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
import com.breadfinancial.breadpartners.sdk.core.models.OfferResponse as OfferResponseEnum

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
    data class OfferResponse(val response: OfferResponseEnum) : BreadPartnerEvent()

    /// Fires when Google Wallet push provisioning is requested from the WebView.
    ///
    /// This event is raised through two paths:
    ///  1. **URL interception** – the WebView's `window.open` override detects a
    ///     `pay.google.com/.../pushprovisioning` URL and the SDK attempts to deep-link
    ///     it directly to the Google Wallet app. When the Wallet app is absent the
    ///     event is forwarded to the host application via [provisioningUrl].
    ///  2. **Explicit JS bridge** – the web page calls
    ///     `window.Android.provisionToGoogleWallet(json)` with the issuer-supplied
    ///     card data. The host app can then call
    ///     `TapAndPayClient.pushTokenize()` using the provided fields.
    ///
    /// - Parameters:
    ///   - provisioningUrl: Full Google Pay push-provisioning frame URL (path 1).
    ///   - opc: Base-64-encoded Opaque Payment Card blob from the issuer server (path 2).
    ///   - cardNetwork: Card network string, e.g. "VISA", "MASTERCARD" (path 2).
    ///   - lastDigits: Last four digits of the card (path 2).
    ///   - tokenServiceProvider: TSP identifier, e.g. "VISA", "MASTERCARD" (path 2).
    ///   - displayName: Human-readable card name shown in the Wallet UI (path 2).
    data class PushProvisionGoogleWallet(
        val provisioningUrl: String? = null,
        val opc: String? = null,
        val cardNetwork: String? = null,
        val lastDigits: String? = null,
        val tokenServiceProvider: String? = null,
        val displayName: String? = null
    ) : BreadPartnerEvent()
}
