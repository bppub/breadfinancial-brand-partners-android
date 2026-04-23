//------------------------------------------------------------------------------
//  File:          PlacementButtonConfiguration.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models

import android.graphics.Color

/**
 * Configuration model for customizing placement button appearance and layout.
 */
data class PlacementButtonConfiguration(
    val imagePosition: ImagePosition = ImagePosition.LEFT,
    val backgroundColor: Int = Color.TRANSPARENT,
    val buttonWidthDp: Int = 200,
    val buttonHeightDp: Int = 100,
    val imageWidthDp: Int = 180,
    val imageHeightDp: Int = 100
)

/**
 * Enum representing the position of the image relative to text in the button.
 */
enum class ImagePosition {
    LEFT,    // Image on left, text on right
    RIGHT,   // Image on right, text on left
    TOP,     // Image on top, text on bottom
    BOTTOM   // Image on bottom, text on top
}

