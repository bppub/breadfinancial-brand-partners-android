/*
 * File:          MoneyUtils.kt
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

/**
 * Platform-independent helpers for money value conversions.
 */
internal object MoneyUtils {
    /**
     * Converts Money value to dollars (divides by 100).
     *
     * @param moneyValue Double value in cents
     * @return Double value in dollars, or null if input is null
     */
    fun fromMoneyToDollars(moneyValue: Long?): Double? {
        return if (moneyValue != null) (moneyValue.toDouble() / 100) else null
    }
}

