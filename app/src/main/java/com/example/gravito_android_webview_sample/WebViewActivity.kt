package com.example.gravito_android_webview_sample

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true
        // Enable Mixed Content Mode
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Set WebViewClient to handle page navigation
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(this@WebViewActivity, "Failed to load URL: $description", Toast.LENGTH_SHORT).show()
            }
        }

        // Set WebChromeClient for additional features
        webView.webChromeClient = WebChromeClient()

        // Bind WebAppInterface to WebView
        val webAppInterface = WebAppInterface(this)
        webView.addJavascriptInterface(webAppInterface, "AndroidInterface")

        // Load a URL
        webView.loadUrl("http://172.18.0.152:5501/localServer/index.html?platform=android")
    }
}