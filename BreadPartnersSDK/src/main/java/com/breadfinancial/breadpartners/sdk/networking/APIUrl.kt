//------------------------------------------------------------------------------
//  File:          APIUrl.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.networking

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment

// Enum to define different types of API URLs
sealed class APIUrlType {
    data class RTPSWebURL(val type: String) : APIUrlType()
    data class BrandStyle(val brandId: String) : APIUrlType()
    data class BrandConfig(val brandId: String) : APIUrlType()
    data object GeneratePlacements : APIUrlType()
    data object ViewPlacement : APIUrlType()
    data object ClickPlacement : APIUrlType()
    data object PreScreen : APIUrlType()
    data object VirtualLookup : APIUrlType()
    data object BatchPreScreen : APIUrlType()
}

// A centralized class for constructing and managing API URLs
class APIUrl(private val urlType: APIUrlType) {

    companion object {
        private var currentBreadPartnersEnvironment: BreadPartnersEnvironment = BreadPartnersEnvironment.PROD

        /// Set the environment globally
        fun setEnvironment(breadPartnersEnvironment: BreadPartnersEnvironment) {
            currentBreadPartnersEnvironment = breadPartnersEnvironment
        }
    }

    private val baseURL: String
    private val rtpsBaseURL: String

    init {
        when (currentBreadPartnersEnvironment) {
            BreadPartnersEnvironment.STAGE -> {
                baseURL = "https://brands.kmsmep.com"
                rtpsBaseURL = "https://acquire1stage.comenity.net"
            }

            BreadPartnersEnvironment.PROD -> {
                baseURL = "https://brands.kmsmep.com"
                rtpsBaseURL = "https://acquire1.comenity.net"
            }

            BreadPartnersEnvironment.UAT -> {
                baseURL = "https://brands.kmsmep.com"
                rtpsBaseURL = "https://acquire1uat.comenity.net"
            }
        }
    }

    // Generates the correct URL based on the URL type
    val url: String
        get() = when (urlType) {
            is APIUrlType.RTPSWebURL -> "$rtpsBaseURL/prescreen/${urlType.type}"
            is APIUrlType.BrandStyle -> "$baseURL/brands/${urlType.brandId}/style"
            is APIUrlType.BrandConfig -> "$baseURL/brands/${urlType.brandId}/config"
            APIUrlType.GeneratePlacements -> "$baseURL/generatePlacements"
            APIUrlType.ViewPlacement -> "$baseURL/ep/v1/view-placement"
            APIUrlType.ClickPlacement -> "$baseURL/ep/v1/click-placement"
            APIUrlType.PreScreen -> "$rtpsBaseURL/api/prescreen"
            APIUrlType.VirtualLookup -> "$rtpsBaseURL/api/virtual_lookup"
            APIUrlType.BatchPreScreen -> "$rtpsBaseURL/batch-prescreen/start"
        }
}
