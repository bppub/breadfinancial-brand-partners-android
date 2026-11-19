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
