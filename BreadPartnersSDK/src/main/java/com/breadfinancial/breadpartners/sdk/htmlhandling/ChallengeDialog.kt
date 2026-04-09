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

    private var challengeCompleted: Boolean = false
    private var initialCookies: String = ""
    private var pageLoadTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var cookieCheckRunnable: Runnable? = null
    private val minimumWaitTimeMs = 2000L // Wait at least 2 seconds after page load before considering completion

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
            private var initialPageLoaded = false

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // After initial page load, prevent all navigation attempts
                // This happens when captcha completes and tries to navigate
                if (initialPageLoaded && !challengeCompleted) {
                    // Check if this navigation is due to captcha completion
                    handler.postDelayed({
                        checkForCompletionNow()
                    }, 500)

                    return true // Block navigation
                }

                return false // Allow initial load
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // On first load, capture initial cookies and start monitoring
                if (!challengeCompleted && !initialPageLoaded) {
                    initialPageLoaded = true
                    pageLoadTime = System.currentTimeMillis()
                    val cookieManager = CookieManager.getInstance()
                    initialCookies = cookieManager.getCookie(baseUrl) ?: ""

                    // Start monitoring for cookie changes (indicates captcha completion)
                    startCookieMonitoring(view)
                }
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

    private fun startCookieMonitoring(webView: WebView?) {
        cookieCheckRunnable = object : Runnable {
            override fun run() {
                if (challengeCompleted) {
                    return
                }

                val cookieManager = CookieManager.getInstance()
                val currentCookies = cookieManager.getCookie(baseUrl) ?: ""
                val elapsedTime = System.currentTimeMillis() - pageLoadTime

                // Only check for completion after minimum wait time to avoid false positives during page load
                if (elapsedTime < minimumWaitTimeMs) {
                    Logger.printLog("Still in initial load period (${elapsedTime}ms elapsed), waiting...")
                    handler.postDelayed(this, 500)
                    return
                }

                // Check if cookies have changed meaningfully
                // Look for session cookie changes which indicate captcha completion
                val hasSessionCookie = currentCookies.contains("incap_ses_")
                val sessionCookieChanged = hasSessionCookie &&
                    !extractSessionCookie(initialCookies).equals(extractSessionCookie(currentCookies))

                if (currentCookies != initialCookies && currentCookies.isNotEmpty() && sessionCookieChanged) {
                    challengeCompleted = true
                    handler.removeCallbacks(this)

                    // Give a small delay for cookies to fully settle
                    handler.postDelayed({
                        completeCaptcha()
                    }, 500)
                } else {
                    // Continue checking every 500ms
                    handler.postDelayed(this, 500)
                }
            }
        }

        // Start checking after initial delay
        handler.postDelayed(cookieCheckRunnable!!, 1000)
    }

    private fun extractSessionCookie(cookies: String): String {
        // Extract the incap_ses cookie value for comparison
        val regex = Regex("incap_ses_\\d+_\\d+=[^;]+")
        return regex.find(cookies)?.value ?: ""
    }

    private fun checkForCompletionNow() {
        if (challengeCompleted) return

        val cookieManager = CookieManager.getInstance()
        val currentCookies = cookieManager.getCookie(baseUrl) ?: ""
        val elapsedTime = System.currentTimeMillis() - pageLoadTime

        // If navigation happens after minimum wait time, treat it as completion
        // The navigation itself is the signal that captcha was completed
        if (elapsedTime >= minimumWaitTimeMs && currentCookies.isNotEmpty()) {
            challengeCompleted = true
            cookieCheckRunnable?.let { handler.removeCallbacks(it) }

            // Give a small delay for any final cookies to settle
            handler.postDelayed({
                completeCaptcha()
            }, 500)
        } else if (elapsedTime < minimumWaitTimeMs) {
            Logger.printLog("Navigation too soon (${elapsedTime}ms < ${minimumWaitTimeMs}ms) - likely false positive")
        } else {
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
        // Stop cookie monitoring when dialog is destroyed
        cookieCheckRunnable?.let { handler.removeCallbacks(it) }

        val webView = view?.findViewById<WebView>(R.id.challengeWebView)
        webView?.destroy()
        super.onDestroyView()
    }
}