package com.example.auroomcasino

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity() {
    private lateinit var casinoWeb: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Имплементация WebView
        casinoWeb = findViewById(R.id.casino_web)

        initWebView()
    }

    init {
        try {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                if (token != null) {

                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        //Ключи webView
        val webSettings: WebSettings = casinoWeb.getSettings()
        webSettings.javaScriptEnabled = true
        casinoWeb.loadUrl("https://auroombet.com/ru")

        // Огроничение для выхода в системный браузер
        casinoWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //для Работы в начном режиме
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(casinoWeb.settings, WebSettingsCompat.FORCE_DARK_OFF)
        }
    }
}