//------------------------------------------------------------------------------
//  File:          ChallengeDialog.kt
//  Author(s):     Bread Financial
//  Date:          08 December 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.htmlhandling

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.breadfinancial.breadpartners.sdk.R
import com.breadfinancial.breadpartners.sdk.utilities.Logger

class ChallengeDialog(
    private val htmlContent: String,
    private val baseUrl: String,
    private val onComplete: (cookies: String) -> Unit
) : DialogFragment() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_challenge, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val webView = view.findViewById<WebView>(R.id.challengeWebView)
        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)

        closeButton.setOnClickListener {
            dismiss()
        }

        // Enable cookie handling
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            domStorageEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // After initial page load, prevent all navigation attempts
                // Check if this navigation is due to captcha completion
                    handler.postDelayed({
                        checkForCompletionNow()
                    }, 500)

                    return true // Block navigation
            }
        }

        // Load the challenge HTML with a base URL to allow iframe loading
        webView.loadDataWithBaseURL(
            baseUrl,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun checkForCompletionNow() {
        val cookieManager = CookieManager.getInstance()
        val currentCookies = cookieManager.getCookie(baseUrl) ?: ""

        if (currentCookies.isNotEmpty()) {

            // Give a small delay for any final cookies to settle
            handler.postDelayed({
                completeCaptcha()
            }, 500)
        }
        else {
            Logger.printLog("No cookies available - cannot complete")
        }
    }

    private fun completeCaptcha() {
        if (isAdded) {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(baseUrl) ?: ""
            Logger.printLog("Completing captcha with cookies: ${cookies.take(100)}...")
            dismiss()
            onComplete(cookies)
        }
    }

    override fun onDestroyView() {
        val webView = view?.findViewById<WebView>(R.id.challengeWebView)
        webView?.destroy()
        super.onDestroyView()
    }
}