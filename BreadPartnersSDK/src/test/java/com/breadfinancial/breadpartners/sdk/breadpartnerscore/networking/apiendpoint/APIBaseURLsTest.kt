//------------------------------------------------------------------------------
//  File:          APIBaseURLsTest.kt
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

class APIBaseURLsTest {
    @Test
    fun liveUsesSameBrandsURLForAllEnvironments() {
        for (environment in BreadPartnersEnvironment.entries) {
            assertEquals(
                "env=$environment",
                "https://brands.kmsmep.com",
                APIBaseURLs.live(environment).brands,
            )
        }
    }

    @Test
    fun liveResolvesRtpsURLPerEnvironment() {
        val expected = mapOf(
            BreadPartnersEnvironment.STAGE to "https://acquire1stage.comenity.net",
            BreadPartnersEnvironment.PROD to "https://acquire1.comenity.net",
            BreadPartnersEnvironment.UAT to "https://acquire1uat.comenity.net",
        )

        for ((environment, rtps) in expected) {
            assertEquals("env=$environment", rtps, APIBaseURLs.live(environment).rtps)
        }
    }
}

