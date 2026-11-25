//------------------------------------------------------------------------------
//  File:          APIEndpointProviding.kt
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

import java.net.URL

/**
 * Resolves an [APIEndpoint] to a concrete [URL].
 *
 * Abstracting this lets callers depend on the interface instead of a global
 * environment, and allows tests to supply their own implementation.
 */
internal interface APIEndpointProviding {
    fun url(endpoint: APIEndpoint): URL
}

