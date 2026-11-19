//------------------------------------------------------------------------------
//  File:          BrandConfigResponse.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("PropertyName")

package com.breadfinancial.breadpartners.sdk.networking.models

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import com.google.gson.annotations.SerializedName

/**
 * Data model represents brand-specific configuration response.
 */
data class BrandConfigResponse(
    val config: Config,
)

/**
 * Holds various configuration values used for brand customization and environment handling.
 */
data class Config(
    val recaptchaEnabledQA: String = "",
    val recaptchaSiteKeyQA: String = "",
    val recaptchaSiteKeyPROD: String = "",
    val recaptchaEnabledPROD: String = "",
    val AEMContent: String = "",
    val clientName: String = "",
    @SerializedName("rsk_UAT_NATIVE_IOS")
    val rskUatNativeIos: String = "",
    @SerializedName("rsk_UAT_NATIVE_ANDROID")
    val rskUatNativeAndroid: String = "",
    @SerializedName("rsk_STAGE_NATIVE_IOS")
    val rskStageNativeIos: String = "",
    @SerializedName("rsk_STAGE_NATIVE_ANDROID")
    val rskStageNativeAndroid: String = "",
    @SerializedName("rsk_PROD_NATIVE_IOS")
    val rskProdNativeIos: String = "",
    @SerializedName("rsk_PROD_NATIVE_ANDROID")
    val rskProdNativeAndroid: String = "",
) {
    fun getRecaptchaKey(environment: BreadPartnersEnvironment): String =
        when (environment) {
            BreadPartnersEnvironment.UAT -> rskUatNativeAndroid
            BreadPartnersEnvironment.STAGE -> rskStageNativeAndroid
            BreadPartnersEnvironment.PROD -> rskProdNativeAndroid
        }
}
