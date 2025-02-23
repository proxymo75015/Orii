package com.origamilabs.orii.ui.main.help

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Activité qui affiche un site web dans une WebView.
 *
 * L'URL à charger est passée dans l'intent via l'extra "websiteUrl".
 */
class WebsiteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Création d'une WebView en code et définition comme vue principale
        val webView = WebView(this)
        setContentView(webView)

        // Configuration de la WebView
        webView.webViewClient = WebViewClient()
        webView.settings.apply {
            javaScriptEnabled = true
        }

        // Chargement de l'URL passée en extra (si non nulle)
        intent.getStringExtra("websiteUrl")?.let { url ->
            webView.loadUrl(url)
        }
    }
}
