//------------------------------------------------------------------------------
//  File:          OfferResponse.kt
//  Author(s):     Bread Financial
//  Date:          14 July 2026
//
//  Descriptions:  Unit tests for the OfferResponse enum and its companion
//  object factory method.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk

import com.breadfinancial.breadpartners.sdk.core.models.OfferResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OfferResponseTest {

    // -------------------------------------------------------------------------
    // fromValue — valid inputs
    // -------------------------------------------------------------------------

    @Test
    fun `fromValue returns YES for "YES"`() {
        assertEquals(OfferResponse.YES, OfferResponse.fromValue("YES"))
    }

    @Test
    fun `fromValue returns NO for "NO"`() {
        assertEquals(OfferResponse.NO, OfferResponse.fromValue("NO"))
    }

    @Test
    fun `fromValue returns NOT_ME for "NOT_ME"`() {
        assertEquals(OfferResponse.NOT_ME, OfferResponse.fromValue("NOT_ME"))
    }

    @Test
    fun `fromValue returns ABANDONED for "ABANDONED"`() {
        assertEquals(OfferResponse.ABANDONED, OfferResponse.fromValue("ABANDONED"))
    }

    @Test
    fun `fromValue returns PRESCREEN_NO for "PRESCREEN_NO"`() {
        assertEquals(OfferResponse.PRESCREEN_NO, OfferResponse.fromValue("PRESCREEN_NO"))
    }

    // -------------------------------------------------------------------------
    // fromValue — invalid / edge-case inputs
    // -------------------------------------------------------------------------

    @Test
    fun `fromValue returns null for unknown string`() {
        assertNull(OfferResponse.fromValue("UNKNOWN"))
    }

    @Test
    fun `fromValue returns null for empty string`() {
        assertNull(OfferResponse.fromValue(""))
    }

    @Test
    fun `fromValue is case-sensitive and returns null for lowercase`() {
        assertNull(OfferResponse.fromValue("yes"))
        assertNull(OfferResponse.fromValue("no"))
        assertNull(OfferResponse.fromValue("not_me"))
        assertNull(OfferResponse.fromValue("abandoned"))
        assertNull(OfferResponse.fromValue("prescreen_no"))
    }

    @Test
    fun `fromValue is case-sensitive and returns null for mixed case`() {
        assertNull(OfferResponse.fromValue("Yes"))
        assertNull(OfferResponse.fromValue("No"))
        assertNull(OfferResponse.fromValue("Abandoned"))
    }

    @Test
    fun `fromValue returns null for whitespace-padded value`() {
        assertNull(OfferResponse.fromValue(" YES"))
        assertNull(OfferResponse.fromValue("YES "))
        assertNull(OfferResponse.fromValue(" YES "))
    }

    // -------------------------------------------------------------------------
    // Enum entries completeness
    // -------------------------------------------------------------------------

    @Test
    fun `entries contains exactly five values`() {
        assertEquals(5, OfferResponse.entries.size)
    }

    @Test
    fun `entries contains all expected enum constants`() {
        val expected = setOf(
            OfferResponse.YES,
            OfferResponse.NO,
            OfferResponse.NOT_ME,
            OfferResponse.ABANDONED,
            OfferResponse.PRESCREEN_NO
        )
        assertEquals(expected, OfferResponse.entries.toSet())
    }

    // -------------------------------------------------------------------------
    // name property round-trip through fromValue
    // -------------------------------------------------------------------------

    @Test
    fun `fromValue round-trips correctly for every enum constant`() {
        OfferResponse.entries.forEach { constant ->
            val result = OfferResponse.fromValue(constant.name)
            assertNotNull("fromValue(\"${constant.name}\") should not be null", result)
            assertEquals(constant, result)
        }
    }

    // -------------------------------------------------------------------------
    // Ordinal / declaration order
    // -------------------------------------------------------------------------

    @Test
    fun `enum constants are declared in expected order`() {
        assertEquals(0, OfferResponse.YES.ordinal)
        assertEquals(1, OfferResponse.NO.ordinal)
        assertEquals(2, OfferResponse.NOT_ME.ordinal)
        assertEquals(3, OfferResponse.ABANDONED.ordinal)
        assertEquals(4, OfferResponse.PRESCREEN_NO.ordinal)
    }

    // -------------------------------------------------------------------------
    // name property
    // -------------------------------------------------------------------------

    @Test
    fun `name property matches expected string for each constant`() {
        assertEquals("YES", OfferResponse.YES.name)
        assertEquals("NO", OfferResponse.NO.name)
        assertEquals("NOT_ME", OfferResponse.NOT_ME.name)
        assertEquals("ABANDONED", OfferResponse.ABANDONED.name)
        assertEquals("PRESCREEN_NO", OfferResponse.PRESCREEN_NO.name)
    }
}