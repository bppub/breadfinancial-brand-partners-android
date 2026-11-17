//------------------------------------------------------------------------------
//  File:          RTPSResponseExtension.swift
//  Author(s):     Bread Financial
//  Date:          17 March 2026
//
//  Descriptions:  This file is part of the BreadPartnersSDK for iOS,
//  providing extension methods for mapping RTPSResponse data to configuration
//  objects.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.extensions

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersAddress
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersBuyer
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.networking.models.RTPSResponse

/**
 * Updates a MerchantConfiguration with buyer information from the RTPS response.
 *
 * This method creates an updated copy of the merchant configuration with buyer
 * information populated from the account lookup API response.
 *
 * - Parameter merchantConfiguration: The original merchant configuration.
 * - Returns: An updated MerchantConfiguration with buyer data from the response
 */
fun RTPSResponse.updateMerchantConfiguration(merchantConfiguration: MerchantConfiguration): MerchantConfiguration {
    val updatedConfig = merchantConfiguration
    val buyer = merchantConfiguration.buyer ?: BreadPartnersBuyer()

    this.firstName?.let { buyer.givenName = it }
    this.lastName?.let { buyer.familyName = it }
    this.middleInitial?.let { buyer.additionalName = it }

    if (this.address1 != null || this.city != null || this.state != null || this.zip != null) {
        val billingAddress = buyer.billingAddress ?: BreadPartnersAddress("")

        this.address1?.let { billingAddress.address1 = it }
        this.address2?.let { billingAddress.address2 = it }
        this.city?.let { billingAddress.locality = it }
        this.state?.let { billingAddress.region = it }
        this.zip?.let { billingAddress.postalCode = it }

        buyer.billingAddress = billingAddress
    }
    updatedConfig.buyer = buyer

    return updatedConfig
}
