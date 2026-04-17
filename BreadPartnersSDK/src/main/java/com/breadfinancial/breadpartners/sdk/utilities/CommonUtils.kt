//------------------------------------------------------------------------------
//  File:          CommonUtils.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.utilities

import android.graphics.Color
import android.os.Build
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.RTPSData
import com.breadfinancial.breadpartners.sdk.networking.APIUrl
import com.breadfinancial.breadpartners.sdk.networking.APIUrlType
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.takeIfNotEmpty
import com.google.gson.Gson
import java.net.URL

/**
 * Utility class for handling common operations across the SDK.
 */
class CommonUtils(
) {

    /**
     * Returns the current timestamp.
     */
    fun getCurrentTimestamp(): String {
        val dateFormatter = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()
        )
        dateFormatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val currentDate = java.util.Date()
        return dateFormatter.format(currentDate)
    }

    /**
     * Safely decodes a JSON string into the specified model class.
     */
    fun <T> decodeJSON(
        jsonString: String, clazz: Class<T>, onSuccess: (T) -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val gson = Gson()
            val result = gson.fromJson(jsonString, clazz)
            onSuccess(result)
        } catch (error: Exception) {
            onError(error)
        }
    }

    /**
     * Constructs the full RTPS Web URL with required query parameters.
     */
    fun buildRTPSWebURL(
        integrationKey: String,
        merchantConfiguration: MerchantConfiguration,
        rtpsConfig: RTPSData,
    ): URL? {
        val queryParams = mapOf(
            "mockMO" to rtpsConfig.mockResponse?.value.takeIfNotEmpty(),
            "mockPA" to rtpsConfig.mockResponse?.value.takeIfNotEmpty(),
            "mockVL" to rtpsConfig.mockResponse?.value.takeIfNotEmpty(),
            "embedded" to "true",
            "clientKey" to integrationKey.takeIfNotEmpty(),
            "prescreenId" to rtpsConfig.prescreenId,
            "cardType" to rtpsConfig.cardType.takeIfNotEmpty(),
            "urlPath" to rtpsConfig.screenName.takeIfNotEmpty(),
            "firstName" to merchantConfiguration.buyer?.givenName.takeIfNotEmpty(),
            "lastName" to merchantConfiguration.buyer?.familyName.takeIfNotEmpty(),
            "address1" to merchantConfiguration.buyer?.billingAddress?.address1.takeIfNotEmpty(),
            "city" to merchantConfiguration.buyer?.billingAddress?.locality.takeIfNotEmpty(),
            "state" to merchantConfiguration.buyer?.billingAddress?.region.takeIfNotEmpty(),
            "zip" to merchantConfiguration.buyer?.billingAddress?.postalCode.takeIfNotEmpty(),
            "storeNumber" to merchantConfiguration.storeNumber.takeIfNotEmpty(),
            "location" to rtpsConfig.locationType?.value?.takeIfNotEmpty(),
            "channel" to rtpsConfig.channel.takeIfNotEmpty(),
        )

        return try {
            val urlComponents = URL(APIUrl(urlType = APIUrlType.RTPSWebURL("offer")).url)
            val queryString =
                queryParams.filterValues { it != null }.map { "${it.key}=${it.value}" }
                    .joinToString("&")

            URL("$urlComponents?$queryString")
        } catch (e: Exception) {
            null
        }
    }


    /**
     * Builds a URL for BPS (Batch Prescreen) Web based on the provided integration and configuration details.
     * - Parameters:
     *  - integrationKey: The unique integration key for the request.
     *  - merchantConfiguration: The merchant configuration containing buyer and merchant-specific data.
     *  - placementConfiguration: The placement configuration containing placement or RTPS data.
     * - Returns: A URL constructed with the given parameters, or null if the URL could not be built.
     */
    fun buildBpsWebURL(
        integrationKey: String,
        merchantConfiguration: MerchantConfiguration,
        placementsConfiguration: PlacementsConfiguration,
    ): URL? {

        val mockResponseValue = placementsConfiguration.rtpsData?.mockResponse?.value

        // Extract buyer information
        val buyer = merchantConfiguration.buyer
        val billingAddress = buyer?.billingAddress

        // Extract order data from RTPS or placement data
        val order =
            placementsConfiguration.rtpsData?.order ?: placementsConfiguration.placementData?.order

        // Extract location - prioritize RTPS location over placement location
        val location = placementsConfiguration.rtpsData?.locationType
            ?: placementsConfiguration.placementData?.locationType

        // Extract channel - prioritize RTPS channel over merchant channel
        val channel = placementsConfiguration.rtpsData?.channel ?: merchantConfiguration.channel
        val subchannel =
            placementsConfiguration.rtpsData?.subChannel ?: merchantConfiguration.subchannel

        val queryParams = mapOf(
            "mockMO" to mockResponseValue.takeIfNotEmpty(),
            "mockPA" to mockResponseValue.takeIfNotEmpty(),
            "mockVL" to mockResponseValue.takeIfNotEmpty(),
            "embedded" to "true",
            "clientKey" to integrationKey,
            "prescreenId" to placementsConfiguration.rtpsData?.prescreenId,
            "firstName" to buyer?.givenName,
            "middleInitial" to buyer?.additionalName,
            "lastName" to buyer?.familyName,
            "address1" to billingAddress?.address1,
            "address2" to billingAddress?.address2,
            "city" to billingAddress?.locality,
            "state" to billingAddress?.region,
            "zip" to billingAddress?.postalCode,
            "emailAddress" to buyer?.email,
            "mobilePhone" to buyer?.phone,
            "alternativePhone" to buyer?.alternativePhone,
            "cardType" to placementsConfiguration.rtpsData?.cardType,
            "storeNumber" to merchantConfiguration.storeNumber,
            "loyaltyNumber" to merchantConfiguration.loyaltyID,
            "customerNumber" to null,
            "cartAmount" to order?.subTotal?.value?.toString(),
            "productAmount" to order?.items?.first()?.unitPrice?.value.toString(),
            "productAmount" to order?.items?.first()?.unitPrice?.value.toString(),
            "checkoutAmount" to order?.totalPrice?.value.toString(),
            "urlPath" to null,
            "location" to location,
            "category" to order?.items?.first()?.category,
            "sku" to order?.items?.first()?.sku,
            "correlationData" to placementsConfiguration.rtpsData?.correlationData,
            "epId" to null,
            "epPlacementId" to null,
            "epSessionId" to null,
            "epMessageId" to null,
            "channel" to channel,
            "subchannel" to subchannel,
            "clientVariable1" to merchantConfiguration.clientVariable1,
            "clientVariable2" to merchantConfiguration.clientVariable2,
            "clientVariable3" to merchantConfiguration.clientVariable3,
            "clientVariable4" to merchantConfiguration.clientVariable4,
            "selectedCardKey" to placementsConfiguration.placementData?.selectedCardKey,
            "defaultSelectedCardKey" to placementsConfiguration.placementData?.defaultSelectedCardKey,
            "departmentId" to merchantConfiguration.departmentId,
            "overrideKey" to merchantConfiguration.overrideKey,
            "cardChoiceCode" to null,
            "associateId" to merchantConfiguration.clerkId,
            "carrier" to null,
            "keyword" to null,
            "shortCode" to null,
            "channelId" to null,
            "applicationSubType" to null,
            "splitPayment" to null
        )

        return try {
            val urlComponents = URL(APIUrl(urlType = APIUrlType.BatchPreScreen).url)
            val queryString =
                queryParams.filterValues { it != null }.map { "${it.key}=${it.value}" }
                    .joinToString("&")

            URL("$urlComponents?$queryString")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns a darker shade of the given color.
     */
    fun darkerColor(color: Int): Int {
        val factor = 0.8f
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.rgb(r, g, b)
    }

    /**
     * Generates a simplified User-Agent string based on the device's manufacturer, model, and OS version.
     */
    fun getUserAgent(): String {
        val osVersion = "Android ${Build.VERSION.RELEASE}" // e.g., "Android 13"
        val deviceModel = Build.MODEL                    // e.g., "Pixel 7"
        val manufacturer = Build.MANUFACTURER           // e.g., "Google"
        return "$manufacturer $deviceModel; $osVersion" // Google Pixel 7; Android 13
    }
}
