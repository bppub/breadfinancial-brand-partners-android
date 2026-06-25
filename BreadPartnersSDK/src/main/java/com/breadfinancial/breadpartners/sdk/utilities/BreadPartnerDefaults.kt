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
        val grayColor = "#767676".toColorInt()
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
                textSize = 16.0f
            ),
            subTitlePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = grayColor,
                textSize = 12.0f
            ),
            headerPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = grayColor,
                textSize = 14.0f
            ),
            headerBgColor = Color.parseColor("#ececec"),
            headingThreePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.BLACK,
                textSize = 14.0f
            ),
            paragraphPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = grayColor,
                textSize = 10.0f
            ),
            connectorPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular), Typeface.BOLD
                ),
                textColor = Color.BLACK,
                textSize = 14.0f
            ),
            disclosurePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    ResourcesCompat.getFont(context, R.font.arial_regular),
                    Typeface.BOLD
                ),
                textColor = grayColor,
                textSize = 10.0f
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


