//------------------------------------------------------------------------------
//  File:          MerchantConfiguration.kt
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
 * Data model representing merchant-related configuration
 */
data class MerchantConfiguration(
    var buyer: BreadPartnersBuyer? = null,
    var loyaltyID: String? = null,
    var campaignID: String? = null,
    var storeNumber: String? = null,
    var departmentId: String? = null,
    var existingCardHolder: Boolean? = null,
    var cardholderTier: String? = null,
    var env: BreadPartnersEnvironment? = null,
    var cardEnv: String? = null,
    var channel: String? = null,
    var subchannel: String? = null,
    var clerkId: String? = null,
    var cardChoiceCode: String? = null,
    var overrideKey: String? = null,
    var clientVariable1: String? = null,
    var clientVariable2: String? = null,
    var clientVariable3: String? = null,
    var clientVariable4: String? = null,
    var accountId: String? = null,
    var applicationId: String? = null,
    var invoiceNumber: String? = null,
    var paymentMode: PaymentMode? = null,
    var providerConfig: Map<String, Any>? = null,
    var skipVerification: Boolean? = null,
    var custom: Map<String, Any>? = null
) {
    enum class PaymentMode {
        FULL, SPLIT
    }
}

data class BreadPartnersBuyer(
    var givenName: String? = null,
    var familyName: String? = null,
    var additionalName: String? = null,
    var birthDate: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var alternativePhone: String? = null,
    var billingAddress: BreadPartnersAddress? = null,
    var shippingAddress: BreadPartnersAddress? = null
)

data class BreadPartnersAddress(
    var address1: String,
    var address2: String? = null,
    var country: String? = null,
    var locality: String? = null,
    var region: String? = null,
    var postalCode: String? = null
)


