//------------------------------------------------------------------------------
//  File:          RTPSApiExtension.kt
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
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.htmlhandling.ChallengeDialog
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentParser
import com.breadfinancial.breadpartners.sdk.htmlhandling.HTMLContentRenderer
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementOverlayType
import com.breadfinancial.breadpartners.sdk.networking.APIClient
import com.breadfinancial.breadpartners.sdk.networking.APIUrl
import com.breadfinancial.breadpartners.sdk.networking.APIUrlType
import com.breadfinancial.breadpartners.sdk.networking.HTTPMethod
import com.breadfinancial.breadpartners.sdk.networking.Result
import com.breadfinancial.breadpartners.sdk.networking.models.ContextRequestBody
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementRequest
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementRequestBody
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementsResponse
import com.breadfinancial.breadpartners.sdk.networking.models.PrescreenResult
import com.breadfinancial.breadpartners.sdk.networking.models.RTPSResponse
import com.breadfinancial.breadpartners.sdk.networking.models.getPrescreenResult
import com.breadfinancial.breadpartners.sdk.networking.requestbuilders.RTPSRequestBuilder
import com.breadfinancial.breadpartners.sdk.security.RecaptchaManager
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.takeIfNotEmpty
import com.breadfinancial.breadpartners.sdk.utilities.CommonUtils
import com.breadfinancial.breadpartners.sdk.utilities.Constants
import com.breadfinancial.breadpartners.sdk.utilities.Constants.prescreenRequiredFieldsError
import com.breadfinancial.breadpartners.sdk.utilities.Logger
import com.google.android.recaptcha.RecaptchaAction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Performs a bot behavior check using the Recaptcha v3 SDK
 * to protect against malicious attacks.
 */
private suspend fun BreadPartnersSDK.executeSecurityCheck(
    merchantConfiguration: MerchantConfiguration,
): String {
    var reCaptchaToken = ""

    val siteKey = brandConfiguration?.config?.getRecaptchaKey(
        merchantConfiguration.env ?: BreadPartnersEnvironment.PROD
    )
    RecaptchaManager.init(Logger).executeReCaptcha(
        context = application,
        siteKey = siteKey ?: "",
        action = RecaptchaAction.custom(customAction = "checkout")
    ) { result ->
        result.onSuccess {
            reCaptchaToken = it
        }
        result.onFailure {
            return@executeReCaptcha
        }
    }
    return reCaptchaToken
}

/**
 * Once the Recaptcha token is obtained, makes the pre-screen lookup API call.
 *
 * - If `prescreenId` was previously saved by the brand partner when calling the pre-screen endpoint,
 *   triggers `virtualLookup`.
 * - Otherwise, calls the pre-screen endpoint to fetch the `prescreenId`.
 * - Both endpoints require user details to build the request payload.
 */
internal fun BreadPartnersSDK.rtpsCall(
    merchantConfiguration: MerchantConfiguration,
    placementsConfiguration: PlacementsConfiguration,
    viewContext: Context,
    callback: (BreadPartnerEvent) -> Unit,
    cookies: String? = null,
) {
    // Check for Batch Prescreen Flow when prescreen id has to be entered by user.
    if (placementsConfiguration.rtpsData?.customerAcceptedOffer == true) {
        return fetchRTPSData(
            merchantConfiguration = merchantConfiguration,
            placementsConfiguration = placementsConfiguration,
            viewContext = viewContext,
            callback = callback,
        )
    }

    // Check if it is a regular RTPS flow or Batch Prescreen (prescreenId is known).
    val isPrescreen = placementsConfiguration.rtpsData?.prescreenId == null

    // Validate required fields for prescreen requests
    if (isPrescreen) {
        val buyer = merchantConfiguration.buyer
        val billingAddress = buyer?.billingAddress

        val givenName = buyer?.givenName
        val familyName = buyer?.familyName
        val address1 = billingAddress?.address1
        val country = billingAddress?.country
        val locality = billingAddress?.locality
        val region = billingAddress?.region
        val postalCode = billingAddress?.postalCode

        // Check if firstname, lastname, and complete address are provided
        if (givenName.isNullOrEmpty() || familyName.isNullOrEmpty() ||
            address1.isNullOrEmpty() || country.isNullOrEmpty() ||
            locality.isNullOrEmpty() || region.isNullOrEmpty() ||
            postalCode.isNullOrEmpty()
        ) {
            Logger.printLog("buyer details or billing address details are missing for pre-screen API call. Please ensure all required fields are provided.")

            return callback(
                BreadPartnerEvent.SdkError(
                    error = Exception(
                        prescreenRequiredFieldsError
                    )
                )
            )

        }
    }

    // Only obtain reCaptcha token for prescreen requests
    GlobalScope.launch {
        val reCaptchaToken = if (isPrescreen) {
            try {
                executeSecurityCheck(merchantConfiguration = merchantConfiguration)
            } catch (e: Exception) {
                callback(
                    BreadPartnerEvent.SdkError(error = e)
                )
                return@launch
            }
        } else {
            ""
        }


        val apiUrl = APIUrl(
            urlType = if (isPrescreen) APIUrlType.PreScreen else APIUrlType.VirtualLookup
        ).url
        val rtpsRequestBuilder = RTPSRequestBuilder(
            merchantConfiguration,
            placementsConfiguration.rtpsData!!,
            reCaptchaToken = reCaptchaToken
        )
        val rtpsRequest = rtpsRequestBuilder.build()
        val headers = mapOf(
            Constants.headerClientKey to integrationKey,
            Constants.headerRequestedWithKey to Constants.headerRequestedWithValue
        )
        APIClient().request(
            urlString = apiUrl,
            method = HTTPMethod.POST,
            body = rtpsRequest,
            headers = headers,
            cookies = cookies
        ) { result ->
            when (result) {
                is Result.Success -> {
                    CommonUtils().decodeJSON(
                        result.data.toString(),
                        RTPSResponse::class.java,
                        onSuccess = { rtpsResponse ->
                            val returnResultType = rtpsResponse.returnCode
                            val prescreenResult = getPrescreenResult(returnResultType)

                            // Map response data back to configurations.
                            placementsConfiguration.rtpsData.prescreenId = rtpsResponse.prescreenId
                            placementsConfiguration.rtpsData.cardType = rtpsResponse.cardType

                            Logger.printLog("PreScreenID:Result: $prescreenResult")

                            // Since this call runs in the background without user interaction,
                            // if the result is not "approved"(in case of regular prescreen call) and not "account found" (in case of lookup call)
                            // or prescreenId is null (in case user is approved, but already has an account),
                            // we simply return without taking any further action.
                            if ((prescreenResult != PrescreenResult.APPROVED && prescreenResult != PrescreenResult.ACCOUNT_FOUND)
                                || placementsConfiguration.rtpsData.prescreenId == null
                            ) {
                                callback(
                                    BreadPartnerEvent.SdkError(
                                        error = Exception(
                                            "Error: PreScreen Result $prescreenResult"
                                        )
                                    )
                                )
                            } else {
                                fetchRTPSData(
                                    rtpsResponse.updateMerchantConfiguration(merchantConfiguration),
                                    placementsConfiguration,
                                    viewContext,
                                    callback,
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
                    // Check if this is an security challenge
                    val errorMessage = result.error.message ?: ""
                    if (errorMessage.startsWith(
                            "Security challenge detected:",
                            ignoreCase = true
                        )
                    ) {
                        val htmlContent =
                            errorMessage.removePrefix("Security challenge detected:").trim()

                        val challengeDialog = ChallengeDialog(
                            htmlContent = htmlContent,
                            baseUrl = apiUrl.substringBefore("/api"),
                            onComplete = { capturedCookies ->
                                // Retry the API call after challenge completion with captured cookies
                                rtpsCall(
                                    merchantConfiguration,
                                    placementsConfiguration,
                                    viewContext,
                                    callback,
                                    capturedCookies,
                                )
                            }
                        )

                        callback(BreadPartnerEvent.RenderPopupView(dialogFragment = challengeDialog))
                    } else {
                        callback(
                            BreadPartnerEvent.SdkError(
                                error = Exception(errorMessage)
                            )
                        )
                    }
                }
            }
        }
    }

}

/**
 * Fetches placement data to be displayed as a text view with a clickable button
 * in the brand partner's UI.
 */
fun BreadPartnersSDK.fetchRTPSData(
    merchantConfiguration: MerchantConfiguration,
    placementsConfiguration: PlacementsConfiguration,
    viewContext: Context,
    callback: (BreadPartnerEvent) -> Unit
) {
    val apiUrl = APIUrl(urlType = APIUrlType.GeneratePlacements).url

    val webUrl: String? = if (placementsConfiguration.rtpsData?.customerAcceptedOffer == true) {
        CommonUtils().buildBpsWebURL(
            integrationKey = integrationKey,
            merchantConfiguration = merchantConfiguration,
            placementsConfiguration,
        )?.toString()
    } else {
        CommonUtils().buildRTPSWebURL(
            integrationKey = integrationKey,
            merchantConfiguration = merchantConfiguration,
            rtpsConfig = placementsConfiguration.rtpsData!!,
        )?.toString()
    }

    val placementRequest = PlacementRequest(
        placements = listOf(
            PlacementRequestBody(
                context = ContextRequestBody(
                    ENV = merchantConfiguration.env?.value.takeIfNotEmpty(),
                    LOCATION = "RTPS-Approval",
                    embeddedUrl = webUrl
                )
            )
        ),
        brandId = integrationKey
    )

    APIClient().request(
        urlString = apiUrl, method = HTTPMethod.POST, body = placementRequest
    ) { result ->
        when (result) {
            is Result.Success -> {
                try {
                    handleRTPSResponse(
                        merchantConfiguration,
                        placementsConfiguration,
                        viewContext,
                        callback,
                        result.data
                    )
                } catch (error: Exception) {
                    callback(
                        BreadPartnerEvent.SdkError(
                            error = Exception(
                                error.message
                            )
                        )
                    )
                }
            }

            is Result.Failure -> {
                callback(
                    BreadPartnerEvent.SdkError(
                        error = Exception(
                            Constants.generatePlacementAPIError(message = "${result.error}")
                        )
                    )
                )
            }
        }
    }
}

/**
 * Handle RTPS data to be displayed as a Popup view
 * in the brand partner's UI.
 */
fun BreadPartnersSDK.handleRTPSResponse(
    merchantConfiguration: MerchantConfiguration,
    placementsConfiguration: PlacementsConfiguration,
    viewContext: Context,
    callback: (BreadPartnerEvent) -> Unit,
    response: Any
) {
    CommonUtils().decodeJSON(
        response.toString(),
        PlacementsResponse::class.java,
        onSuccess = { placementsResponse ->
            val htmlContent = placementsResponse.placementContent
                ?.firstOrNull()
                ?.contentData
                ?.htmlContent
                .orEmpty()

            val popupPlacementModel = HTMLContentParser().extractPopupPlacementModel(htmlContent)
                ?: return@decodeJSON callback(
                    BreadPartnerEvent.SdkError(
                        error = Exception(Constants.popupPlacementParsingError)
                    )
                )

            // Update location from render context
            val location =
                placementsResponse.placements?.first()?.renderContext?.LOCATION.toString()
            val updatedModel = popupPlacementModel.copy(location = location)

            // Determine overlay type
            val overlayType = HTMLContentParser().handleOverlayType(updatedModel.overlayType)
                ?: PlacementOverlayType.EMBEDDED_OVERLAY

            HTMLContentRenderer(
                integrationKey,
                merchantConfiguration,
                placementsConfiguration,
                brandConfiguration,
                false,
                callback
            ).createPopupOverlay(
                popupPlacementModel = updatedModel,
                overlayType = overlayType
            )
        },
        onError = { error ->
            callback(
                BreadPartnerEvent.SdkError(
                    error = Exception(
                        error.message
                    )
                )
            )
        })
}
