//------------------------------------------------------------------------------
//  File:          DeviceInformationProviderTest.kt
//  Author(s):     Bread Financial
//  Date:          31 December 2026
//
//  Descriptions:  This file is part of the BreadPartners SDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.breadpartners.infrastructure.device

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class DeviceInformationProviderTest {
    @Test
    fun userAgentIncludesDeviceModelAndOperatingSystem() {
        assertEquals(
            "${Build.MANUFACTURER} ${Build.MODEL}; Android ${Build.VERSION.RELEASE}",
            DeviceInformationProvider.userAgent,
        )
    }
}
