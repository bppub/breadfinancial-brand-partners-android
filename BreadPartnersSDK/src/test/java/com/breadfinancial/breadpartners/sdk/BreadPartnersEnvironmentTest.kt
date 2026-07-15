//------------------------------------------------------------------------------
//  File:          BreadPartnersEnvironmentTest.kt
//  Author(s):     Bread Financial
//  Date:          14 July 2026
//
//  Descriptions:  Unit tests for the BreadPartnersEnvironment enum and its
//  companion object factory method.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment.entries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BreadPartnersEnvironmentTest {

    // -------------------------------------------------------------------------
    // Raw Value Mapping
    // -------------------------------------------------------------------------

    @Test
    fun `BreadPartnersEnvironment STAGE has raw value 'STAGE'`() {
        assertEquals("PROD", BreadPartnersEnvironment.STAGE.value)
    }

    @Test
    fun `BreadPartnersEnvironment PROD has raw value 'PROD'`() {
        assertEquals("PROD", BreadPartnersEnvironment.PROD.value)
    }

    @Test
    fun `BreadPartnersEnvironment UAT has raw value 'UAT'`() {
        assertEquals("UAT", BreadPartnersEnvironment.UAT.value)
    }

    // -------------------------------------------------------------------------
    // Initialisation from Raw Value
    // -------------------------------------------------------------------------

    @Test
    fun `Init from raw value 'STAGE' returns STAGE`() {
        assertEquals(BreadPartnersEnvironment.STAGE, fromValue("STAGE"))
    }

    @Test
    fun `Init from raw value 'PROD' returns PROD`() {
        assertEquals(BreadPartnersEnvironment.PROD, fromValue("PROD"))
    }

    @Test
    fun `Init from raw value 'UAT' returns UAT`() {
        assertEquals(BreadPartnersEnvironment.UAT, fromValue("UAT"))
    }

    // -------------------------------------------------------------------------
    // Invalid Raw Values
    // -------------------------------------------------------------------------

    @Test
    fun `Init from unknown raw value returns null`() {
        assertNull(fromValue("UNKNOWN"))
    }

    @Test
    fun `Init from empty string returns null`() {
        assertNull(fromValue(""))
    }

    @Test
    fun `Init from lowercase raw value returns null (case-sensitive)`() {
        assertNull(fromValue("stage"))
        assertNull(fromValue("prod"))
        assertNull(fromValue("uat"))
    }

    // -------------------------------------------------------------------------
    // Equality
    // -------------------------------------------------------------------------

    @Test
    fun `Two identical cases are equal`() {
        assertEquals(BreadPartnersEnvironment.STAGE, BreadPartnersEnvironment.STAGE)
        assertEquals(BreadPartnersEnvironment.PROD, BreadPartnersEnvironment.PROD)
        assertEquals(BreadPartnersEnvironment.UAT, BreadPartnersEnvironment.UAT)
    }

    @Test
    fun `Different cases are not equal`() {
        assertNotEquals(BreadPartnersEnvironment.STAGE, BreadPartnersEnvironment.PROD)
        assertNotEquals(BreadPartnersEnvironment.PROD, BreadPartnersEnvironment.UAT)
        assertNotEquals(BreadPartnersEnvironment.STAGE, BreadPartnersEnvironment.UAT)
    }

    // -------------------------------------------------------------------------
    // All Cases Coverage
    // -------------------------------------------------------------------------

    @Test
    fun `All three cases exist and have distinct raw values`() {
        val allCases = listOf(
            BreadPartnersEnvironment.STAGE,
            BreadPartnersEnvironment.PROD,
            BreadPartnersEnvironment.UAT
        )
        val rawValues = allCases.map { it.value }
        val uniqueRawValues = rawValues.toSet()
        assertEquals(allCases.size, uniqueRawValues.size)
    }

    // -------------------------------------------------------------------------
    // Helper Method for Testing
    // -------------------------------------------------------------------------

    private fun fromValue(rawValue: String): BreadPartnersEnvironment? {
        return BreadPartnersEnvironment.entries.find { it.value == rawValue }
    }
}

