//------------------------------------------------------------------------------
//  File:          DictionarySerializationExtensionsTest.kt
//  Author(s):     Bread Financial
//  Date:          31 December 2026
//
//  Descriptions:  This file is part of the BreadPartners SDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.breadpartnerscore.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DictionarySerializationExtensionsTest {
    @Test
    fun assignDefinedIncludesOnlyDefinedAndNonemptyValues() {
        val values = mutableMapOf<String, Any?>("existing" to "kept")

        values.assignDefined(
            mapOf(
                "string" to "value",
                "empty" to "",
                "missing" to null,
                "number" to 5,
                "boolean" to true,
            ),
        )

        assertEquals("kept", values["existing"])
        assertEquals("value", values["string"])
        assertFalse(values.containsKey("empty"))
        assertFalse(values.containsKey("missing"))
        assertEquals(5, values["number"])
        assertEquals(true, values["boolean"])
    }
}
