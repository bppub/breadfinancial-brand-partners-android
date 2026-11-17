//------------------------------------------------------------------------------
//  File:          AnalyticsManager.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.analytics

import com.breadfinancial.breadpartners.sdk.analytics.models.Analytics
import com.breadfinancial.breadpartners.sdk.networking.APIClient
import com.breadfinancial.breadpartners.sdk.networking.APIUrl
import com.breadfinancial.breadpartners.sdk.networking.APIUrlType
import com.breadfinancial.breadpartners.sdk.networking.HTTPMethod
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementsResponse
import com.breadfinancial.breadpartners.sdk.utilities.CommonUtils
import com.breadfinancial.breadpartners.sdk.utilities.Constants

/**
 * `AnalyticsManager` logs user interactions with placements in the app.
 * It tracks two events:
 * 1. **Click Placement**: When the user clicks on the placement.
 * 2. **View Placement**: When the user sees or interacts with the placement without clicking.
 */
class AnalyticsManager {
    private var apiKey: String = ""

    fun setApiKey(newApiKey: String) {
        apiKey = newApiKey
    }

    // Helper function to send analytics payload
    private fun sendAnalyticsPayload(
        apiUrl: String,
        payload: Analytics.Payload,
    ) {
        val headers = mapOf(
            Constants.headerAuthorityKey to Constants.headerAuthorityValue,
            Constants.headerAcceptKey to Constants.headerAcceptValue,
            Constants.headerAcceptEncodingKey to Constants.headerAcceptEncodingValue,
            Constants.headerAcceptLanguageKey to Constants.headerAcceptLanguageValue,
            Constants.headerAccessControlRequestHeadersKey to Constants.headerAccessControlRequestHeadersValue,
            Constants.headerAccessControlRequestMethodKey to Constants.headerAccessControlRequestMethodValue,
        )
        APIClient().request(
            urlString = apiUrl,
            method = HTTPMethod.OPTIONS,
            body = payload,
            headers = headers,
        ) {}
    }

    // ToDo: Setup analytics payload
    // Helper function to create analytics payload
    private fun createAnalyticsPlacementPayload(
        name: String,
        placementResponse: PlacementsResponse,
    ): Analytics.Payload {
        val timestamp = CommonUtils().getCurrentTimestamp()

        return Analytics.Payload(
            name = name,
            props = Analytics.Props(
                eventProperties = Analytics.EventProperties(
                    placement = Analytics.Placement(
                        id = placementResponse.placements?.first()?.id,
                        placementContentId = placementResponse.placementContent?.first()?.id,
                        overlayContentId = placementResponse.placementContent?.first()?.id,
                    ),
                    placementContent = Analytics.PlacementContent(
                        id = placementResponse.placementContent?.first()?.id,
                        contentType = placementResponse.placementContent?.first()?.contentType,
                        metadata = placementResponse.placementContent?.first()?.metadata,
                    ),
                    metadata = mapOf(
                        "location" to placementResponse.placements
                            ?.first()
                            ?.renderContext
                            ?.LOCATION,
                    ),
                    actionTarget = null,
                ),
                userProperties = emptyMap(),
            ),
            context = Analytics.Context(
                timestamp = timestamp,
                apiKey = apiKey,
                browserCtx = Analytics.BrowserCtx(
                    library = Analytics.Library(
                        name = "bread-partners-sdk-android",
                        version = "0.0.1",
                    ),
                    userAgent = CommonUtils().getUserAgent(),
                    page = Analytics.Page(
                        path = "ToDO:Android Screen Name",
                        url = "ToDO",
                    ),
                ),
                trackingInfo = Analytics.TrackingInfo(
                    userTrackingId = placementResponse.placements
                        ?.first()
                        ?.renderContext
                        ?.SDK_TID,
                    sessionTrackingId = "ToDO",
                ),
            ),
        )
    }

    // Centralized function to send placement analytics
    private fun sendPlacementAnalytics(
        name: String,
        placementResponse: PlacementsResponse,
        apiUrlType: APIUrlType,
    ) {
        val payload = createAnalyticsPlacementPayload(name, placementResponse)
        val apiUrl = APIUrl(urlType = apiUrlType).url
        sendAnalyticsPayload(apiUrl, payload)
    }

    // Public function to send view placement analytics
    fun sendViewPlacement(placementResponse: PlacementsResponse) {
        sendPlacementAnalytics("view-placement", placementResponse, APIUrlType.ViewPlacement)
    }

    // Public function to send click placement analytics
    fun sendClickPlacement(placementResponse: PlacementsResponse) {
        sendPlacementAnalytics("click-placement", placementResponse, APIUrlType.ClickPlacement)
    }
}
