//------------------------------------------------------------------------------
//  File:          PlacementsConfiguration.kt
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

import android.graphics.Color
import android.graphics.Typeface
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnerDefaults
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnerDefaults.Companion.LIGHT_GRAY_COLOR

/**
 * Data class that provides configurations for the `registerPlacement` or `submitRTPS` methods.
 *
 * @property placementData Defines text placements on the brand partner screen for the `registerPlacementFlow`.
 * @property rtpsData Specifies the real-time pre-screen configuration for the prescreen flow.
 * @property popUpStyling Configures the popup styling for each element rendered within the popup.
 */
data class PlacementsConfiguration(
    val placementData: PlacementData? = null,
    val rtpsData: RTPSData? = null,
    var popUpStyling: PopUpStyling? = null,
)

/**
 * Structure used to provide styling configurations for the `PopupController`.
 *
 * - Colors are defined using `UIColor` or `CGColor` for various popup elements like header background, border.
 * - Text styles are configured using `PopupTextStyle` structure for titles, headers, and other text elements.
 * - Button style can be optionally specified using `PopupActionButtonStyle`.
 */
data class PopUpStyling(
    val loaderColor: Int = Color.BLACK,
    val crossColor: Int = Color.BLACK,
    val dividerColor: Int = LIGHT_GRAY_COLOR,
    val borderColor: Int = LIGHT_GRAY_COLOR,
    val backgroundColor: Int = Color.WHITE,
    val titlePopupTextStyle: PopupTextStyle = PopupTextStyle(
        textColor = Color.BLACK,
        textSize = BreadPartnerDefaults.TITLE_POPUP_TEXT_SIZE
    ),
    val subTitlePopupTextStyle: PopupTextStyle = PopupTextStyle(
        textSize = BreadPartnerDefaults.SUBTITLE_POPUP_TEXT_SIZE
    ),
    val headerPopupTextStyle: PopupTextStyle = PopupTextStyle(
        textSize = BreadPartnerDefaults.HEADER_POPUP_TEXT_SIZE
    ),
    val headerBgColor: Int = Color.LTGRAY,
    val headingThreePopupTextStyle: PopupTextStyle = PopupTextStyle(
        textColor = Color.BLACK,
        textSize = BreadPartnerDefaults.HEADING_THREE_POPUP_TEXT_SIZE
    ),
    val paragraphPopupTextStyle: PopupTextStyle = PopupTextStyle(
        textSize = BreadPartnerDefaults.PARAGRAPH_POPUP_TEXT_SIZE
    ),
    val connectorPopupTextStyle: PopupTextStyle = PopupTextStyle(
        textColor = Color.BLACK,
        textSize = BreadPartnerDefaults.CONNECTOR_POPUP_TEXT_SIZE
    ),
    val disclosurePopupTextStyle: PopupTextStyle = PopupTextStyle(
        textSize = BreadPartnerDefaults.DISCLOSURE_POPUP_TEXT_SIZE
    ),
    var actionButtonStyle: PopupActionButtonStyle = PopupActionButtonStyle(),
)


/**
 * Structure that defines text styling config for popup elements.
 *
 * - `font`: Specifies the font family and font size for the text.
 * - `textColor`: Specifies the color of the text.
 * - `textSize`: Specifies the size of the text.
 */
data class PopupTextStyle(
    val font: Typeface? = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
    val textColor: Int? = BreadPartnerDefaults.GRAY_COLOR,
    val textSize: Float? = null,
    val superscriptTextScale: Float = BreadPartnerDefaults.SUPERSCRIPT_TEXT_SCALE
)

/**
 * Structure that defines style configurations for action buttons in popups.
 *
 * - `font`: Specifies the font family and font size for the button title.
 * - `textColor`: Specifies the color of the button title text.
 * - `frame`: Specifies the frame dimensions for the button.
 * - `backgroundColor`: Specifies the background color of the button.
 * - `cornerRadius`: Specifies the corner radius for rounded button edges.
 * - `padding`: Specifies the padding within the button and title.
 */
data class PopupActionButtonStyle(
    val font: Typeface? = null,
    val textColor: Int = Color.WHITE,
    val textSize: Float = 12F,
    val backgroundColor: Int = Color.BLACK,
    val cornerRadius: Float = BreadPartnerDefaults.ACTION_BUTTON_CORNER_RADIUS
)

