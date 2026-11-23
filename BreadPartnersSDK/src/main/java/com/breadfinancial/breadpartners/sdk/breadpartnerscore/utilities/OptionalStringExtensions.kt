//------------------------------------------------------------------------------
//  File:          OptionalStringExtensions.kt
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

/**
 * Extension function on nullable String that filters out null, empty, and
 * whitespace-only strings, returning the original string if it contains
 * non-whitespace content.
 */
internal fun String?.takeIfNotEmpty(): String? {
    return this?.trim()?.takeIf { it.isNotEmpty() }
}
