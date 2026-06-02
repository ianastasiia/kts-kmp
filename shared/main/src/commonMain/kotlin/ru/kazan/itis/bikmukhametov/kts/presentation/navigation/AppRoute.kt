package ru.kazan.itis.bikmukhametov.kts.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Onboarding : AppRoute
}
