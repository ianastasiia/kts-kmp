package ru.kazan.itis.bikmukhametov.main.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainRoute : NavKey {
    @Serializable
    data object Main : MainRoute
}
