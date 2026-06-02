package ru.kazan.itis.bikmukhametov.kts

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ru.kazan.itis.bikmukhametov.kts.presentation.App
import ru.kazan.itis.bikmukhametov.kts.presentation.di.initKoin

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
