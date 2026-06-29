//------------------------------------------------------------------------------
//  File:          RTPSRequestBuilder.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.networking.requestbuilders

import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.RTPSData
import com.breadfinancial.breadpartners.sdk.networking.models.RTPSRequest
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.takeIfNotEmpty

/**
 * Builder class to create a RTPSRequest for RTPS flow.
 */
class RTPSRequestBuilder(
    private val merchantConfiguration: MerchantConfiguration,
    private val rtpsData: RTPSData,
    private val reCaptchaToken: String
) {

    fun build(): RTPSRequest {
        val buyer = merchantConfiguration.buyer
        if (rtpsData.prescreenId == null) {
            // Fetch PreScreenID using prescreen end point with user info and recaptcha token
            return RTPSRequest(
                urlPath = rtpsData.screenName.takeIfNotEmpty(),
                firstName = buyer?.givenName.takeIfNotEmpty(),
                lastName = buyer?.familyName.takeIfNotEmpty(),
                address1 = buyer?.billingAddress?.address1.takeIfNotEmpty(),
                country = buyer?.billingAddress?.country.takeIfNotEmpty(),
                city = buyer?.billingAddress?.locality.takeIfNotEmpty(),
                state = buyer?.billingAddress?.region.takeIfNotEmpty(),
                zip = buyer?.billingAddress?.postalCode.takeIfNotEmpty(),
                storeNumber = merchantConfiguration.storeNumber.takeIfNotEmpty() ?: "8883",
                location = rtpsData.locationType?.value.takeIfNotEmpty(),
                channel = merchantConfiguration.channel.takeIfNotEmpty(),
                subchannel = merchantConfiguration.subchannel.takeIfNotEmpty(),
                reCaptchaToken = reCaptchaToken.takeIfNotEmpty(),
                mockResponse = rtpsData.mockResponse?.value,
                overrideConfig = RTPSRequest.OverrideConfig(enhancedPresentment = true),
                customerAcceptedOffer = rtpsData.customerAcceptedOffer,
                mobilePhone = buyer?.phone.takeIfNotEmpty(),
                emailAddress = buyer?.email.takeIfNotEmpty(),
                alternativePhone = buyer?.alternativePhone.takeIfNotEmpty(),
                )
        } else {
            // Validate PreScreenID using virtuallookup
            return RTPSRequest(
                urlPath = rtpsData.screenName.takeIfNotEmpty(),
                prescreenId = rtpsData.prescreenId?.toString().takeIfNotEmpty(),
                channel = merchantConfiguration.channel.takeIfNotEmpty(),
                subchannel = merchantConfiguration.subchannel.takeIfNotEmpty(),
                mockResponse = rtpsData.mockResponse?.value,
                overrideConfig = RTPSRequest.OverrideConfig(enhancedPresentment = true),
                customerAcceptedOffer = rtpsData.customerAcceptedOffer
            )
        }

    }
}
