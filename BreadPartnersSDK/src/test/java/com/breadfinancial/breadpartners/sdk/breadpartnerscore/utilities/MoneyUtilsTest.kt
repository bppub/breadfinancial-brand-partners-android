/*
 * File:          MoneyUtilsTest.kt
 * Author(s):     Bread Financial
 * Date:          31 December 2026
 *
 * Descriptions:  This file is part of the BreadPartners SDK for Android,
 * providing UI components and functionalities to integrate Bread Financial
 * services into partner applications.
 *
 * © 2026 Bread Financial
 */

package com.breadfinancial.breadpartners.sdk.breadpartnerscore.utilities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyUtilsTest {
    @Test
    fun convertsCentsToDollars() {
        val cases = listOf(
            Long.MIN_VALUE to Long.MIN_VALUE.toDouble() / 100.0,
            -12345L to -123.45,
            0L to 0.0,
            1L to 0.01,
            12345L to 123.45,
            Long.MAX_VALUE to Long.MAX_VALUE.toDouble() / 100.0,
        )

        for ((cents, expectedDollars) in cases) {
            assertEquals(
                "cents=$cents",
                expectedDollars,
                MoneyUtils.fromMoneyToDollars(cents)!!,
                0.0,
            )
        }
    }

    @Test
    fun returnsNullForMissingMoneyValue() {
        assertNull(MoneyUtils.fromMoneyToDollars(null))
    }
}
