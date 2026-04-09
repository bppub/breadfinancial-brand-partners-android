//------------------------------------------------------------------------------
//  File:          PlacementApiExtension.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.extensions

import android.content.Context
import com.breadfinancial.breadpartners.sdk.core.BreadPartnersSDK
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentParser
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentRenderer
import com.breadfinancial.breadpartners.sdk.networking.APIClient
import com.breadfinancial.breadpartners.sdk.networking.APIUrl
import com.breadfinancial.breadpartners.sdk.networking.APIUrlType
import com.breadfinancial.breadpartners.sdk.networking.HTTPMethod
import com.breadfinancial.breadpartners.sdk.networking.Result
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementsResponse
import com.breadfinancial.breadpartners.sdk.networking.requestbuilders.PlacementRequestBuilder
import com.breadfinancial.breadpartners.sdk.utilities.CommonUtils
import com.breadfinancial.breadpartners.sdk.utilities.Constants
import com.breadfinancial.breadpartners.sdk.utilities.Logger

/**
 * Fetches placement data to be displayed as a text view with a clickable button
 * in the brand partner's UI.
 */
fun BreadPartnersSDK.fetchPlacementData(
    merchantConfiguration: MerchantConfiguration,
    placementsConfiguration: PlacementsConfiguration,
    context: Context,
    splitTextAndAction: Boolean = false,
    openPlacementExperience: Boolean = false,
    callback: (BreadPartnerEvent) -> Unit
) {
    val apiUrl = APIUrl(
        urlType = APIUrlType.GeneratePlacements
    ).url
    val builder = PlacementRequestBuilder(
        integrationKey = integrationKey,
        merchantConfiguration,
        placementsConfiguration.placementData
    )
    val placementRequest = builder.build()

    val headers = mapOf("X-Bread-Testing" to "captcha")

    APIClient().request(
        urlString = apiUrl, method = HTTPMethod.POST, body = placementRequest, headers = headers
    ) { result ->
        when (result) {
            is Result.Success -> {
                CommonUtils().decodeJSON(result.data.toString(),
                    PlacementsResponse::class.java,
                    onSuccess = { placementsResponse ->
                        if (openPlacementExperience) {
                            val popupPlacementHTMLContent =
                                placementsResponse.placementContent?.firstOrNull { it.metadata?.templateId?.contains("overlay") == true }
                            try {
                                HTMLContentParser().extractPopupPlacementModel(
                                    popupPlacementHTMLContent?.contentData?.htmlContent ?: ""
                                )?.let { popupPlacementModel ->
                                    Logger.logPopupPlacementModelDetails(popupPlacementModel)
                                    val overlayType =
                                        HTMLContentParser().handleOverlayType(popupPlacementModel.overlayType)
                                    if (overlayType != null) {
                                        HTMLContentRenderer(
                                            integrationKey,
                                            merchantConfiguration,
                                            placementsConfiguration,
                                            brandConfiguration,
                                            splitTextAndAction,
                                            callback
                                        ).createPopupOverlay(
                                            popupPlacementModel, overlayType
                                        )
                                    } else {
                                        callback(
                                            BreadPartnerEvent.SdkError(
                                                error = Exception(Constants.missingPopupPlacementError)
                                            )
                                        )
                                    }
                                }
                            } catch (error: Exception) {
                                callback(
                                    BreadPartnerEvent.SdkError(
                                        error = Exception(error.message)
                                    )
                                )
                            }
                        } else {
                            HTMLContentRenderer(
                                integrationKey,
                                merchantConfiguration,
                                placementsConfiguration,
                                brandConfiguration,
                                splitTextAndAction,
                                callback
                            ).handleTextPlacement(
                                placementsResponse, application
                            )
                        }
                    },
                    onError = { error ->
                        callback(
                            BreadPartnerEvent.SdkError(
                                error = Exception(error.message)
                            )
                        )
                    })
            }

            is Result.Failure -> {
                callback(
                    BreadPartnerEvent.SdkError(
                        error = Exception(result.error)
                    )
                )
            }
        }
    }
}