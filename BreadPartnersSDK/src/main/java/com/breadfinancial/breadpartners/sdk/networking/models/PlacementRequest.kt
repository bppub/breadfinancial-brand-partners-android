//------------------------------------------------------------------------------
//  File:          PlacementRequest.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.breadfinancial.breadpartners.sdk.networking.models


/**
 * Represents a request for fetching placement data based on brand and placement details.
 */
data class PlacementRequest(
    val placements: List<PlacementRequestBody>? = null, val brandId: String? = null
)

/**
 * Represents an individual placement request with an ID and context.
 */
data class PlacementRequestBody(
    val id: String? = null, val context: ContextRequestBody? = null
)

/**
 * Holds additional context information for a placement request.
 */
data class ContextRequestBody(
    val SDK_TID: String? = null,
    val ENV: String? = null,
    val RTPS_ID: String? = null,
    val BUYER_ID: String? = null,
    val PREQUAL_ID: String? = null,
    val PREQUAL_CREDIT_LIMIT: String? = null,
    val LOCATION: String? = null,
    val PRICE: Long? = null,
    val EXISTING_CH: Boolean? = null,
    val CARDHOLDER_TIER: String? = null,
    val STORE_NUMBER: String? = null,
    val LOYALTY_ID: String? = null,
    val OVERRIDE_KEY: String? = null,
    val CLIENT_VAR_1: String? = null,
    val CLIENT_VAR_2: String? = null,
    val CLIENT_VAR_3: String? = null,
    val CLIENT_VAR_4: String? = null,
    val DEPARTMENT_ID: String? = null,
    val channel: String? = null,
    val subchannel: String? = null,
    val CMP: String? = null,
    val ALLOW_CHECKOUT: Boolean? = null,
    val UPQ_CHECKOUT_PARAMS: String? = null,
    val UPQ_PARAMS: String? = null,
    val embeddedUrl: String? = null
)
