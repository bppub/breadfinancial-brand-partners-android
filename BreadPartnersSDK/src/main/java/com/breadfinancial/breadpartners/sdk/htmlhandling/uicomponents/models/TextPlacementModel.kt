//------------------------------------------------------------------------------
//  File:          TextPlacementModel.kt
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

/**
 * Data model representing text content and configuration,
 * typically used for rendering text.
 */
data class TextPlacementModel(
    val actionType: String?,
    val actionTarget: String?,
    val contentText: String?,
    val actionLink: String?,
    val actionContentId: String?,
//    val htmlContent: String?
)

/**
 * Enum representing the types of actions triggered when
 * interacting with linked text elements.
 */
enum class PlacementActionType(val value: String) {
    SHOW_OVERLAY("SHOW_OVERLAY"),
    REDIRECT("REDIRECT"),
    BREAD_APPLY("BREAD_APPLY"),
    REDIRECT_INTERNAL("REDIRECT_INTERNAL"),
    VERSATILE_ECO("VERSATILE_ECO"),
    NO_ACTION("NO_ACTION");

    companion object {
        fun fromValue(value: String): PlacementActionType? {
            return entries.find { it.value == value }
        }
    }
}
