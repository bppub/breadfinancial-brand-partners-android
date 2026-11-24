//------------------------------------------------------------------------------
//  File:          JSONUtilitiesTest.kt
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
import org.junit.Assert.assertTrue
import org.junit.Test

class JSONUtilitiesTest {
    private data class Sample(
        val name: String?,
        val count: Int?,
    )

    @Test
    fun decodesValidJSON() {
        var decoded: Sample? = null
        var error: Exception? = null

        decodeJSON(
            jsonString = """{"name":"bread","count":3}""",
            classOfType = Sample::class.java,
            onSuccess = { decoded = it },
            onError = { error = it },
        )

        assertNull(error)
        assertEquals("bread", decoded?.name)
        assertEquals(3, decoded?.count)
    }

    @Test
    fun callsOnErrorForMalformedJSON() {
        var successCalled = false
        var error: Exception? = null

        decodeJSON(
            jsonString = """{"name":""",
            classOfType = Sample::class.java,
            onSuccess = { successCalled = true },
            onError = { error = it },
        )

        assertTrue(!successCalled)
        assertTrue(error != null)
    }

    @Test
    fun callsOnErrorForTypeMismatch() {
        var successCalled = false
        var error: Exception? = null

        decodeJSON(
            jsonString = """{"name":"bread","count":"abc"}""",
            classOfType = Sample::class.java,
            onSuccess = { successCalled = true },
            onError = { error = it },
        )

        assertTrue(!successCalled)
        assertTrue(error != null)
    }

    @Test
    fun decodesMissingFieldsAsNull() {
        var decoded: Sample? = null
        var error: Exception? = null

        decodeJSON(
            jsonString = "{}",
            classOfType = Sample::class.java,
            onSuccess = { decoded = it },
            onError = { error = it },
        )

        assertNull(error)
        assertNull(decoded?.name)
        assertNull(decoded?.count)
    }
}

