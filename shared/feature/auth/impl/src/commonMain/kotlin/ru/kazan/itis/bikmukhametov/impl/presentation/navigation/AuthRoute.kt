package ru.kazan.itis.bikmukhametov.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthRoute : NavKey {
    @Serializable
    data object Login : AuthRoute
}
