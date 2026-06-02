package ru.kazan.itis.bikmukhametov.impl.presentation.component

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val MIME_TYPE = "text/html"
private const val ENCODING = "UTF-8"
private const val AUTH_URL = "https://auth.smartbotpro.ru"

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun YandexCaptchaWidget(
    siteKey: String,
    modifier: Modifier,
    onToken: (String) -> Unit
) {

    val jsCallback =
        "if(window.AndroidCallback && typeof window.AndroidCallback.onToken === 'function') " +
                "{ AndroidCallback.onToken(token); }"

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onToken(token: String) {
                        post { onToken(token) }
                    }
                }, "AndroidCallback")

                webViewClient = WebViewClient()

                loadDataWithBaseURL(
                    AUTH_URL,
                    yandexCaptchaHtml(siteKey, jsCallback),
                    MIME_TYPE,
                    ENCODING,
                    null
                )
            }
        },
        update = { webView ->
            webView.requestLayout()
        }
    )
}
