//------------------------------------------------------------------------------
//  File:          DeviceInformationProvider.kt
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

/**
 * Provides information about the device the SDK is running on.
 */
internal object DeviceInformationProvider {
    /**
     * A simplified User-Agent string based on the device's manufacturer, model, and OS version,
     * for example `Google Pixel 7; Android 13`.
     */
    val userAgent: String
        get() {
            val osVersion = "Android ${Build.VERSION.RELEASE}" // e.g., "Android 13"
            val deviceModel = Build.MODEL // e.g., "Pixel 7"
            val manufacturer = Build.MANUFACTURER // e.g., "Google"
            return "$manufacturer $deviceModel; $osVersion" // Google Pixel 7; Android 13
        }
}
