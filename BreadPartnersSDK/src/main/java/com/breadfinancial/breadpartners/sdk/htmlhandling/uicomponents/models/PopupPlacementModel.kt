//------------------------------------------------------------------------------
//  File:          PopupPlacementModel.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models

import android.text.Spanned

/**
 * Data model representing the content and configuration required
 * to render a popup within the SDK.
 */
data class PopupPlacementModel(
    val overlayType: String,
    val location: String,
    val brandLogoUrl: String,
    val webViewUrl: String,
    val overlayTitle: Spanned,
    val overlaySubtitle: Spanned,
    val overlayContainerBarHeading: Spanned,
    val bodyHeader: Spanned,
    val primaryActionButtonAttributes: PrimaryActionButtonModel?,
    val dynamicBodyModel: DynamicBodyModel,
    val disclosure: Spanned,
) {
    data class DynamicBodyModel(
        val bodyDiv: Map<String, DynamicBodyContent>,
    )

    data class DynamicBodyContent(
        val tagValuePairs: Map<String, Spanned>,
    )
}

/**
 * Data model representing the primary action button
 * displayed inside a popup.
 */
data class PrimaryActionButtonModel(
    val dataOverlayType: String? = null,
    val dataContentFetch: String? = null,
    val dataActionTarget: String? = null,
    val dataActionType: String? = null,
    val dataActionContentId: String? = null,
    val dataLocation: String? = null,
    val buttonText: String? = null,
)

/**
 * Enum representing different types of popup overlays.
 */
enum class PlacementOverlayType(
    val value: String,
) {
    EMBEDDED_OVERLAY("EMBEDDED_OVERLAY"),
    SINGLE_PRODUCT_OVERLAY("SINGLE_PRODUCT_OVERLAY"),
    ;

    companion object {
        fun fromValue(value: String): PlacementOverlayType? {
            return entries.find { it.value == value }
        }
    }
}
