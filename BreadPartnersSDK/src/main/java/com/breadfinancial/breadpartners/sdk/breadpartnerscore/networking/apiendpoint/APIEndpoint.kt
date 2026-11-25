//------------------------------------------------------------------------------
//  File:          APIEndpoint.kt
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
 * Describes every API endpoint the SDK talks to and builds its full URL.
 */
internal sealed class APIEndpoint {
    data class RtpsWebUrl(
        val type: String,
    ) : APIEndpoint()

    data object BpsWebUrl : APIEndpoint()

    data class BrandStyle(
        val brandId: String,
    ) : APIEndpoint()

    data class BrandConfig(
        val brandId: String,
    ) : APIEndpoint()

    data object GeneratePlacements : APIEndpoint()

    data object ViewPlacement : APIEndpoint()

    data object ClickPlacement : APIEndpoint()

    data object Prescreen : APIEndpoint()

    data object VirtualLookup : APIEndpoint()

    /**
     * Builds the full URL string for this endpoint.
     *
     * @param environment Environment used to resolve base URLs when [baseURLs] is null.
     * @param baseURLs Optional base URLs that take precedence over the environment defaults.
     */
    fun url(
        environment: BreadPartnersEnvironment,
        baseURLs: APIBaseURLs? = null,
    ): String {
        val resolved = baseURLs ?: APIBaseURLs.live(environment)
        val brandsBaseUrl = resolved.brands
        val rtpsBaseUrl = resolved.rtps

        return when (this) {
            is RtpsWebUrl -> "$rtpsBaseUrl/prescreen/$type"
            BpsWebUrl -> "$rtpsBaseUrl/batch-prescreen/start"
            is BrandStyle -> "$brandsBaseUrl/brands/$brandId/style"
            is BrandConfig -> "$brandsBaseUrl/brands/$brandId/config"
            GeneratePlacements -> "$brandsBaseUrl/generatePlacements"
            ViewPlacement -> "$brandsBaseUrl/ep/v1/view-placement"
            ClickPlacement -> "$brandsBaseUrl/ep/v1/click-placement"
            Prescreen -> "$rtpsBaseUrl/api/prescreen"
            VirtualLookup -> "$rtpsBaseUrl/api/virtual_lookup"
        }
    }
}
