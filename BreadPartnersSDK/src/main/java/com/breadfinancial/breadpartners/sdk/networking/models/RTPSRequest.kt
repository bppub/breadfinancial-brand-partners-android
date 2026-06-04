//------------------------------------------------------------------------------
//  File:          RTPSRequest.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.networking.models

/**
 * Represents the request model for an RTPS call.
 */
data class RTPSRequest(
    val urlPath: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val address1: String? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val storeNumber: String? = null,
    val location: String? = null,
    val channel: String? = null,
    val subchannel: String? = null,
    var reCaptchaToken: String? = null,
    val mockResponse: String? = null,
    val overrideConfig: OverrideConfig? = null,
    val prescreenId: String? = null,
    val platform: String = "android",
    val customerAcceptedOffer: Boolean? = null,
    val mobilePhone: String? = null,
    val email: String? = null
) {

    data class OverrideConfig(
        val enhancedPresentment: Boolean? = null
    )
}
