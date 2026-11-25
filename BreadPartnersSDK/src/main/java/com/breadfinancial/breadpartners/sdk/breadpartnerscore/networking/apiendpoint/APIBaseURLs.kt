//------------------------------------------------------------------------------
//  File:          APIBaseURLs.kt
//  Author(s):     Bread Financial
//  Date:          25 November 2026
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.breadpartnerscore.networking.apiendpoint

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment

/**
 * Base URLs used to build SDK API endpoints.
 *
 * @property brands Base URL for brand style, brand config, and placement APIs.
 * @property rtps Base URL for RTPS (prescreen) APIs.
 */
internal data class APIBaseURLs(
    val brands: String,
    val rtps: String,
) {
    companion object {
        private const val BRANDS_BASE_URL = "https://brands.kmsmep.com"

        /**
         * Returns the production base URLs for the given [environment].
         */
        fun live(environment: BreadPartnersEnvironment): APIBaseURLs {
            val rtps =
                when (environment) {
                    BreadPartnersEnvironment.STAGE -> "https://acquire1stage.comenity.net"
                    BreadPartnersEnvironment.PROD -> "https://acquire1.comenity.net"
                    BreadPartnersEnvironment.UAT -> "https://acquire1uat.comenity.net"
                }

            return APIBaseURLs(brands = BRANDS_BASE_URL, rtps = rtps)
        }
    }
}
