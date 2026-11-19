//------------------------------------------------------------------------------
//  File:          PlacementResponse.kt
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
 * Represents the response model containing all placement-related data.
 */
data class PlacementsResponse(
    val placements: List<PlacementsModel>? = null,
    val placementContent: List<PlacementContentModel>? = null,
)

/**
 * Represents an individual placement for rendering.
 */
data class PlacementsModel(
    val id: String? = null,
    val content: PlacementContentReferenceModel? = null,
    val renderContext: RenderContextModel? = null,
)

/**
 * Represents a reference to placement content via ID.
 */
data class PlacementContentReferenceModel(
    val contentId: String? = null,
)

/**
 * Contains contextual information for rendering a placement.
 */
data class RenderContextModel(
    val LOCATION: String? = null,
    val subchannel: String? = null,
    val RTPS_ID: String? = null,
    val PREQUAL_ID: String? = null,
    val PRICE: Int? = null,
    val DATETIME: String? = null,
    val SDK_TID: String? = null,
    val BUYER_ID: String? = null,
    val channel: String? = null,
    val PREQUAL_CREDIT_LIMIT: String? = null,
    val ENV: String? = null,
    val ALLOW_CHECKOUT: Boolean? = null,
    val embeddedUrl: String? = null,
)

/**
 * Represents the content model for a placement.
 */
data class PlacementContentModel(
    val id: String? = null,
    val contentType: String? = null,
    val contentData: ContentDataModel? = null,
    val metadata: MetadataModel? = null,
)

/**
 * Contains the HTML content used to render the placement.
 */
data class ContentDataModel(
    val htmlContent: String? = null,
)

/**
 * Represents additional metadata about a placement.
 */
data class MetadataModel(
    val placementId: String? = null,
    val productType: String? = null,
    val messageId: String? = null,
    val templateId: String? = null,
)
