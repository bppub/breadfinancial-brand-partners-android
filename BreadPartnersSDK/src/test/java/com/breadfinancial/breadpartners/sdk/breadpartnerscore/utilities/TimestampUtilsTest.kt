//------------------------------------------------------------------------------
//  File:          TimestampUtilsTest.kt
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

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TimestampUtilsTest {
    @Test
    fun formatsDateAsUTCTimestampWithMilliseconds() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:00"), Locale.US)
        calendar.clear()
        calendar.set(2026, Calendar.SEPTEMBER, 2, 10, 15, 30)
        calendar.set(Calendar.MILLISECOND, 456)

        assertEquals(
            "2026-09-02T05:15:30.456Z",
            TimestampUtils.formattedUTCTimestamp(calendar.time),
        )
    }
}
