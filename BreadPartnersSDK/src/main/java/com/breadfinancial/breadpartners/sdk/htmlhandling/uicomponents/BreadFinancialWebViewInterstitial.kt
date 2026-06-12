//------------------------------------------------------------------------------
//  File:          BreadFinancialWebViewInterstitial.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("UNUSED_PARAMETER", "unused", "SpellCheckingInspection")

package com.breadfinancial.breadpartners.sdk.htmlhandling.uicomponents

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.OfferResponse
import com.breadfinancial.breadpartners.sdk.utilities.Logger
import org.json.JSONObject

/**
 * Manages WebView interactions and events within the SDK.
 */
internal class BreadFinancialWebViewInterstitial(
    private val context: Context,
    private val callback: (BreadPartnerEvent) -> Unit?
) {
    private var webView: WebView? = null
    private var popupWebView: WebView? = null
    private var webViewParent: ViewGroup? = null
    private var showNavigationDialog = false

    /**
     * Replaces the given parent view with a WebView and loads the specified URL.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun replaceViewWithWebView(
        parent: ViewGroup, url: String, onPageLoadCompleted: (String) -> Unit
    ) {
        webViewParent = parent
        webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                // Only enable remote debugging in debug builds
                setWebContentsDebuggingEnabled(
                    android.os.Build.TYPE == "eng" ||
                            android.provider.Settings.Global.getInt(
                                context.contentResolver,
                                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                            ) != 0
                )
            }

            Logger.logLoadingURL(url = url)
            webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    injectAnchorInterceptorScript(view)
                    injectFocusOutlineRemoval(view)

                    url?.let {
                        onPageLoadCompleted(it)
                    }
                }

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    error?.let {
                        onPageLoadCompleted(it.toString())
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message
                ): Boolean {
                    Log.d("BreadPartnersSDK", "┌─── onCreateWindow ────────────────────────────")
                    Log.d("BreadPartnersSDK", "│ isDialog=$isDialog  isUserGesture=$isUserGesture")
                    Log.d("BreadPartnersSDK", "│ Parent WebView URL: ${view.url}")
                    Log.d("BreadPartnersSDK", "│ resultMsg target: ${resultMsg.target?.javaClass?.simpleName}")
                    Log.d("BreadPartnersSDK", "│ Creating child WebView for provisioning popup…")

                    val popupView = WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            // Build a Chrome-like UA by stripping the markers that
                            // Google's provisioning server uses to block WebView clients:
                            //   "; wv)"       → the explicit WebView flag
                            //   "Version/X.X" → present in WebView, absent in Chrome
                            val rawUA = view.settings.userAgentString
                            val spoofedUA = rawUA
                                .replace(Regex(";\\s*wv\\)"), ")")
                                .replace(Regex("\\s+Version/[0-9.]+\\s+"), " ")
                            userAgentString = spoofedUA
                            Log.d("BreadPartnersSDK", "│ Popup UA original : $rawUA")
                            Log.d("BreadPartnersSDK", "│ Popup UA spoofed  : $spoofedUA")
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                Log.d("BreadPartnersSDK", "│ Popup onPageStarted: $url")
                            }

                            // Log the actual outgoing headers on every request to the
                            // provisioning endpoint (confirms Referer/Origin are sent).
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                val urlStr = request?.url?.toString() ?: return null
                                if (urlStr.contains("pushprovisioning")) {
                                    Log.d("BreadPartnersSDK", "│ Popup shouldInterceptRequest: $urlStr")
                                    Log.d("BreadPartnersSDK", "│   outgoing headers: ${request.requestHeaders}")
                                }
                                return null
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                val urlStr = request.url.toString()
                                val scheme = request.url.scheme?.lowercase() ?: ""
                                Log.d("BreadPartnersSDK", "│ Popup shouldOverrideUrlLoading")
                                Log.d("BreadPartnersSDK", "│   url=$urlStr")
                                Log.d("BreadPartnersSDK", "│   scheme=$scheme  isRedirect=${request.isRedirect}  method=${request.method}")
                                Log.d("BreadPartnersSDK", "│   requestHeaders=${request.requestHeaders}")

                                // Google's provisioning server validates the HTTP Referer header
                                // against the origin parameter.  A standalone WebView popup sends
                                // no Referer automatically, which triggers a 400 Bad Request.
                                // Intercept the first load and re-issue it with the correct headers.
                                if (urlStr.contains("pushprovisioning") &&
                                    !request.requestHeaders.containsKey("Referer")
                                ) {
                                    val originParam = request.url.getQueryParameter("origin") ?: ""
                                    Log.d("BreadPartnersSDK", "│   Provisioning URL — origin param: '$originParam'")
                                    if (originParam.isNotEmpty()) {
                                        val headers = mapOf(
                                            "Referer" to originParam,
                                            "Origin"  to originParam
                                        )
                                        Log.d("BreadPartnersSDK", "│   Re-loading with injected headers: $headers")
                                        view.loadUrl(urlStr, headers)
                                        return true
                                    }
                                }

                                // Handle intent:// or other deep links the provisioning
                                // frame might fire (e.g. launching Google Wallet natively).
                                if (scheme != "https" && scheme != "http") {
                                    Log.d("BreadPartnersSDK", "│   Non-http scheme — attempting Intent.parseUri")
                                    try {
                                        val intent = Intent.parseUri(
                                            request.url.toString(),
                                            Intent.URI_INTENT_SCHEME
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        Log.d("BreadPartnersSDK", "│   Launching intent: ${intent.`package`} / ${intent.action}")
                                        context.startActivity(intent)
                                        Log.d("BreadPartnersSDK", "│   Intent launched successfully")
                                    } catch (e: ActivityNotFoundException) {
                                        Log.w("BreadPartnersSDK", "│   ActivityNotFoundException for scheme '$scheme': ${e.message}")
                                    } catch (e: Exception) {
                                        Log.w("BreadPartnersSDK", "│   Exception for scheme '$scheme': ${e.javaClass.simpleName}: ${e.message}")
                                    }
                                    return true
                                }
                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                Log.d("BreadPartnersSDK", "│ Popup onPageFinished: $url")
                                Log.d("BreadPartnersSDK", "│   title='${view?.title}'")
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                Log.w("BreadPartnersSDK", "│ Popup onReceivedError")
                                Log.w("BreadPartnersSDK", "│   url=${request?.url}")
                                Log.w("BreadPartnersSDK", "│   isForMainFrame=${request?.isForMainFrame}")
                                Log.w("BreadPartnersSDK", "│   errorCode=${error?.errorCode}  description='${error?.description}'")
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: android.webkit.WebResourceResponse?
                            ) {
                                val statusCode = errorResponse?.statusCode ?: 0
                                Log.w("BreadPartnersSDK", "│ Popup onReceivedHttpError")
                                Log.w("BreadPartnersSDK", "│   url=${request?.url}")
                                Log.w("BreadPartnersSDK", "│   isForMainFrame=${request?.isForMainFrame}")
                                Log.w("BreadPartnersSDK", "│   statusCode=$statusCode  reason='${errorResponse?.reasonPhrase}'")

                                // HTTP 400 on the provisioning frame = Google's server-side
                                // enforcement: web push provisioning is intentionally blocked
                                // from Android WebView regardless of UA or headers.
                                // The correct path is the native TapAndPay API.
                                //
                                // 1. Fire PushProvisionGoogleWallet so the host app knows.
                                // 2. Notify the parent WebView via a JS custom event so an
                                //    updated issuer page can call
                                //    window.BreadPartnersSDK.provisionToGoogleWallet(opc,…)
                                if (request?.isForMainFrame == true &&
                                    statusCode == 400 &&
                                    request.url.toString().contains("pushprovisioning")
                                ) {
                                    val provUrl = request.url.toString()
                                    Log.w("BreadPartnersSDK", "│   Provisioning frame blocked (400) — web push provisioning not supported in Android WebView")
                                    Log.w("BreadPartnersSDK", "│   ACTION REQUIRED: issuer page must call window.BreadPartnersSDK.provisionToGoogleWallet(opc,…) when isAndroid=true")

                                    // Notify the parent WebView so the issuer's JS can react.
                                    Handler(Looper.getMainLooper()).post {
                                        webView?.evaluateJavascript(
                                            """
                                            (function(){
                                                try {
                                                    window.dispatchEvent(new CustomEvent(
                                                        'BreadPartnersSDK_provisioningBlocked',
                                                        { detail: { reason: 'ANDROID_WEBVIEW_NOT_SUPPORTED', url: '${provUrl.replace("'", "\\'")}' } }
                                                    ));
                                                } catch(e) {}
                                            })();
                                            """.trimIndent(), null
                                        )
                                        callback(
                                            BreadPartnerEvent.PushProvisionGoogleWallet(
                                                provisioningUrl = provUrl
                                            )
                                        )
                                    }
                                }
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: android.webkit.SslErrorHandler?,
                                error: android.net.http.SslError?
                            ) {
                                Log.w("BreadPartnersSDK", "│ Popup onReceivedSslError: primaryError=${error?.primaryError}  url=${error?.url}")
                                handler?.cancel()
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                val level = consoleMessage?.messageLevel()?.name ?: "?"
                                val msg = consoleMessage?.message() ?: ""
                                val src = consoleMessage?.sourceId() ?: ""
                                val line = consoleMessage?.lineNumber() ?: 0
                                Log.d("BreadPartnersSDK", "│ Popup console[$level] $msg  ($src:$line)")
                                return true
                            }

                            override fun onCloseWindow(window: WebView) {
                                Log.d("BreadPartnersSDK", "│ Popup onCloseWindow — removing child WebView")
                                webViewParent?.removeView(popupWebView)
                                popupWebView?.destroy()
                                popupWebView = null
                                Log.d("BreadPartnersSDK", "└─── onCloseWindow done ─────────────────────────")
                            }
                        }
                    }
                    popupWebView = popupView
                    // Add 1×1 invisible overlay so the WebView is attached and
                    // can load the provisioning frame and exchange postMessages.
                    webViewParent?.addView(popupView, ViewGroup.LayoutParams(1, 1))
                    Log.d("BreadPartnersSDK", "│ Child WebView added to parent layout (1×1)")

                    val transport = resultMsg.obj as WebView.WebViewTransport
                    transport.webView = popupView
                    resultMsg.sendToTarget()
                    Log.d("BreadPartnersSDK", "│ WebViewTransport sent — popup WebView is active")
                    Log.d("BreadPartnersSDK", "└───────────────────────────────────────────────")
                    return true
                }

                override fun onCloseWindow(window: WebView) {
                    Log.d("BreadPartnersSDK", "Main window closed (onCloseWindow)")
                }

                override fun onJsBeforeUnload(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: android.webkit.JsResult
                ): Boolean {
                    if (showNavigationDialog) {
                        showNavigationDialog = false
                        return false // let the system show the native dialog
                    }
                    result.confirm()
                    return true
                }
            }

            addJavascriptInterface(WebAppInterface(this), "Android")

            loadUrl(url)
        }

        parent.addView(webView)
    }

    fun destroyWebView() {
        webView?.let {
            Handler(Looper.getMainLooper()).post {
                popupWebView?.destroy()
                popupWebView = null
                webViewParent = null
                it.destroy()
                webView = null
            }
        }
    }

    /**
     * Interface to handle messages sent from JavaScript running in the WebView.
     */
    private inner class WebAppInterface(webView: WebView) {

        @JavascriptInterface
        fun onAppRestartClicked(url: String) {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase()
            if (scheme == "https" || scheme == "http") {
                listener?.onAppRestartClicked(url)
            } else {
                callback(BreadPartnerEvent.OnSDKEventLog("onAppRestartClicked blocked unsafe URL scheme: $scheme"))
            }
        }

        @JavascriptInterface
        fun logAnchorTags(data: String) {
//            Logger().printWebAnchorLogs(data)
        }

        @JavascriptInterface
        fun openHtmlContent(html: String) {
            Handler(Looper.getMainLooper()).post {
                try {
                    // Extract the base URL from the current WebView URL
                    val baseUrl = webView?.url?.let { url ->
                        val uri = Uri.parse(url)
                        "${uri.scheme}://${uri.host}"
                    } ?: run {
                        callback(BreadPartnerEvent.SdkError(error = Exception("openHtmlContent: unable to resolve base URL – WebView URL is null")))
                        return@post
                    }

                    // Add base tag to HTML if not present to resolve relative URLs
                    val modifiedHtml = if (!html.contains("<base", ignoreCase = true)) {
                        html.replaceFirst(
                            "<head>",
                            "<head><base href=\"$baseUrl/\">",
                            ignoreCase = true
                        )
                    } else {
                        html
                    }

                    val pdfWebView = WebView(context)
                        .apply {
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL(null, modifiedHtml, "text/html", "UTF-8", null)
                        }

                    // Extract title from HTML
                    val titleMatch =
                        Regex("<title[^>]*>([^<]*)</title>", RegexOption.IGNORE_CASE).find(
                            modifiedHtml
                        )
                    val pageTitle = titleMatch?.groupValues?.get(1) ?: "Disclosure"

                    val printManager =
                        context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val printAdapter =
                        pdfWebView.createPrintDocumentAdapter(pageTitle)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build()
                    printManager.print(
                        pageTitle,
                        printAdapter,
                        printAttributes
                    )

                } catch (e: Exception) {
                    callback(BreadPartnerEvent.SdkError(error = e))
                }
            }
        }

        @JavascriptInterface
        fun openExternally(url: String) {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase()
            if (scheme != "https" && scheme != "http") {
                callback(BreadPartnerEvent.OnSDKEventLog("openExternally blocked unsafe URL scheme: $scheme"))
                return
            }

            // Intercept Google Pay push provisioning URLs.
            // A standard WebView/browser cannot host the push provisioning flow
            // because Google Wallet requires secure app-to-app communication.
            // We try to deep-link directly into the Google Wallet app first; if the
            // app is not installed we surface a PushProvisionGoogleWallet event so
            // the host application can handle it (e.g. redirect to Play Store).
            if (isGooglePayPushProvisioningUrl(uri)) {
                handleGooglePayPushProvisioning(uri)
                return
            }

            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } catch (e: Exception) {
                callback(BreadPartnerEvent.SdkError(error = e))
            }
        }

        /**
         * JavaScript bridge called by the web page when it has issuer-supplied card
         * provisioning data and wants native Android code to complete the Google
         * Wallet push provisioning flow.
         *
         * Expected JSON shape:
         * {
         *   "opc":                  "<base64-encoded OPC from issuer server>",
         *   "cardNetwork":          "VISA" | "MASTERCARD" | …,
         *   "lastDigits":           "1234",
         *   "tokenServiceProvider": "VISA" | "MASTERCARD" | …,
         *   "displayName":          "My Visa Card"
         * }
         *
         * The host app receives a [BreadPartnerEvent.PushProvisionGoogleWallet] event
         * and is responsible for calling TapAndPayClient.pushTokenize() with the data.
         *
         * Web-page usage (after checking window.BreadPartnersSDK.isAndroid):
         *   window.BreadPartnersSDK.provisionToGoogleWallet({ opc, cardNetwork, … });
         */
        @JavascriptInterface
        fun provisionToGoogleWallet(cardDataJson: String) {
            Handler(Looper.getMainLooper()).post {
                try {
                    val payload = org.json.JSONObject(cardDataJson)
                    callback(
                        BreadPartnerEvent.PushProvisionGoogleWallet(
                            opc = payload.optString("opc").takeIf { it.isNotBlank() },
                            cardNetwork = payload.optString("cardNetwork").takeIf { it.isNotBlank() },
                            lastDigits = payload.optString("lastDigits").takeIf { it.isNotBlank() },
                            tokenServiceProvider = payload.optString("tokenServiceProvider").takeIf { it.isNotBlank() },
                            displayName = payload.optString("displayName").takeIf { it.isNotBlank() }
                        )
                    )
                } catch (e: Exception) {
                    callback(BreadPartnerEvent.SdkError(error = e))
                }
            }
        }

        @JavascriptInterface
        fun postMessage(message: String) {
            Log.d("BreadPartnersSDK:", "WebViewMessage: $message")

            try {
                if (message == "undefined") {
                    return
                }
                val parsedData = JSONObject(message)
                val action = parsedData.optJSONObject("action")
                val type = action?.optString("type")

                when (type) {
                    "HEIGHT_CHANGED" -> {
                        // Handle height change if needed
                    }

                    "LOAD_ADOBE_TRACKING_ID" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            val adobeTrackingId = payload.optString("adobeTrackingId")
                            Logger.printLog("BreadPartnersSDK: AdobeTrackingID: $adobeTrackingId")
                        }
                    }

                    "VIEW_PAGE" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            val pageName = payload.optString("pageName")
                            callback(BreadPartnerEvent.ScreenName(name = pageName))
                        }
                    }

                    "CANCEL_APPLICATION" -> {
                        callback(BreadPartnerEvent.PopupClosed)
                    }

                    "SUBMIT_APPLICATION" -> {
                        callback(BreadPartnerEvent.ScreenName(name = "submit-application"))
                    }

                    "RECEIVE_APPLICATION_RESULT" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.WebViewSuccess(result = payload))
                        }
                    }

                    "RECEIVE_PRESCREEN_APPLICATION_RESULT" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.WebViewSuccess(result = payload))
                        }
                    }

                    "UNIFIED_OFFERS_RECEIVED" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.WebViewSuccess(result = payload))
                            callback(BreadPartnerEvent.UnifiedOffersReceived(result = payload))
                        }
                    }

                    "RECEIVE_PREQUAL_APPLICATION_RESULT" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.WebViewSuccess(result = payload))
                            callback(BreadPartnerEvent.ReceivePrequalApplicationResult(result = payload))
                        }
                    }

                    "RECEIVE_UNIFIED_CHECKOUT_APPLICATION_RESULT" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.WebViewSuccess(result = payload))
                            callback(
                                BreadPartnerEvent.ReceiveUnifiedCheckoutApplicationResult(
                                    result = payload
                                )
                            )
                            callback(BreadPartnerEvent.PopupClosed)
                        }
                    }

                    "SUBMIT_PREQUAL_APPLICATION" -> {
                        callback(BreadPartnerEvent.SubmitPrequalApplication)
                    }

                    "APPLICATION_COMPLETED" -> {
                        callback(BreadPartnerEvent.ScreenName(name = "application-completed"))
                        callback(BreadPartnerEvent.ApplicationCompleted)
                        callback(BreadPartnerEvent.PopupClosed)
                    }

                    "LOG_OUT_OR_RESTART" -> {
                        showNavigationDialog = true
                    }

                    "OFFER_RESPONSE" -> {
                        val payload = action.optString("payload")
                        val offerResponse = OfferResponse.fromValue(payload)
                        if (offerResponse != null) {
                            callback(BreadPartnerEvent.OfferResponse(offerResponse))
                            if (offerResponse == OfferResponse.NO || offerResponse == OfferResponse.NOT_ME) {
                                callback(BreadPartnerEvent.PopupClosed)
                            }
                        }
                    }

                    "RECEIVE_ACCOUNT_EXISTS" -> {
                        action.optJSONObject("payload")?.let { payload ->
                            Logger.logApplicationResultDetails(payload)
                            callback(BreadPartnerEvent.ReceiveAccountExists(result = payload))
                        }
                    }

                    else -> {
                        callback(BreadPartnerEvent.OnSDKEventLog(message))
                    }
                }
            } catch (e: Exception) {
                callback(BreadPartnerEvent.SdkError(error = e))
            }
        }
    }

    /**
     * Returns true if the URI points to a Google Pay push provisioning frame.
     * These URLs cannot be rendered in a standard WebView or browser — they
     * require the Google Wallet app's secure provisioning context.
     */
    private fun isGooglePayPushProvisioningUrl(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        val path = uri.path?.lowercase() ?: return false
        return (host == "pay.google.com" || host == "pay.sandbox.google.com") &&
                path.contains("pushprovisioning")
    }

    /**
     * Handles a Google Pay push provisioning URL that was intercepted from
     * the WebView's window.open call.
     *
     * Strategy:
     *  1. Check if Google Wallet is installed via PackageManager.
     *  2. If installed, try to launch it directly using ACTION_VIEW + setPackage.
     *     Google Wallet's internal WebView has the Google Play Services bridge
     *     required for push provisioning to succeed.
     *  3. If setPackage fails (activity not found in Wallet), fall back to a
     *     generic ACTION_VIEW so the OS can route to any capable app (e.g.,
     *     Chrome, which may handle pay.google.com verified links).
     *  4. If no handler exists — or Wallet is not installed at all — surface a
     *     [BreadPartnerEvent.PushProvisionGoogleWallet] event so the host app
     *     can redirect the user to the Play Store or apply another fallback.
     */
    private fun handleGooglePayPushProvisioning(uri: Uri) {
        val googleWalletPackage = "com.google.android.apps.walletnfcrel"

        Log.d("BreadPartnersSDK", "=== Google Pay Push Provisioning Debug ===")
        Log.d("BreadPartnersSDK", "Target URI: $uri")
        Log.d("BreadPartnersSDK", "URI scheme: ${uri.scheme}, host: ${uri.host}, path: ${uri.path}")
        Log.d("BreadPartnersSDK", "Looking up package: '$googleWalletPackage'")

        // Enumerate all installed packages whose name contains "wallet" or "pay"
        // to identify the real package name of the installed Wallet app.
        try {
            @Suppress("DEPRECATION")
            val allPackages = context.packageManager.getInstalledPackages(0)
            val walletRelated = allPackages.filter { pkg ->
                pkg.packageName.contains("wallet", ignoreCase = true) ||
                        pkg.packageName.contains("tapandpay", ignoreCase = true) ||
                        (pkg.packageName.contains("google", ignoreCase = true) &&
                                pkg.packageName.contains("pay", ignoreCase = true))
            }
            if (walletRelated.isEmpty()) {
                Log.d("BreadPartnersSDK", "DEBUG: No wallet/pay-related packages found on device")
            } else {
                Log.d("BreadPartnersSDK", "DEBUG: Installed wallet/pay-related packages (${walletRelated.size}):")
                walletRelated.forEach { pkg ->
                    @Suppress("DEPRECATION")
                    val versionCode = pkg.versionCode
                    Log.d("BreadPartnersSDK", "  → ${pkg.packageName}  versionName=${pkg.versionName}  versionCode=$versionCode")
                }
            }
        } catch (e: Exception) {
            Log.w("BreadPartnersSDK", "DEBUG: Failed to enumerate packages: ${e.message}")
        }

        // Log what activities the OS can resolve for this URI (no package filter).
        try {
            val probeIntent = Intent(Intent.ACTION_VIEW, uri)
            @Suppress("DEPRECATION")
            val resolveInfoList = context.packageManager.queryIntentActivities(probeIntent, 0)
            if (resolveInfoList.isEmpty()) {
                Log.d("BreadPartnersSDK", "DEBUG: No activities resolved for URI (no package filter)")
            } else {
                Log.d("BreadPartnersSDK", "DEBUG: Activities that can handle the URI (${resolveInfoList.size}):")
                resolveInfoList.forEach { info ->
                    Log.d("BreadPartnersSDK", "  → ${info.activityInfo.packageName} / ${info.activityInfo.name}")
                }
            }
        } catch (e: Exception) {
            Log.w("BreadPartnersSDK", "DEBUG: Failed to query intent activities: ${e.message}")
        }

        // Step 1 — Check installation (requires <queries> in the consuming
        // app's manifest; the SDK's manifest already declares this package).
        val isWalletInstalled = try {
            val pkgInfo = context.packageManager.getPackageInfo(googleWalletPackage, 0)
            Log.d("BreadPartnersSDK", "DEBUG: getPackageInfo SUCCESS for '$googleWalletPackage': versionName=${pkgInfo.versionName}")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("BreadPartnersSDK", "DEBUG: getPackageInfo FAILED for '$googleWalletPackage': NameNotFoundException — ${e.message}")
            false
        } catch (e: Exception) {
            Log.w("BreadPartnersSDK", "DEBUG: getPackageInfo FAILED for '$googleWalletPackage': ${e.javaClass.simpleName} — ${e.message}")
            false
        }

        Log.d("BreadPartnersSDK", "isWalletInstalled=$isWalletInstalled for package '$googleWalletPackage'")

        if (isWalletInstalled) {
            // Step 2 — Targeted intent: open inside Google Wallet's own browser.
            val walletIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(googleWalletPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Log what activities Wallet itself exposes for this URI.
            try {
                @Suppress("DEPRECATION")
                val walletResolve = context.packageManager.queryIntentActivities(walletIntent, 0)
                Log.d("BreadPartnersSDK", "DEBUG: Activities in '$googleWalletPackage' that handle the URI (${walletResolve.size}):")
                walletResolve.forEach { info ->
                    Log.d("BreadPartnersSDK", "  → ${info.activityInfo.name}")
                }
                if (walletResolve.isEmpty()) {
                    Log.d("BreadPartnersSDK", "DEBUG: Wallet package has NO activity registered for this URI — targeted intent will fail")
                }
            } catch (e: Exception) {
                Log.w("BreadPartnersSDK", "DEBUG: Failed to query wallet activities: ${e.message}")
            }

            try {
                context.startActivity(walletIntent)
                Logger.printLog("BreadPartnersSDK: Google Pay push provisioning handed off to Google Wallet app")
                return
            } catch (e: ActivityNotFoundException) {
                Log.w("BreadPartnersSDK", "DEBUG: startActivity with setPackage threw ActivityNotFoundException: ${e.message}")
                Logger.printLog("BreadPartnersSDK: Google Wallet installed but its activity could not handle the URL directly; trying generic intent")
            } catch (e: Exception) {
                Log.w("BreadPartnersSDK", "DEBUG: startActivity with setPackage threw ${e.javaClass.simpleName}: ${e.message}")
                Handler(Looper.getMainLooper()).post { callback(BreadPartnerEvent.SdkError(error = e)) }
                return
            }

            // Step 3 — Generic fallback: let the OS route to Chrome or another
            // registered verified-link handler (e.g. pay.google.com domain).
            val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(genericIntent)
                Logger.printLog("BreadPartnersSDK: Google Pay push provisioning handed off via generic system intent")
                return
            } catch (e: ActivityNotFoundException) {
                Log.w("BreadPartnersSDK", "DEBUG: generic startActivity threw ActivityNotFoundException: ${e.message}")
                Logger.printLog("BreadPartnersSDK: No app could handle the Google Pay provisioning URL; firing PushProvisionGoogleWallet event")
            } catch (e: Exception) {
                Log.w("BreadPartnersSDK", "DEBUG: generic startActivity threw ${e.javaClass.simpleName}: ${e.message}")
                Handler(Looper.getMainLooper()).post { callback(BreadPartnerEvent.SdkError(error = e)) }
                return
            }
        } else {
            Log.d("BreadPartnersSDK", "DEBUG: Wallet package NOT found by PackageManager — check <queries> in manifest and package name spelling")
            Logger.printLog("BreadPartnersSDK: Google Wallet app not found, firing PushProvisionGoogleWallet event")
        }

        // Step 4 — Surface the event on the main thread so the host app can
        // prompt the user to install Google Wallet from the Play Store, or
        // apply any other platform-appropriate fallback.
        Handler(Looper.getMainLooper()).post {
            callback(
                BreadPartnerEvent.PushProvisionGoogleWallet(
                    provisioningUrl = uri.toString()
                )
            )
        }
    }

    /// This is to remove default focus outlines that some browsers add when elements are clicked,
    /// which can interfere with the visual design of the WebView content.
    /// By injecting this CSS, we ensure a cleaner look without unexpected outlines.
    private fun injectFocusOutlineRemoval(view: WebView?) {
        view?.evaluateJavascript(
            """
            (function() {
                var style = document.createElement('style');
                style.textContent = '* { outline: none !important; }';
                document.head.appendChild(style);
            })();
            """.trimIndent(), null
        )
    }

    private fun injectAnchorInterceptorScript(view: WebView?) {
        view?.evaluateJavascript(
            """
        (function() {
            // ── BreadPartnersSDK Native Bridge ────────────────────────────────────────
            // Expose a stable namespace that the web page can use to detect it is
            // running inside the native Android SDK and to call native methods.
            //
            // Push provisioning usage (web page side):
            //   if (window.BreadPartnersSDK && window.BreadPartnersSDK.isAndroid) {
            //       window.BreadPartnersSDK.provisionToGoogleWallet({
            //           opc:                  "<base64 OPC from issuer>",
            //           cardNetwork:          "VISA",
            //           lastDigits:           "1234",
            //           tokenServiceProvider: "VISA",
            //           displayName:          "My Visa Card"
            //       });
            //   }
            if (!window.BreadPartnersSDK) {
                window.BreadPartnersSDK = {
                    isAndroid: true,
                    provisionToGoogleWallet: function(cardData) {
                        try {
                            var json = (typeof cardData === 'string')
                                ? cardData
                                : JSON.stringify(cardData);
                            window.Android.provisionToGoogleWallet(json);
                        } catch(e) {
                            console.error('BreadPartnersSDK.provisionToGoogleWallet error: ' + e);
                        }
                    }
                };
            }

            // Override window.open to intercept JS-driven navigation and HTML content popups
            if (!window.__breadWindowOpenOverridden__) {
                window.__breadWindowOpenOverridden__ = true;
                // Keep a reference to the real browser window.open so we can delegate
                // special popups (e.g. Google Pay provisioning frame) back to the native
                // WebChromeClient.onCreateWindow, which keeps the postMessage channel alive.
                var _nativeWindowOpen = window.open.bind(window);
                window.open = function(url, target, features) {
                    if (url && url !== '' && url !== 'about:blank') {
                        // Google Pay push provisioning frame MUST go through native
                        // onCreateWindow so the parent page can receive its postMessage
                        // response.  Do NOT intercept these URLs.
                        if (url.indexOf('pushprovisioning') !== -1) {
                            return _nativeWindowOpen(url, target, features);
                        }
                        window.Android.openExternally(url);
                        return null;
                    }
                    // about:blank or empty url: the page will call document.write() or
                    // set innerHTML on the returned window to inject HTML content
                    // (e.g. a loan agreement). Capture that content and send it to
                    // Android to open in a browser.
                    var _content = '';
                    function sendContent() {
                        if (_content) {
                            window.Android.openHtmlContent(_content);
                            _content = '';
                        }
                    }
                    var fakeBody = {};
                    Object.defineProperty(fakeBody, 'innerHTML', {
                        set: function(html) { _content = html; sendContent(); },
                        get: function() { return _content; }
                    });
                    var fakeDoc = {
                        write: function(html) { _content += html; },
                        writeln: function(html) { _content += html + '\n'; },
                        close: function() { sendContent(); },
                        body: fakeBody
                    };
                    return { document: fakeDoc, focus: function() {}, close: function() {} };
                };
            }

            function isVisible(elem) {
                return !!(elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length);
            }        
            function handleAnchors() {
                const anchors = document.querySelectorAll('a[target="_blank"], a[data-open-externally="true"]');
                const anchorsHTML = Array.from(anchors).map(a => a.outerHTML);
                
                if (anchorsHTML.length > 0) {
                    window.Android.logAnchorTags('AnchorTags:\n' + anchorsHTML.join('\n'));
                } else {
                    window.Android.logAnchorTags('AnchorTags:No anchors found with target="_blank" or data-open-externally="true"');
                }

                anchors.forEach(a => {
                    // Prevent attaching multiple listeners
                    if (!a.__handled__) {
                        a.__handled__ = true;
                        a.addEventListener('click', function(event) {
                            event.preventDefault();
                            window.Android.openExternally(a.href);
                        });
                    }
                });
            }

            function handleRestartButton() {
                const btn = document.querySelector('#appRestart');
                if (btn && isVisible(btn)) {
                    if (!btn.__handled__) {
                        btn.__handled__ = true;
                        btn.addEventListener('click', function(event) {
                            event.preventDefault();
                            if (btn.href) {
                                window.Android.onAppRestartClicked(btn.href);
                            }
                        });
                    }
                }
            }


            // Initial run
            handleAnchors();
            handleRestartButton();
            
            // Watch for DOM changes
            const observer = new MutationObserver(function(mutations) {
                let shouldHandle = false;
                for (const mutation of mutations) {
                    if (mutation.addedNodes.length) {
                        shouldHandle = true;
                        break;
                    }
                }
                if (shouldHandle) {
                    handleAnchors();
                    handleRestartButton();
                }
            });

            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        })();
        """.trimIndent(), null
        )
    }

    interface WebViewRestartButtonListener {
        fun onAppRestartClicked(url: String)
    }

    private var listener: WebViewRestartButtonListener? = null

    fun setOnAppRestartListener(listener: WebViewRestartButtonListener) {
        this.listener = listener
    }
}