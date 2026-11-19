//------------------------------------------------------------------------------
//  File:          DictionarySerializationExtensions.kt
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

import java.net.URLEncoder

/**
 * Converts this map to a query string with URL-encoded keys and values.
 * Entries with `null` values are skipped; non-string values use `toString()`.
 */
internal fun Map<String, Any?>.toQueryString(): String =
    this
        .filter { it.value != null }
        .map { (key, value) ->
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
            "$encodedKey=$encodedValue"
        }.joinToString("&")

/**
 * Merges source maps into this map, only including non-null values and non-empty strings.
 * Later sources override earlier ones. Non-string values such as `0` or `false` are kept.
 *
 * @param sources One or more source maps to merge from
 * @return This map (same instance) with merged values
 */
internal fun <T : MutableMap<String, Any?>> T.assignDefined(vararg sources: Map<String, Any?>): T {
    for (source in sources) {
        for ((key, value) in source) {
            if (value != null && value != "") {
                this[key] = value
            }
        }
    }
    return this
}
