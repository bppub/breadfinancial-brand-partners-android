//------------------------------------------------------------------------------
//  File:          PopupUIExtension.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.breadfinancial.breadpartners.sdk.R
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.PopUpStyling
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.BreadFinancialWebViewInterstitial
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementOverlayType
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PopupPlacementModel
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.PopupDialog
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.PopupElements
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.applyTextStyle
import com.breadfinancial.breadpartners.sdk.utilities.CommonUtils
import com.bumptech.glide.Glide

/**
 * Replaces every URLSpan inside this TextView with a custom ClickableSpan whose
 * onClick routes to [onLinkClicked].  This lets us handle fragment links
 * (e.g. "Back to top" with href="#top") that Html.fromHtml() turns into URLSpans
 * but that the default LinkMovementMethod cannot resolve inside a native view.
 */
private fun TextView.makeLinksClickable(onLinkClicked: (url: String) -> Unit) {
    val raw = text ?: return
    val spannable = SpannableString.valueOf(raw)
    val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
    if (urlSpans.isEmpty()) return

    urlSpans.forEach { urlSpan ->
        val start = spannable.getSpanStart(urlSpan)
        val end   = spannable.getSpanEnd(urlSpan)
        val flags = spannable.getSpanFlags(urlSpan)
        val url   = urlSpan.url
        spannable.removeSpan(urlSpan)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                onLinkClicked(url)
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = true
                ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }, start, end, flags)
    }

    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}

/**
 * Initializes and sets up the UI components of the popup,
 * including layout, styling, and content binding.
 */
fun PopupDialog.setupUI() {

    val popupStyle = placementsConfiguration?.popUpStyling!!
    val buttonStyle = popupStyle.actionButtonStyle

    closeButton = popupView.findViewById(R.id.close_button)
    brandLogo = popupView.findViewById(R.id.brand_logo)
    dividerTop = popupView.findViewById(R.id.divider_top)
    dividerBottom = popupView.findViewById(R.id.divider_bottom)
    titleLabel = popupView.findViewById(R.id.title_label)
    subtitleLabel = popupView.findViewById(R.id.subtitle_label)
    headerView = popupView.findViewById(R.id.header_view)
    headerLabel = popupView.findViewById(R.id.header_label)
    disclosureLabel = popupView.findViewById(R.id.disclosure_label)
    bottomBanner = popupView.findViewById(R.id.bottom_banner)
    actionButton = popupView.findViewById(R.id.action_button)
    contentContainer = popupView.findViewById(R.id.content_container)
    contentStackView = popupView.findViewById(R.id.content_stack_view)
    overlayProductView = popupView.findViewById(R.id.overlay_product_view)
    overlayEmbeddedView = popupView.findViewById(R.id.overlay_embedded_view)
    loader = popupView.findViewById(R.id.loader_indicator)
    loader.updateLoaderColor(placementsConfiguration?.popUpStyling?.loaderColor!!)

    closeButton.setOnClickListener {
        closeButtonTapped()
    }
    closeButton.setColorFilter(popupStyle.crossColor)

    Glide.with(this).load(popupModel.brandLogoUrl).into(brandLogo)

    titleLabel.text = popupModel.overlayTitle
    titleLabel.applyTextStyle(popupStyle.titlePopupTextStyle)
    subtitleLabel.text = popupModel.overlaySubtitle
    subtitleLabel.applyTextStyle(popupStyle.subTitlePopupTextStyle)
    disclosureLabel.text = popupModel.disclosure
    disclosureLabel.applyTextStyle(popupStyle.disclosurePopupTextStyle)
    if (popupModel.overlayContainerBarHeading.isEmpty()) {
        headerView.visibility = View.GONE
    } else {
        headerLabel.text = popupModel.overlayContainerBarHeading
        headerLabel.applyTextStyle(popupStyle.headerPopupTextStyle)
    }

    actionButton.text = popupModel.primaryActionButtonAttributes?.buttonText ?: "Action"
    actionButton.typeface = Typeface.create(buttonStyle?.font, Typeface.BOLD)

    val drawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = buttonStyle!!.cornerRadius
        setColor(buttonStyle.backgroundColor)
    }

    val pressedDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = buttonStyle!!.cornerRadius
        setColor(CommonUtils().darkerColor(buttonStyle.backgroundColor))
    }

    val states = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        addState(intArrayOf(), drawable)
    }

    actionButton.background = states

    dividerTop.setBackgroundColor(popupStyle.dividerColor)

    dividerBottom.setBackgroundColor(popupStyle.dividerColor)

    actionButton.setOnClickListener {
        callback(BreadPartnerEvent.PopupClosed)

        onActionButtonTapped()
    }

    context?.let {
        webViewManager = BreadFinancialWebViewInterstitial(it) { event ->
            when (event) {
                is BreadPartnerEvent.PopupClosed -> closeButtonTapped()
                else -> callback(event)
            }
        }
        webViewManager.setOnAppRestartListener(this)
        addSectionsToLinearLayout(
            popupModel, contentStackView, it, popupStyle
        )

        // The ScrollView wraps overlay_view so that scroll would work.
        val scrollView = overlayProductView.parent as? ScrollView

        // Shared link-click handler for all native TextViews in the popup.
        val linkClickHandler: (String) -> Unit = { url ->
            if (url.equals("#epjs-css-overlay-header", ignoreCase = true)) {
                scrollView?.smoothScrollTo(0, 0)
            }
        }

        disclosureLabel.makeLinksClickable(linkClickHandler)

        PopupElements.shared.decorateLinearLayout(
            linearLayout = contentContainer, borderColor = popupStyle.borderColor
        )
        headerView.setBackgroundColor(Color.GRAY)
        PopupElements.shared.decorateLinearLayout(
            linearLayout = headerView,
            bottomLeftRadius = 0f,
            bottomRightRadius = 0f,
            backgroundColor = popupStyle.headerBgColor,
            borderColor = popupStyle.headerBgColor,
        )
    }

    when (overlayType) {
        PlacementOverlayType.EMBEDDED_OVERLAY -> displayEmbeddedOverlay(popupModel)
        PlacementOverlayType.SINGLE_PRODUCT_OVERLAY -> displayProductOverlay()
    }

    if (overlayType == PlacementOverlayType.SINGLE_PRODUCT_OVERLAY) {
        fetchWebViewPlacement()
    }
}

/**
 * Dynamically adds header and paragraph sections to the popup's
 * sub-container based on the provided placement configuration.
 */
fun PopupDialog.addSectionsToLinearLayout(
    popupModel: PopupPlacementModel,
    container: LinearLayout,
    context: Context,
    popupStyle: PopUpStyling
) {
    val bodyDivModel = popupModel.dynamicBodyModel.bodyDiv
    val tagPriorityList = listOf("h3", "p", "connector")
    val sortedDictList = bodyDivModel.entries.sortedBy { entry ->
        entry.key.replace("div", "").toIntOrNull() ?: 0
    }
    sortedDictList.forEachIndexed { _, (_, value) ->
        val tagValuePairs = value.tagValuePairs
        tagPriorityList.forEach { tag ->
            val content = tagValuePairs[tag]
            content?.let {
                PopupElements.shared.createLabelForTag(
                    popupModel = popupStyle, tag = tag, value = it, context
                )?.let { label ->
                    container.addView(label)
                }
            }
        }
    }
    val containerFooter = bodyDivModel.entries.firstOrNull { it.key.contains("footer") }

    containerFooter.let { footerEntry ->
        val labelValue = footerEntry?.value?.tagValuePairs
        val valueFooter: Spanned? = labelValue?.get("footer")
        if (valueFooter != null) {
            PopupElements.shared.createLabelForTag(
                popupModel = popupStyle, tag = "footer", value = valueFooter, context
            )?.let { label ->
                container.addView(label)
            }
        } else {
            val firstValue = labelValue?.values?.firstOrNull()
            firstValue?.let { spannedValue ->
                PopupElements.shared.createLabelForTag(
                    popupStyle, tag = "footer", value = spannedValue, context
                )?.let { label ->
                    container.addView(label)
                }
            }
        }
    }
    if (bodyDivModel.isEmpty()) {
        container.visibility = View.GONE
    } else {
        container.visibility = View.VISIBLE
    }
}

/**
 * Displays the product overlay view in the popup.
 */
fun PopupDialog.displayProductOverlay() {
    loader.visibility = View.GONE
    overlayEmbeddedView.visibility = View.GONE
    bottomBanner.visibility = View.VISIBLE
    overlayProductView.visibility = View.VISIBLE
}