//------------------------------------------------------------------------------
//  File:          Constants.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("ConstPropertyName")

package com.breadfinancial.breadpartners.sdk.utilities

/**
 * Object containing constant values used throughout the SDK.
 */
object Constants {

    // API constants
    const val headerClientKey = "X-Client-Key"
    const val headerRequestedWithKey = "X-Requested-With"
    const val headerRequestedWithValue = "XMLHttpRequest"
    const val headerUserAgentKey = "User-Agent"
    const val headerPlatformKey = "platform"
    const val headerPlatformValue = "android"
    const val headerContentType = "Content-Type"
    const val headerContentTypeValue = "application/json"
    const val headerOriginKey = "Origin"
    const val headerOriginValue = "https://brand-sdk.kmsmep.com"
    const val headerAuthorityKey = "authority"
    const val headerAuthorityValue = "metrics.kmsmep.com"
    const val headerAcceptKey = "Accept"
    const val headerAcceptValue = "*/*"
    const val headerAcceptEncodingKey = "Accept-Encoding"
    const val headerAcceptEncodingValue = "gzip, deflate, br, zstd"
    const val headerAcceptLanguageKey = "Accept-Language"
    const val headerAcceptLanguageValue = "en-GB,en-US;q=0.9,en;q=0.8"
    const val headerAccessControlRequestHeadersKey = "Access-Control-Request-Headers"
    const val headerAccessControlRequestHeadersValue = "content-type"
    const val headerAccessControlRequestMethodKey = "Access-Control-Request-Method"
    const val headerAccessControlRequestMethodValue = "POST"

    fun nativeSDKAlertTitle(): String {
        return "Bread Partner"
    }

    fun catchError(message: String): String {
        return "$error $message"
    }

    const val securityCheckAlertTitle = "Pre-Fraud Check"
    const val securityCheckFailureAlertTitle =
        "Unable to Verify. Please call us at 1-800-xxx-xxxx for assistance."

    const val okButton = "Ok"

    const val securityCheckAlertAcknolwedgeMessage =
        "Your web view will load once the captcha verification is successfully completed. This ensures that all transactions are secure."

    fun securityCheckAlertFailedMessage(error: String): String {
        return "Error: ${error}"
    }

    const val error = "Error:"

    fun generatePlacementAPIError(message: String): String {
        return "$error $message"
    }

    const val consecutivePlacementRequestDataError = "Consecutive placement request data not found"

    const val apiResToJsonMapError = "Unable to convert response to map."

    const val textPlacementError = "$error Unable to handle text placement type."
    const val missingTextPlacementError = "Unhandled text placement type."
    const val noTextPlacementError = "No text placement type found."
    const val textPlacementParsingError = "Unable to parse text placement."
    const val prescreenRequiredFieldsError =
        "Error: Prescreen requires customer information: firstname, lastname, and complete billing address must be provided in MerchantConfiguration."

    const val popupPlacementParsingError = "$error Unable to parse popup placement."
    const val missingPopupPlacementError = "Unhandled popup placement type."

    const val somethingWentWrong = "Something went wrong. Please try again later."

    fun unableToLoadWebURL(message: String): String {
        return "$error Web Url Loading Issue: $message"
    }
}