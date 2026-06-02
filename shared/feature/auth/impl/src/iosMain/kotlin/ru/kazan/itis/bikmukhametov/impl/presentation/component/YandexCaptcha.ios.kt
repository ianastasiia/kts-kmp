package ru.kazan.itis.bikmukhametov.impl.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private const val AUTH_URL = "https://auth.smartbotpro.ru"
private const val JS_MESSAGE_HANDLER_NAME = "iOSCallback"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YandexCaptchaWidget(
    siteKey: String,
    modifier: Modifier,
    onToken: (String) -> Unit
) {
    val jsCallback =
        "if(window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers." +
                "iOSCallback) { window.webkit.messageHandlers.iOSCallback.postMessage(token); }"

    val configuration = remember {
        WKWebViewConfiguration().apply {
            preferences.javaScriptEnabled = true
            preferences.javaScriptCanOpenWindowsAutomatically = true

            userContentController.addScriptMessageHandler(
                scriptMessageHandler = TokenMessageHandler(onToken),
                name = JS_MESSAGE_HANDLER_NAME
            )
        }
    }

    val webView = remember(configuration) {
        WKWebView(frame = CGRectZero.readValue(), configuration = configuration).apply {
            opaque = false
            backgroundColor = platform.UIKit.UIColor.clearColor
        }
    }

    remember(siteKey) {
        webView.loadHTMLString(
            string = yandexCaptchaHtml(siteKey, jsCallback),
            baseURL = NSURL.URLWithString(AUTH_URL)
        )
    }

    UIKitView(
        factory = { webView },
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        update = { view ->
            view.setNeedsLayout()
            view.layoutIfNeeded()
        }
    )
}

private class TokenMessageHandler(
    private val onToken: (String) -> Unit
) : NSObject(), WKScriptMessageHandlerProtocol {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val token = didReceiveScriptMessage.body as? String
        if (!token.isNullOrEmpty()) {
            dispatch_async(dispatch_get_main_queue()) {
                onToken(token)
            }
        }
    }
}
