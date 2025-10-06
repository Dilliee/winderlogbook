package com.winderlogbook

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import com.winderlogbook.databinding.ActivityMainBinding
import com.winderlogbook.service.BiometricAuthService
import android.widget.Toast
import com.winderlogbook.database.WinderLogbookDatabase

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView
    private lateinit var biometricAuthService: BiometricAuthService
    private lateinit var winderLogbookInterface: WinderLogbookInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        biometricAuthService = BiometricAuthService(this)
        
        // Set up authentication callback
        biometricAuthService.setAuthenticationCallback { success, message ->
            if (success) {
                Toast.makeText(this, "✅ Authentication successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "❌ Authentication failed: $message", Toast.LENGTH_LONG).show()
            }
        }
        
        // Always initialize the app - authentication will be handled via JavaScript
        initializeApp()
    }


    private fun initializeApp() {
        setupWebView()
        loadWinderLogbookApp()
    }
    
    private fun setupWebView() {
        webView = binding.webView
        
        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        
        // Set up asset loader for local files
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
        
        // Add JavaScript interface for native communication
        winderLogbookInterface = WinderLogbookInterface(this)
        winderLogbookInterface.initializeBiometricService(this)
        webView.addJavascriptInterface(winderLogbookInterface, "WinderLogbook")
    }
    
    private fun loadWinderLogbookApp() {
        // Load the bundled React app
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
