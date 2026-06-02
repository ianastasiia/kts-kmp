package ru.kazan.itis.bikmukhametov.impl.presentation.component

/**
 * HTML-страница для виджета Yandex Smart Captcha в WebView.
 * [siteKey] подставляется в data-sitekey контейнера.
 * [jsCallback] выполняется при получении токена, передаваеи токен в нативный колбэк платформ.
 */

internal fun yandexCaptchaHtml(siteKey: String, jsCallback: String): String = """
    <!DOCTYPE html>
    <html style="height: 100%; margin: 0; padding: 0; background: transparent;">
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <script src="https://smartcaptcha.yandexcloud.net/captcha.js" defer></script>
        <style>
            html, body {
                margin: 0;
                padding: 0;
                height: 100%;
                width: 100%;
                background-color: transparent !important;
                overflow: hidden;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            }
            #captcha-container {
                width: 100%;
                min-height: 120px;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 0;
                margin: 0;
            }
            .smart-captcha-frame {
                max-height: 110px !important;
            }
            .smart-captcha {
                overflow: hidden !important;
            }
        </style>
        <script>
            function onSmartCaptchaToken(token) {
                $jsCallback
            }
        </script>
    </head>
    <body>
        <div id="captcha-container"
             class="smart-captcha"
             data-sitekey="$siteKey"
             data-callback="onSmartCaptchaToken">
        </div>
    </body>
    </html>
""".trimIndent()
