//------------------------------------------------------------------------------
//  File:          JSONUtilities.kt
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

import com.google.gson.Gson

/**
 * Safely decodes a JSON string into the specified model class.
 */
internal fun <T> decodeJSON(
    jsonString: String,
    classOfType: Class<T>,
    onSuccess: (T) -> Unit,
    onError: (Exception) -> Unit,
) {
    try {
        val gson = Gson()
        val result = gson.fromJson(jsonString, classOfType)
        onSuccess(result)
    } catch (error: Exception) {
        onError(error)
    }
}

