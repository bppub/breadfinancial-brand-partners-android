//------------------------------------------------------------------------------
//  File:          PopupDialog.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.breadfinancial.breadpartners.sdk.R
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.BreadFinancialWebViewInterstitial
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PlacementOverlayType
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.models.PopupPlacementModel
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.extensions.closeButtonTapped
import com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents.popup.extensions.setupUI
import com.breadfinancial.breadpartners.sdk.networking.models.BrandConfigResponse
import com.breadfinancial.breadpartners.sdk.utilities.LoaderIndicator
import com.bumptech.glide.Glide

/**
 * DialogFragment that renders a dynamic popup based on placement data,
 * handling UI rendering, user interactions, and event callbacks.
 */
class PopupDialog(
    internal val integrationKey: String,
    internal val popupModel: PopupPlacementModel,
    internal val overlayType: PlacementOverlayType,
    internal var merchantConfiguration: MerchantConfiguration?,
    internal var placementsConfiguration: PlacementsConfiguration?,
    internal var brandConfiguration: BrandConfigResponse?,
    internal val callback: (BreadPartnerEvent) -> Unit?
) : DialogFragment(), BreadFinancialWebViewInterstitial.WebViewRestartButtonListener {

    internal lateinit var popupView: View
    internal lateinit var closeButton: ImageView
    internal lateinit var brandLogo: ImageView
    internal lateinit var dividerTop: View
    internal lateinit var dividerBottom: View
    internal lateinit var titleLabel: TextView
    internal lateinit var subtitleLabel: TextView
    internal lateinit var headerView: LinearLayout
    internal lateinit var headerLabel: TextView
    internal lateinit var disclosureLabel: TextView
    internal lateinit var contentContainer: LinearLayout
    internal lateinit var contentStackView: LinearLayout
    internal lateinit var bottomBanner: LinearLayout
    internal lateinit var actionButton: Button
    internal lateinit var overlayProductView: LinearLayout
    internal lateinit var overlayEmbeddedView: FrameLayout
    internal lateinit var webViewManager: BreadFinancialWebViewInterstitial
    internal lateinit var loader: LoaderIndicator
    internal lateinit var webViewPlacementModel: PopupPlacementModel
    internal fun isWebViewPlacementModelInitialized() = ::webViewPlacementModel.isInitialized

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
//        isCancelable = false
        popupView = inflater.inflate(R.layout.popup_dialog, container, false)

        setupUI()

        return popupView
    }

    override fun onStart() {
        super.onStart()

        val cornerRadius = 32f
        val radii = FloatArray(8) { cornerRadius }
        val shape = RoundRectShape(radii, null, null)

        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = Color.WHITE
        shapeDrawable.paint.strokeWidth = 1f
        shapeDrawable.paint.color = Color.GRAY

        dialog?.window?.setBackgroundDrawable(shapeDrawable)

        dialog?.window?.setLayout(
            (requireContext().resources.displayMetrics.widthPixels * 0.9).toInt(),
            (requireContext().resources.displayMetrics.heightPixels * 0.9).toInt()
        )

        if (popupModel.location == "RTPS-Approval") {
            closeButton.visibility = View.GONE
        }
    }

    /**
     * Displays the embedded overlay UI using the provided placement model.
     */
    internal fun displayEmbeddedOverlay(popupPlacementModel: PopupPlacementModel) {
        bottomBanner.visibility = View.GONE
        overlayProductView.visibility = View.GONE
        loader.visibility = View.VISIBLE
        overlayEmbeddedView.visibility = View.VISIBLE
        val placeholder = popupView.findViewById<FrameLayout>(R.id.overlay_embedded_view)
        webViewManager.replaceViewWithWebView(
            placeholder, popupPlacementModel.webViewUrl
        ) {
            loader.visibility = View.GONE
        }
        Glide.with(this).load(popupPlacementModel.brandLogoUrl).into(brandLogo)
    }

    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)
        closeButtonTapped()
    }

    override fun onAppRestartClicked(url: String) {
        val placeholder = popupView.findViewById<FrameLayout>(R.id.overlay_embedded_view)

        Handler(Looper.getMainLooper()).post {
            webViewManager.replaceViewWithWebView(
                placeholder, url
            ) {
                loader.visibility = View.GONE
            }
        }
    }
}