//------------------------------------------------------------------------------
//  File:          APIEndpointTest.kt
//  Author(s):     Bread Financial
//  Date:          25 November 2026
//
//  Descriptions:  This file is part of the BreadPartners SDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.breadpartnerscore.networking.apiendpoint

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class APIEndpointTest {
    private val customBaseURLs = APIBaseURLs(
        brands = "http://localhost:8080",
        rtps = "http://127.0.0.1:9090",
    )

    @Test
    fun buildsLiveURLsForProd() {
        val cases = listOf(
            APIEndpoint.RtpsWebUrl("offer") to "https://acquire1.comenity.net/prescreen/offer",
            APIEndpoint.BpsWebUrl to "https://acquire1.comenity.net/batch-prescreen/start",
            APIEndpoint.BrandStyle("abc") to "https://brands.kmsmep.com/brands/abc/style",
            APIEndpoint.BrandConfig("abc") to "https://brands.kmsmep.com/brands/abc/config",
            APIEndpoint.GeneratePlacements to "https://brands.kmsmep.com/generatePlacements",
            APIEndpoint.ViewPlacement to "https://brands.kmsmep.com/ep/v1/view-placement",
            APIEndpoint.ClickPlacement to "https://brands.kmsmep.com/ep/v1/click-placement",
            APIEndpoint.Prescreen to "https://acquire1.comenity.net/api/prescreen",
            APIEndpoint.VirtualLookup to "https://acquire1.comenity.net/api/virtual_lookup",
        )

        for ((endpoint, expected) in cases) {
            assertEquals("endpoint=$endpoint", expected, endpoint.url(BreadPartnersEnvironment.PROD))
        }
    }

    @Test
    fun rtpsEndpointsFollowEnvironment() {
        assertEquals(
            "https://acquire1stage.comenity.net/api/prescreen",
            APIEndpoint.Prescreen.url(BreadPartnersEnvironment.STAGE),
        )
        assertEquals(
            "https://acquire1uat.comenity.net/api/virtual_lookup",
            APIEndpoint.VirtualLookup.url(BreadPartnersEnvironment.UAT),
        )
    }

    @Test
    fun brandsEndpointsIgnoreEnvironment() {
        for (environment in BreadPartnersEnvironment.entries) {
            assertEquals(
                "env=$environment",
                "https://brands.kmsmep.com/generatePlacements",
                APIEndpoint.GeneratePlacements.url(environment),
            )
        }
    }

    @Test
    fun baseURLsOverrideTakesPrecedenceOverEnvironment() {
        assertEquals(
            "http://localhost:8080/brands/abc/config",
            APIEndpoint.BrandConfig("abc").url(BreadPartnersEnvironment.PROD, customBaseURLs),
        )
        assertEquals(
            "http://127.0.0.1:9090/batch-prescreen/start",
            APIEndpoint.BpsWebUrl.url(BreadPartnersEnvironment.PROD, customBaseURLs),
        )
    }
}

