//------------------------------------------------------------------------------
//  File:          OfferResponse.kt
//  Author(s):     Bread Financial
//  Date:          19 May 2026
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.models

/// Enum representing the possible offer response values from the WebView.
enum class OfferResponse {
    YES,
    NO,
    NOT_ME,
    ABANDONED,
    PRESCREEN_NO;

    companion object {
        fun fromValue(value: String): OfferResponse? {
            return entries.find { it.name == value }
        }
    }
}
