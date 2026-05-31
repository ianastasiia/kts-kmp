package ru.kazan.itis.bikmukhametov.profile.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ProfileRoute : NavKey {
    @Serializable
    data object Profile : ProfileRoute
}
