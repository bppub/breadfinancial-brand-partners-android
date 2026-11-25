//------------------------------------------------------------------------------
//  File:          LiveAPIEndpointProvider.kt
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
import java.net.MalformedURLException
import java.net.URL

/**
 * Default [APIEndpointProviding] that resolves endpoints for a given environment,
 * optionally using custom [baseURLs].
 */
internal class LiveAPIEndpointProvider(
    private val environment: BreadPartnersEnvironment,
    private val baseURLs: APIBaseURLs? = null,
) : APIEndpointProviding {
    /**
     * @throws IllegalStateException if the endpoint produces an invalid URL.
     */
    override fun url(endpoint: APIEndpoint): URL {
        val urlString = endpoint.url(environment, baseURLs)

        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw IllegalStateException("APIEndpoint produced an invalid URL: $urlString", e)
        }
    }
}

