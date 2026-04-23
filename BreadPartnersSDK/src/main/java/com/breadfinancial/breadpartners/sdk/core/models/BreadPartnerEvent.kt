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
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementButtonConfiguration

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

    /// Renders an image button that can be clicked to trigger an action.
    /// - Parameters:
    ///   - containerView: A ViewGroup containing the image and optional text.
    ///   - imageUrl: URL of the image to load.
    ///   - buttonText: Text to display with the button (optional).
    ///   - configuration: Configuration for button appearance and layout.
    ///   - onClick: Callback to be invoked when the button is clicked.
    data class RenderImageButton(
        val containerView: ViewGroup,
        val imageUrl: String,
        val buttonText: String?,
        val configuration: PlacementButtonConfiguration = PlacementButtonConfiguration(),
        val onClick: () -> Unit
    ) : BreadPartnerEvent()

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

}
