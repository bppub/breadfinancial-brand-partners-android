//------------------------------------------------------------------------------
//  File:          RTPSData.kt
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

/**
 * Data class that provides configurations for the `silentRTPSRequest` method.
 */
data class RTPSData(
    var financingType: String? = null,
    var order: Order? = null,
    var locationType: BreadPartnersLocationType? = null,
    var screenName: String? = null,
    var cardType: String? = null,
    var country: String? = null,
    var prescreenId: Long? = null,
    var correlationData: String? = null,
    var customerAcceptedOffer: Boolean? = null,
    var channel: String? = null,
    var subChannel: String? = null,
    var mockResponse: BreadPartnersMockOptions? = null,
)

/**
 * Enum representing mock options used to simulate RTPs call behavior
 * for testing and development purposes within the SDK.
 */
enum class BreadPartnersMockOptions(
    val value: String,
) {
    NO_MOCK(""),
    SUCCESS("success"),
    NO_HIT("noHit"),
    MAKE_OFFER("makeOffer"),
    ACKNOWLEDGE("ackknowledge"),
    EXISTING_ACCOUNT("existingAccount"),
    EXISTING_OFFER("existingOffer"),
    NEW_OFFER("newOffer"),
    ERROR("error"),
}
