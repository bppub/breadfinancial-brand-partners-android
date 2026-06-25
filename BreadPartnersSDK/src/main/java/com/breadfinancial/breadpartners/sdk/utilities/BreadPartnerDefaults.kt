//------------------------------------------------------------------------------
//  File:          BreadPartnerDefaults.kt
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

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.breadfinancial.breadpartners.sdk.R
import com.breadfinancial.breadpartners.sdk.core.models.PopUpStyling
import com.breadfinancial.breadpartners.sdk.core.models.PopupActionButtonStyle
import com.breadfinancial.breadpartners.sdk.core.models.PopupTextStyle
import androidx.core.graphics.toColorInt

/**
 * `BreadPartnerDefaults` class provides default configurations/styles/properties
 * used across the BreadPartner SDK.
 */
class BreadPartnerDefaults private constructor() {
    companion object {
        val shared: BreadPartnerDefaults by lazy { BreadPartnerDefaults() }
        // Default color used for gray text to match with web SDK.
        val GRAY_COLOR = "#767676".toColorInt()
        const val TITLE_POPUP_TEXT_SIZE = 16.0f
        const val SUBTITLE_POPUP_TEXT_SIZE = 12.0f
        const val HEADER_POPUP_TEXT_SIZE = 14.0f
        const val HEADING_THREE_POPUP_TEXT_SIZE = 14.0f
        const val PARAGRAPH_POPUP_TEXT_SIZE = 10.0f
        const val CONNECTOR_POPUP_TEXT_SIZE = 14.0f
        const val DISCLOSURE_POPUP_TEXT_SIZE = 10.0f
    }
    // region Default Popup Style
    fun createPopUpStyling(context: Context): PopUpStyling {
        return PopUpStyling(
            loaderColor = Color.parseColor("#0f2233"),
            crossColor = Color.BLACK,
            dividerColor = Color.parseColor("#ececec"),
            borderColor = Color.parseColor("#ececec"),
            titlePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.BLACK,
                textSize = TITLE_POPUP_TEXT_SIZE
            ),
            subTitlePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = GRAY_COLOR,
                textSize = SUBTITLE_POPUP_TEXT_SIZE
            ),
            headerPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = GRAY_COLOR,
                textSize = HEADER_POPUP_TEXT_SIZE
            ),
            headerBgColor = Color.parseColor("#ececec"),
            headingThreePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.BLACK,
                textSize = HEADING_THREE_POPUP_TEXT_SIZE
            ),
            paragraphPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = GRAY_COLOR,
                textSize = PARAGRAPH_POPUP_TEXT_SIZE
            ),
            connectorPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.BLACK,
                textSize = CONNECTOR_POPUP_TEXT_SIZE
            ),
            disclosurePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular),
                    Typeface.BOLD
                ),
                textColor = GRAY_COLOR,
                textSize = DISCLOSURE_POPUP_TEXT_SIZE
            ),
            actionButtonStyle = PopupActionButtonStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.WHITE,
                backgroundColor = Color.BLACK,
                cornerRadius = 60.0F,
            )
        )
    }
    //endregion

}


