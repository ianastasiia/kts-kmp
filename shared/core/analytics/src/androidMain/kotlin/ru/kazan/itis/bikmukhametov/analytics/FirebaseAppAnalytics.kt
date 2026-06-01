package ru.kazan.itis.bikmukhametov.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseAppAnalytics(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AppAnalytics {

    override fun logEvent(name: String, params: Map<String, String>) {
        if (params.isEmpty()) {
            firebaseAnalytics.logEvent(name, null)
            return
        }
        val bundle = Bundle(params.size).apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }
}

fun initFirebaseCrashlytics() {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
}
