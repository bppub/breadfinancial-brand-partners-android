//------------------------------------------------------------------------------
//  File:          OptionalStringExtensionsTests.kt
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
import org.junit.Assert.assertNull
import org.junit.Test

class OptionalStringExtensionsTests {
    @Test
    fun filtersMissingOrBlankStrings() {
        assertNull(null.takeIfNotEmpty())
        assertNull("".takeIfNotEmpty())
        assertNull(" ".takeIfNotEmpty())
        assertNull("\n\t".takeIfNotEmpty())
    }

    @Test
    fun preservesNonblankString() {
        assertEquals("value", "value".takeIfNotEmpty())
        assertEquals("value", "  value  ".takeIfNotEmpty())
        assertEquals("value", "value\n".takeIfNotEmpty())
    }
}
