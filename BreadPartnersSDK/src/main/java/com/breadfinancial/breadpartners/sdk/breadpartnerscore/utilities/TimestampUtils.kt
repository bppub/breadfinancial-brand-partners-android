//------------------------------------------------------------------------------
//  File:          TimestampUtils.kt
//  Author(s):     Bread Financial
//  Date:          31 December 2026
//
//  Descriptions:  This file is part of the BreadPartners SDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.breadpartnerscore.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Platform-independent helpers for timestamp formatting.
 */
internal object TimestampUtils {
    /**
     * Formats the given date as a UTC timestamp with milliseconds,
     * for example `2026-09-02T05:15:30.456Z`.
     *
     * The locale is fixed to [Locale.US] so the output always uses the Gregorian
     * calendar and ASCII digits, regardless of the device locale.
     *
     * @param date The date to format.
     * @return The formatted UTC timestamp.
     */
    fun formattedUTCTimestamp(date: Date): String {
        val dateFormatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US,
        )
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormatter.format(date)
    }
}
