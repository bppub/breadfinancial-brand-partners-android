//------------------------------------------------------------------------------
//  File:          RTPSResponse.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.networking.models

/**
 * Enum representing the results from a RTPS check.
 */
enum class PrescreenResult {
    // Account found from virtual lookup
    ACCOUNT_FOUND,

    // Has been pre-approved
    APPROVED,
    NO_HIT,

    // Not pre-approved but should/could apply
    MAKE_OFFER,
    ACKNOWLEDGE
}

/**
 * Mapping of return codes from the API to corresponding PrescreenResult.
 */
val prescreenResultMap: Map<String, PrescreenResult> = mapOf(
    "0" to PrescreenResult.ACCOUNT_FOUND,
    "01" to PrescreenResult.APPROVED,
    "10" to PrescreenResult.NO_HIT,
    "11" to PrescreenResult.MAKE_OFFER,
    "12" to PrescreenResult.ACKNOWLEDGE
)

/**
 * Returns the PrescreenResult associated with given code.
 */
fun getPrescreenResult(apiResponse: String?): PrescreenResult {
    return if (apiResponse.isNullOrBlank()) {
        PrescreenResult.NO_HIT
    } else {
        prescreenResultMap[apiResponse] ?: PrescreenResult.NO_HIT
    }
}

/**
 * Data class representing the response from an RTPS call.
 */
data class RTPSResponse(
    val returnCode: String?,
    val prescreenId: Long?,
    val firstName: String?,
    val middleInitial: String?,
    val lastName: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val state: String?,
    val zip: String?,
    val mobilePhone: String?,
    val emailAddress: String?,
    val alternativePhone: String?,
    val cardType: String?,
    val isExpired: Boolean?,
    val hasExistingAccount: Boolean?,
    val errorMessage: String?,
    val errorCode: Int?,
)