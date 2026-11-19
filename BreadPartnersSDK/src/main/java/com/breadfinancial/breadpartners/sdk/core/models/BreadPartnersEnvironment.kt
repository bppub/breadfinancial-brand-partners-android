//------------------------------------------------------------------------------
//  File:          BreadPartnersEnvironment.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.models

/**
 * Represents the different environments the SDK can operate in.
 *
 * @property STAGE Use this environment for testing and development.
 * @property PROD **Default** Use this environment for production.
 */
enum class BreadPartnersEnvironment(
    val value: String,
) {
    STAGE("STAGE"),
    PROD("PROD"),
    UAT("UAT"),
}
