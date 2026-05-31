package ru.kazan.itis.bikmukhametov.analytics

interface AppAnalytics {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}
