//------------------------------------------------------------------------------
//  File:          HTMLContentParser.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling

import com.breadfinancial.breadpartners.sdk.htmlhandling.extensions.getHtmlAsSpanned
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementActionType
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementOverlayType
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PopupPlacementModel
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PrimaryActionButtonModel
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.TextPlacementModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Parses and processes HTML content for rendering and interaction handling.
 */
class HTMLContentParser {

    /**
     * Extracts a TextPlacementModel from the given HTML content.
     */
    fun extractTextPlacementModel(htmlContent: String): TextPlacementModel {
        val document = Jsoup.parse(htmlContent)

        val actionType = document.select("[data-action-type]").attr("data-action-type")
            .takeIf { it.isNotEmpty() }
        val actionTarget = document.select("[data-action-target]").attr("data-action-target")
            .takeIf { it.isNotEmpty() }
        val actionContentId =
            document.select("[data-action-content-id]").attr("data-action-content-id")
                .takeIf { it.isNotEmpty() }
        val contentText = document.select(".epjs-body").first()?.ownText().orEmpty().trim()
        val actionLink = document.select(".epjs-body-action a").text().takeIf { it.isNotEmpty() }
        val paymentDetailsSup = document.select("sup").text()
        val paymentDetails =
            document.select(".ep-text-placement").text().replace(actionLink.orEmpty(), "")
                .replace(paymentDetailsSup, "").trim()
        val htmlContentWithFormatting = document.select(".ep-text-placement").html()

        val finalContentText = when {
            contentText == paymentDetails -> contentText
            contentText.isEmpty() -> paymentDetails
            paymentDetails.isEmpty() -> contentText
            else -> "$contentText $paymentDetails"
        }.takeIf { it.isNotEmpty() }

        return TextPlacementModel(
            actionType = actionType,
            actionTarget = actionTarget,
            contentText = finalContentText,
            actionLink = actionLink,
            actionContentId = actionContentId,
            htmlContent = htmlContentWithFormatting
        )
    }

    /**
     * Parses HTML content to extract a PopupPlacementModel.
     */
    fun extractPopupPlacementModel(htmlContent: String): PopupPlacementModel? {
        return try {
            val document = Jsoup.parse(htmlContent)

            val overlayType =
                document.select("[data-overlay-metadata]").first()?.attr("data-overlay-type")
                    .orEmpty()
            val brandLogoUrl = document.select(".brand.logo img").first()?.attr("src").orEmpty()
            val webViewUrl = document.select("iframe").first()?.attr("src").orEmpty()
            val overlayTitle = document.select(".epjs-css-overlay-title").getHtmlAsSpanned()
            val overlaySubtitle = document.select(".epjs-css-overlay-subtitle").getHtmlAsSpanned()
            val overlayContainerBarHeading =
                document.select(".epjs-css-overlay-body-title-bar").getHtmlAsSpanned()
            val bodyHeader = document.select(".epjs-css-overlay-header").getHtmlAsSpanned()
            val disclosure = document.select(".epjs-css-overlay-disclosures").getHtmlAsSpanned()
            val primaryActionButtonAttributes =
                extractPrimaryCTAButtonAttributes(document, ".action-button")

            val dynamicBodyModel = processDynamicBodyModel(document)

            PopupPlacementModel(
                overlayType = overlayType,
                brandLogoUrl = brandLogoUrl,
                location = "",
                webViewUrl = webViewUrl,
                overlayTitle = overlayTitle,
                overlaySubtitle = overlaySubtitle,
                overlayContainerBarHeading = overlayContainerBarHeading,
                bodyHeader = bodyHeader,
                primaryActionButtonAttributes = primaryActionButtonAttributes,
                dynamicBodyModel = dynamicBodyModel,
                disclosure = disclosure
            )
        } catch (error: Exception) {
            throw error
        }
    }

    /**
     * Processes the HTML document to generate a DynamicBodyModel for the popup.
     */
    private fun processDynamicBodyModel(document: Document): PopupPlacementModel.DynamicBodyModel {
        val bodyContainer = document.select(".epjs-css-overlay-body-content")
        val mutableBodyDiv = mutableMapOf<String, PopupPlacementModel.DynamicBodyContent>()
        var sequenceCounter = 0
        bodyContainer.forEachIndexed { _, mainParent ->
            mainParent.children().forEach { child ->
                when (child.className()) {
                    "epjs-css-overlay-value-prop" -> {
                        val bodyContent = PopupPlacementModel.DynamicBodyContent(
                            child.children().associate { it.tagName() to it.getHtmlAsSpanned() })
                        mutableBodyDiv["div${sequenceCounter++}"] = bodyContent
                    }

                    "epjs-css-overlay-value-prop-connector" -> {
                        val bodyContent = PopupPlacementModel.DynamicBodyContent(
                            mapOf("connector" to child.getHtmlAsSpanned())
                        )
                        mutableBodyDiv["div${sequenceCounter++}"] = bodyContent
                    }

                    "epjs-css-overlay-body-footer" -> {
                        val bodyContent = PopupPlacementModel.DynamicBodyContent(
                            child.children().associate { it.tagName() to it.getHtmlAsSpanned() })

                        mutableBodyDiv["footer${sequenceCounter++}"] = bodyContent
                    }

                    else -> {

                    }
                }
            }
        }

        return PopupPlacementModel.DynamicBodyModel(mutableBodyDiv.toMap())
    }

    /**
     * Extracts attributes for the primary CTA button from the HTML document.
     */
    private fun extractPrimaryCTAButtonAttributes(
        document: Document, selector: String
    ): PrimaryActionButtonModel? {
        val button = document.select(selector).first() ?: return null

        return PrimaryActionButtonModel(dataOverlayType = document.select(".epjs-css-modal-footer")
            .first()?.attr("data-overlay-type"),
            dataContentFetch = button.attr("data-content-fetch").takeIf { it.isNotEmpty() },
            dataActionTarget = button.attr("data-action-target").takeIf { it.isNotEmpty() },
            dataActionType = button.attr("data-action-type").takeIf { it.isNotEmpty() },
            dataActionContentId = button.attr("data-action-content-id").takeIf { it.isNotEmpty() },
            dataLocation = button.attr("data-location").takeIf { it.isNotEmpty() },
            buttonText = button.select("span").text().takeIf { it.isNotEmpty() })
    }

    /**
     * Parses the given response string to a PlacementActionType.
     */
    fun handleActionType(response: String): PlacementActionType? {
        return try {
            PlacementActionType.valueOf(response)
        } catch (error: Exception) {
            throw error
        }
    }

    /**
     * Safely parses the given response string to a PlacementOverlayType.
     */
    fun handleOverlayType(response: String): PlacementOverlayType? {
        return try {
            PlacementOverlayType.valueOf(response)
        } catch (error: Exception) {
            throw error
        }
    }
}
