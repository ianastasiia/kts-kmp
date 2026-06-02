package ru.kazan.itis.bikmukhametov.analytics.di

import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.kazan.itis.bikmukhametov.analytics.AppAnalytics
import ru.kazan.itis.bikmukhametov.analytics.FirebaseAppAnalytics
actual fun analyticsPlatformModules(): List<Module> = listOf(
    module {
        single { FirebaseAnalytics.getInstance(androidContext()) }
        single<AppAnalytics> { FirebaseAppAnalytics(get()) }
    },
)
