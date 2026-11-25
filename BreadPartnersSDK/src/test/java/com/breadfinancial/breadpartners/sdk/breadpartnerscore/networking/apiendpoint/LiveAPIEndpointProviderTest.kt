//------------------------------------------------------------------------------
//  File:          LiveAPIEndpointProviderTest.kt
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
import java.net.MalformedURLException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LiveAPIEndpointProviderTest {
    @Test
    fun returnsURLForEnvironment() {
        val provider = LiveAPIEndpointProvider(BreadPartnersEnvironment.STAGE)

        val url = provider.url(APIEndpoint.RtpsWebUrl("offer"))

        assertEquals("https://acquire1stage.comenity.net/prescreen/offer", url.toString())
        assertEquals("acquire1stage.comenity.net", url.host)
    }

    @Test
    fun usesCustomBaseURLsWhenProvided() {
        val provider = LiveAPIEndpointProvider(
            environment = BreadPartnersEnvironment.PROD,
            baseURLs = APIBaseURLs(brands = "http://localhost:8080", rtps = "http://localhost:8080"),
        )

        assertEquals(
            "http://localhost:8080/brands/abc/style",
            provider.url(APIEndpoint.BrandStyle("abc")).toString(),
        )
    }

    @Test
    fun throwsIllegalStateExceptionForInvalidURL() {
        val provider = LiveAPIEndpointProvider(
            environment = BreadPartnersEnvironment.PROD,
            baseURLs = APIBaseURLs(brands = "not-a-valid-scheme://host", rtps = "not-a-valid-scheme://host"),
        )

        try {
            provider.url(APIEndpoint.GeneratePlacements)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("not-a-valid-scheme://host/generatePlacements"))
            assertTrue(e.cause is MalformedURLException)
        }
    }
}

