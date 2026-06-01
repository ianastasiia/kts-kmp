package ru.kazan.itis.bikmukhametov.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.kazan.itis.bikmukhametov.analytics.AppAnalytics
import ru.kazan.itis.bikmukhametov.analytics.NoOpAppAnalytics

actual fun analyticsPlatformModules(): List<Module> = listOf(
    module {
        single<AppAnalytics> { NoOpAppAnalytics() }
    },
)
