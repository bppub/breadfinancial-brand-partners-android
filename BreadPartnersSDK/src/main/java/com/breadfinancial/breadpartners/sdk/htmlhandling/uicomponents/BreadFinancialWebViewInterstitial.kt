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
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
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
    private var showNavigationDialog = false

    /**
     * Replaces the given parent view with a WebView and loads the specified URL.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun replaceViewWithWebView(
        parent: ViewGroup, url: String, onPageLoadCompleted: (String) -> Unit
    ) {
        webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
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

                    val popup = HtmlPopupDisplayer(
                        html = modifiedHtml,
                        baseUrl = baseUrl,
                        onDismiss = {
                            // Handle popup closure
                        }
                    )
                    popup.show()


//
//                    val docsDir = java.io.File(context.cacheDir, "bread_docs").also { it.mkdirs() }
//                    // Use a fixed filename so it is always overwritten and never accumulates
//                    val tempFile = java.io.File(docsDir, "document.html")
//                    tempFile.writeText(modifiedHtml)
//                    val uri = androidx.core.content.FileProvider.getUriForFile(
//                        context,
//                        "${context.packageName}.breadpartners.fileprovider",
//                        tempFile
//                    )
//                    val intent = Intent(Intent.ACTION_VIEW).apply {
//                        setDataAndType(uri, "text/html")
//                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    }
//
//                    // Grant URI permission to all potential receivers
//                    val resInfoList = context.packageManager.queryIntentActivities(intent, 0)
//                    for (resolveInfo in resInfoList) {
//                        val packageName = resolveInfo.activityInfo.packageName
//                        context.grantUriPermission(
//                            packageName,
//                            uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        )
//                    }
//                    context.startActivity(intent)
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
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } catch (e: Exception) {
                callback(BreadPartnerEvent.SdkError(error = e))
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
            // Override window.open to intercept JS-driven navigation and HTML content popups
            if (!window.__breadWindowOpenOverridden__) {
                window.__breadWindowOpenOverridden__ = true;
                window.open = function(url, target, features) {
                    if (url && url !== '' && url !== 'about:blank') {
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

    /**
     * HTML Popup Displayer helper class
     * Converts HTML to PDF and displays it in a popup dialog
     *
     * Usage:
     *   val popup = HtmlPopupDisplayer(
     *       html = "<html>...</html>",
     *       baseUrl = "https://example.com",
     *       onDismiss = { /* Handle dismiss */ }
     *   )
     *   popup.show()
     */
    inner class HtmlPopupDisplayer(
        private val html: String,
        private val baseUrl: String = "",
        private val onDismiss: () -> Unit = {}
    ) {
        private var dialog: Dialog? = null
        private var pdfWebView: WebView? = null
        private var popupWebView: WebView? = null

        /**
         * Shows the HTML popup dialog with PDF content
         */
        fun show() {
            Handler(Looper.getMainLooper()).post {
                try {
                    convertHtmlToPdfAndShow()
                } catch (e: Exception) {
                    Logger.printLog("Error showing HTML popup: ${e.message}")
                }
            }
        }

        /**
         * Closes the popup dialog
         */
        fun dismiss() {
            Handler(Looper.getMainLooper()).post {
                dialog?.dismiss()
                cleanup()
            }
        }

        /**
         * Converts HTML to PDF and displays it in the popup
         */
        @SuppressLint("SetJavaScriptEnabled")
        private fun convertHtmlToPdfAndShow() {
            // Create a temporary WebView to render the HTML for PDF generation
            pdfWebView = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Logger.printLog("HTML content rendered for PDF conversion")

                        // Wait a bit for JavaScript to fully render
                        Handler(Looper.getMainLooper()).postDelayed({
                            createPdfViewerPopup(view)
                        }, 1500)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        val errorMsg = error?.description?.toString() ?: "Unknown error"
                        Logger.printLog("Error rendering HTML for PDF: $errorMsg")
                    }
                }

                // Load the HTML content
                val finalHtml = addBaseUrlToHtml(html, baseUrl)
                loadDataWithBaseURL(
                    baseUrl.ifEmpty { "https://localhost/" },
                    finalHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }

        /**
         * Creates the popup dialog with a WebView displaying the PDF content
         */
        @SuppressLint("SetJavaScriptEnabled")
        private fun createPdfViewerPopup(renderedWebView: WebView?) {
            // Create main container for header + PDF WebView
            val mainContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Create header with close button
            val headerContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(50)
                )
                setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
                gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.END
                setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
            }

            // Create close button
            val closeButton = Button(context).apply {
                text = "✕ Close"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
                setTextColor(android.graphics.Color.parseColor("#333333"))
                textSize = 14f
                setPadding(dpToPx(15), dpToPx(8), dpToPx(15), dpToPx(8))
            }

            headerContainer.addView(closeButton)

            // Create WebView for PDF display
            popupWebView = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Logger.printLog("PDF viewer loaded successfully")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        val errorMsg = error?.description?.toString() ?: "Unknown error"
                        Logger.printLog("Error loading PDF viewer: $errorMsg")
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: android.webkit.JsResult?
                    ): Boolean {
                        Logger.printLog("JS Alert: $message")
                        result?.confirm()
                        return true
                    }
                }

                // Display the HTML content as PDF using Google's PDF Viewer
                val finalHtml = addBaseUrlToHtml(html, baseUrl)

                // Alternative: Display HTML directly in WebView
                loadDataWithBaseURL(
                    baseUrl.ifEmpty { "https://localhost/" },
                    finalHtml,
                    "text/html",
                    "UTF-8",
                    null
                )

                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
            }

            mainContainer.addView(headerContainer)
            mainContainer.addView(popupWebView)

            // Create dialog
            dialog = Dialog(context, android.R.style.Theme_Material_Light_Dialog).apply {
                setTitle(null)
                setCancelable(true)
                setOnDismissListener {
                    onDismiss()
                    cleanup()
                }

                // Set the main container as the dialog content
                setContentView(mainContainer)

                // Configure dialog window size
                window?.apply {
                    val displayMetrics = context.resources.displayMetrics
                    val dialogWidth = displayMetrics.widthPixels
                    val dialogHeight = displayMetrics.heightPixels
                    setLayout(dialogWidth, dialogHeight)
                }

                show()
            }

            // Set up close button click listener
            closeButton.setOnClickListener {
                dismiss()
            }

            // Clean up the temporary rendering WebView
            Handler(Looper.getMainLooper()).postDelayed({
                pdfWebView?.destroy()
                pdfWebView = null
            }, 500)
        }

        /**
         * Adds base URL tag to HTML if not present
         */
        private fun addBaseUrlToHtml(htmlContent: String, url: String): String {
            return if (!htmlContent.contains("<base", ignoreCase = true) && url.isNotEmpty()) {
                htmlContent.replaceFirst(
                    "<head>",
                    "<head><base href=\"$url/\">",
                    ignoreCase = true
                )
            } else {
                htmlContent
            }
        }

        /**
         * Convert dp to pixels
         */
        private fun dpToPx(dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        /**
         * Cleanup resources
         */
        private fun cleanup() {
            Handler(Looper.getMainLooper()).post {
                pdfWebView?.apply {
                    stopLoading()
                    clearHistory()
                    destroy()
                }
                pdfWebView = null

                popupWebView?.apply {
                    stopLoading()
                    clearHistory()
                    destroy()
                }
                popupWebView = null
                dialog = null
            }
        }
    }
}