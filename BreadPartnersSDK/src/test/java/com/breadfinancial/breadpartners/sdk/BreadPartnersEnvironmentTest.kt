//------------------------------------------------------------------------------
//  File:          BreadPartnersEnvironmentTest.kt
//  Author(s):     Bread Financial
//  Date:          14 July 2026
//
//  Descriptions:  Unit tests for the BreadPartnersEnvironment enum, covering
//  all enum constants, their values, ordinals, and overall integrity.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BreadPartnersEnvironmentTest {

    // -------------------------------------------------------------------------
    // Enum value property verification
    // -------------------------------------------------------------------------

    @Test
    fun `STAGE enum constant has correct value`() {
        assertEquals("STAGE", BreadPartnersEnvironment.STAGE.value)
    }

    @Test
    fun `PROD enum constant has correct value`() {
        assertEquals("PROD", BreadPartnersEnvironment.PROD.value)
    }

    @Test
    fun `UAT enum constant has correct value`() {
        assertEquals("UAT", BreadPartnersEnvironment.UAT.value)
    }

    // -------------------------------------------------------------------------
    // Enum entries completeness
    // -------------------------------------------------------------------------

    @Test
    fun `entries contains exactly three values`() {
        assertEquals(3, BreadPartnersEnvironment.entries.size)
    }

    @Test
    fun `entries contains all expected enum constants`() {
        val expected = setOf(
            BreadPartnersEnvironment.STAGE,
            BreadPartnersEnvironment.PROD,
            BreadPartnersEnvironment.UAT
        )
        assertEquals(expected, BreadPartnersEnvironment.entries.toSet())
    }

    @Test
    fun `all enum entries have non-null values`() {
        BreadPartnersEnvironment.entries.forEach { constant ->
            assertNotNull("${constant.name} should have a non-null value", constant.value)
        }
    }

    // -------------------------------------------------------------------------
    // Name property verification
    // -------------------------------------------------------------------------

    @Test
    fun `STAGE name property matches expected string`() {
        assertEquals("STAGE", BreadPartnersEnvironment.STAGE.name)
    }

    @Test
    fun `PROD name property matches expected string`() {
        assertEquals("PROD", BreadPartnersEnvironment.PROD.name)
    }

    @Test
    fun `UAT name property matches expected string`() {
        assertEquals("UAT", BreadPartnersEnvironment.UAT.name)
    }

    @Test
    fun `name property matches value property for each constant`() {
        BreadPartnersEnvironment.entries.forEach { constant ->
            assertEquals(
                "name and value should match for ${constant.name}",
                constant.name,
                constant.value
            )
        }
    }

    // -------------------------------------------------------------------------
    // Ordinal / declaration order verification
    // -------------------------------------------------------------------------

    @Test
    fun `STAGE is declared first with ordinal 0`() {
        assertEquals(0, BreadPartnersEnvironment.STAGE.ordinal)
    }

    @Test
    fun `PROD is declared second with ordinal 1`() {
        assertEquals(1, BreadPartnersEnvironment.PROD.ordinal)
    }

    @Test
    fun `UAT is declared third with ordinal 2`() {
        assertEquals(2, BreadPartnersEnvironment.UAT.ordinal)
    }

    @Test
    fun `enum constants are declared in expected order`() {
        val expectedOrder = listOf(
            BreadPartnersEnvironment.STAGE,
            BreadPartnersEnvironment.PROD,
            BreadPartnersEnvironment.UAT
        )
        assertEquals(expectedOrder, BreadPartnersEnvironment.entries)
    }

    // -------------------------------------------------------------------------
    // Uniqueness and consistency verification
    // -------------------------------------------------------------------------

    @Test
    fun `all enum values are unique`() {
        val values = BreadPartnersEnvironment.entries.map { it.value }
        assertEquals(values.size, values.distinct().size)
    }

    @Test
    fun `all enum names are unique`() {
        val names = BreadPartnersEnvironment.entries.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `all enum ordinals are unique and sequential`() {
        val ordinals = BreadPartnersEnvironment.entries.map { it.ordinal }
        assertEquals(listOf(0, 1, 2), ordinals)
    }

    // -------------------------------------------------------------------------
    // Enum constants accessor verification
    // -------------------------------------------------------------------------

    @Test
    fun `can access STAGE constant via companion object pattern`() {
        assertNotNull(BreadPartnersEnvironment.STAGE)
        assertEquals("STAGE", BreadPartnersEnvironment.STAGE.value)
    }

    @Test
    fun `can access PROD constant via companion object pattern`() {
        assertNotNull(BreadPartnersEnvironment.PROD)
        assertEquals("PROD", BreadPartnersEnvironment.PROD.value)
    }

    @Test
    fun `can access UAT constant via companion object pattern`() {
        assertNotNull(BreadPartnersEnvironment.UAT)
        assertEquals("UAT", BreadPartnersEnvironment.UAT.value)
    }

    // -------------------------------------------------------------------------
    // Enum.valueOf verification
    // -------------------------------------------------------------------------

    @Test
    fun `valueOf returns correct constant for STAGE`() {
        val result = BreadPartnersEnvironment.valueOf("STAGE")
        assertEquals(BreadPartnersEnvironment.STAGE, result)
    }

    @Test
    fun `valueOf returns correct constant for PROD`() {
        val result = BreadPartnersEnvironment.valueOf("PROD")
        assertEquals(BreadPartnersEnvironment.PROD, result)
    }

    @Test
    fun `valueOf returns correct constant for UAT`() {
        val result = BreadPartnersEnvironment.valueOf("UAT")
        assertEquals(BreadPartnersEnvironment.UAT, result)
    }

    @Test
    fun `valueOf is case-sensitive and throws IllegalArgumentException for lowercase`() {
        try {
            BreadPartnersEnvironment.valueOf("stage")
            throw AssertionError("Expected IllegalArgumentException for lowercase 'stage'")
        } catch (_: IllegalArgumentException) {
            // Expected behavior
        }
    }

    @Test
    fun `valueOf throws IllegalArgumentException for unknown constant`() {
        try {
            BreadPartnersEnvironment.valueOf("UNKNOWN")
            throw AssertionError("Expected IllegalArgumentException for unknown constant")
        } catch (_: IllegalArgumentException) {
            // Expected behavior
        }
    }

    // -------------------------------------------------------------------------
    // Type and property verification
    // -------------------------------------------------------------------------

    @Test
    fun `enum value property is accessible for all constants`() {
        BreadPartnersEnvironment.entries.forEach { constant ->
            val value = constant.value
            assertNotNull("Value should not be null for ${constant.name}", value)
            assert(value.isNotEmpty())
        }
    }

    // -------------------------------------------------------------------------
    // Enum.entries vs valueOf consistency
    // -------------------------------------------------------------------------

    @Test
    fun `all entries can be found via valueOf using their name`() {
        BreadPartnersEnvironment.entries.forEach { constant ->
            val result = BreadPartnersEnvironment.valueOf(constant.name)
            assertEquals(constant, result)
        }
    }

    @Test
    fun `all entries maintain their value after retrieval via valueOf`() {
        BreadPartnersEnvironment.entries.forEach { constant ->
            val retrieved = BreadPartnersEnvironment.valueOf(constant.name)
            assertEquals(constant.value, retrieved.value)
        }
    }
}

