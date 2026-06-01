package ru.kazan.itis.bikmukhametov.analytics

import io.github.aakira.napier.Napier

class NoOpAppAnalytics : AppAnalytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        Napier.d(tag = "AppAnalytics") { "event=$name params=$params" }
    }
}
