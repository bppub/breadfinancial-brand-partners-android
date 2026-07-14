//------------------------------------------------------------------------------
//  File:          OfferResponseTest.kt
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfferResponseTest {

    // -------------------------------------------------------------------------
    // Raw Value Mapping
    // -------------------------------------------------------------------------

    @Test
    fun `OfferResponse YES has raw value 'YES'`() {
        assertEquals("YES", OfferResponse.YES.name)
    }

    @Test
    fun `OfferResponse NO has raw value 'NO'`() {
        assertEquals("NO", OfferResponse.NO.name)
    }

    @Test
    fun `OfferResponse NOT_ME has raw value 'NOT_ME'`() {
        assertEquals("NOT_ME", OfferResponse.NOT_ME.name)
    }

    @Test
    fun `OfferResponse ABANDONED has raw value 'ABANDONED'`() {
        assertEquals("ABANDONED", OfferResponse.ABANDONED.name)
    }

    @Test
    fun `OfferResponse PRESCREEN_NO has raw value 'PRESCREEN_NO'`() {
        assertEquals("PRESCREEN_NO", OfferResponse.PRESCREEN_NO.name)
    }

    // -------------------------------------------------------------------------
    // Initialisation from Raw Value
    // -------------------------------------------------------------------------

    @Test
    fun `Init from raw value 'YES' returns YES`() {
        assertEquals(OfferResponse.YES, OfferResponse.fromValue("YES"))
    }

    @Test
    fun `Init from raw value 'NO' returns NO`() {
        assertEquals(OfferResponse.NO, OfferResponse.fromValue("NO"))
    }

    @Test
    fun `Init from raw value 'NOT_ME' returns NOT_ME`() {
        assertEquals(OfferResponse.NOT_ME, OfferResponse.fromValue("NOT_ME"))
    }

    @Test
    fun `Init from raw value 'ABANDONED' returns ABANDONED`() {
        assertEquals(OfferResponse.ABANDONED, OfferResponse.fromValue("ABANDONED"))
    }

    @Test
    fun `Init from raw value 'PRESCREEN_NO' returns PRESCREEN_NO`() {
        assertEquals(OfferResponse.PRESCREEN_NO, OfferResponse.fromValue("PRESCREEN_NO"))
    }

    // -------------------------------------------------------------------------
    // Invalid Raw Values
    // -------------------------------------------------------------------------

    @Test
    fun `Init from unknown raw value returns null`() {
        assertNull(OfferResponse.fromValue("UNKNOWN"))
    }

    @Test
    fun `Init from empty string returns null`() {
        assertNull(OfferResponse.fromValue(""))
    }

    @Test
    fun `Init from lowercase raw value returns null (case-sensitive)`() {
        assertNull(OfferResponse.fromValue("yes"))
        assertNull(OfferResponse.fromValue("no"))
        assertNull(OfferResponse.fromValue("not_me"))
        assertNull(OfferResponse.fromValue("abandoned"))
        assertNull(OfferResponse.fromValue("prescreen_no"))
    }

    // -------------------------------------------------------------------------
    // Equality
    // -------------------------------------------------------------------------

    @Test
    fun `Two identical cases are equal`() {
        assertEquals(OfferResponse.YES, OfferResponse.YES)
        assertEquals(OfferResponse.NO, OfferResponse.NO)
        assertEquals(OfferResponse.NOT_ME, OfferResponse.NOT_ME)
        assertEquals(OfferResponse.ABANDONED, OfferResponse.ABANDONED)
        assertEquals(OfferResponse.PRESCREEN_NO, OfferResponse.PRESCREEN_NO)
    }

    @Test
    fun `Different cases are not equal`() {
        assertNotEquals(OfferResponse.YES, OfferResponse.NO)
        assertNotEquals(OfferResponse.NOT_ME, OfferResponse.ABANDONED)
        assertNotEquals(OfferResponse.PRESCREEN_NO, OfferResponse.YES)
    }

    // -------------------------------------------------------------------------
    // All Cases Coverage
    // -------------------------------------------------------------------------

    @Test
    fun `All five cases exist and have distinct raw values`() {
        val allCases = listOf(
            OfferResponse.YES,
            OfferResponse.NO,
            OfferResponse.NOT_ME,
            OfferResponse.ABANDONED,
            OfferResponse.PRESCREEN_NO
        )
        val rawValues = allCases.map { it.name }
        val uniqueRawValues = rawValues.toSet()
        assertEquals(allCases.size, uniqueRawValues.size)
    }
}

