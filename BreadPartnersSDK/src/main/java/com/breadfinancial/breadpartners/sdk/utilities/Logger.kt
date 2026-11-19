//------------------------------------------------------------------------------
//  File:          Logger().kt
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

import android.util.Log
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PopupPlacementModel
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.TextPlacementModel
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Class responsible for logging information for debugging and tracking purposes.
 */
object Logger {
    private var isLoggingEnabled: Boolean = true

    var loggingEnabled: Boolean
        get() = isLoggingEnabled
        set(value) {
            isLoggingEnabled = value
        }

    var callback: ((BreadPartnerEvent) -> Unit?)? = null
        set(value) {
            field = value
        }

    private fun generateDashLine(length: Int): String {
        return "-".repeat(length)
    }

    internal fun printLog(message: String) {
        if (!isLoggingEnabled) return
        callback?.invoke(BreadPartnerEvent.OnSDKEventLog(message))
        System.out.write(message.toByteArray())
        System.out.write("\n".toByteArray()) // Adding a newline after the log
    }

    fun logRequestDetails(
        url: String,
        method: String,
        headers: Map<String, String>?,
        body: ByteArray?,
    ) {
        if (!isLoggingEnabled) return

        printLog("\n${generateDashLine(20)} Request Details ${generateDashLine(20)}")
        printLog("URL     : $url")
        printLog("Method  : $method")

        headers?.let {
            printLog("Headers : $it")
        } ?: printLog("Headers : None")

        body?.let {
            try {
                val jsonString = String(it, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(jsonString)
                val prettyJsonString = jsonObject.toString(4)
                printLog("Body    : $prettyJsonString")
            } catch (e: Exception) {
                printLog("Body    : None or Unformatted")
            }
        } ?: printLog("Body    : None or Unformatted")

        printLog("${generateDashLine(60)}\n")
    }

    fun logResponseDetails(
        url: String,
        statusCode: Int,
        headers: Map<String, List<String>>,
        body: ByteArray?,
    ) {
        if (!isLoggingEnabled) return

        printLog("\n${generateDashLine(20)} Response Details ${generateDashLine(20)}")
        printLog("URL         : $url")
        printLog("Status Code : $statusCode")
        printLog("Headers     : $headers")

        body?.let {
            try {
                val jsonString = String(it, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(jsonString)
                val prettyJsonString = jsonObject.toString(4)
                printLog("Body        : $prettyJsonString")
            } catch (e: Exception) {
                val bodyString = String(it, StandardCharsets.UTF_8)
                printLog("Body        : $bodyString")
            }
        } ?: printLog("Body        : None or Unformatted")

        printLog("${generateDashLine(60)}\n")
    }

    fun logTextPlacementModelDetails(model: TextPlacementModel) {
        if (!isLoggingEnabled) return
        printLog("\n${generateDashLine(20)} Text Placement Model Details ${generateDashLine(20)}")
        printLog("Action Type       : ${model.actionType ?: "N/A"}")
        printLog("Action Target     : ${model.actionTarget ?: "N/A"}")
        printLog("Content Text      : ${model.contentText ?: "N/A"}")
        printLog("Action Link       : ${model.actionLink ?: "N/A"}")
        printLog("Action Content ID : ${model.actionContentId ?: "N/A"}")
        printLog("${generateDashLine(60)}\n")
    }

    fun logPopupPlacementModelDetails(model: PopupPlacementModel) {
        if (!isLoggingEnabled) return

        printLog("\n${generateDashLine(20)} Popup Placement Model Details ${generateDashLine(20)}")
        printLog("Overlay Type                  : ${model.overlayType}")
        printLog("Brand Logo URL                : ${model.brandLogoUrl}")
        printLog("WebView URL                   : ${model.webViewUrl}")
        printLog("Overlay Title                 : ${model.overlayTitle}")
        printLog("Overlay Subtitle              : ${model.overlaySubtitle}")
        printLog("Overlay Container Bar Heading : ${model.overlayContainerBarHeading}")
        printLog("Body Header                   : ${model.bodyHeader}")
        printLog("Disclosure                    : ${model.disclosure}")

        model.primaryActionButtonAttributes?.let { primaryActionButton ->
            printLog("\n${generateDashLine(20)} Primary Action Button Details ${generateDashLine(20)}")
            printLog("  Data Overlay Type       : ${primaryActionButton.dataOverlayType ?: "N/A"}")
            printLog("  Data Content Fetch      : ${primaryActionButton.dataContentFetch ?: "N/A"}")
            printLog("  Data Action Target      : ${primaryActionButton.dataActionTarget ?: "N/A"}")
            printLog("  Data Action Type        : ${primaryActionButton.dataActionType ?: "N/A"}")
            printLog("  Data Action Content ID  : ${primaryActionButton.dataActionContentId ?: "N/A"}")
            printLog("  Data Location           : ${primaryActionButton.dataLocation ?: "N/A"}")
            printLog("  Button Text             : ${primaryActionButton.buttonText ?: "N/A"}")
        } ?: run {
            printLog("${generateDashLine(20)} Primary Action Button: N/A ${generateDashLine(20)}")
        }

        if (model.dynamicBodyModel.bodyDiv.isNotEmpty()) {
            val logOutput = StringBuilder(
                "\n${generateDashLine(20)} Dynamic Body Model Details ${
                    generateDashLine(
                        20,
                    )
                }\n",
            )
            model.dynamicBodyModel.bodyDiv.forEach { (key, bodyContent) ->
                logOutput.append("  Body Div Key [$key]:\n")
                bodyContent.tagValuePairs.forEach { (tag, value) ->
                    logOutput.append("    - $tag: $value\n")
                }
            }
            logOutput.append("${generateDashLine(60)}\n")
            printLog(logOutput.toString())
        } else {
            printLog("\n${generateDashLine(20)} Dynamic Body Model Details ${generateDashLine(20)}")
            printLog("Dynamic Body Model: N/A")
            printLog("${generateDashLine(60)}\n")
        }
    }

    fun logLoadingURL(url: String) {
        if (!isLoggingEnabled) return

        printLog("${generateDashLine(20)} WebView URL ${generateDashLine(20)}")
        printLog(url.trim())
        printLog("${generateDashLine(60)}\n")
    }

    fun logApplicationResultDetails(payload: JSONObject) {
        if (!isLoggingEnabled) return
        printLog("\n${generateDashLine(20)} Application Result Details ${generateDashLine(20)}")
        printLog("Application ID     : ${payload.optString("applicationId", "N/A")}")
        printLog("Call ID            : ${payload.optString("callId", "N/A")}")
        printLog("Card Type          : ${payload.optString("cardType", "N/A")}")
        printLog("Email Address      : ${payload.optString("emailAddress", "N/A")}")
        printLog("Message            : ${payload.optString("message", "N/A")}")
        printLog("Mobile Phone       : ${payload.optString("mobilePhone", "N/A")}")
        printLog("Result             : ${payload.optString("result", "N/A")}")
        printLog("Status             : ${payload.optString("status", "N/A")}")
        printLog("${generateDashLine(60)}\n")
    }

    fun printWebAnchorLogs(data: String) {
        if (!isLoggingEnabled) return
        Log.i("", "\n${generateDashLine(20)} Anchor List ${generateDashLine(20)}")
        Log.i("", data)
        Log.i("", "${generateDashLine(60)}\n")
    }
}
