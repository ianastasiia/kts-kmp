package ru.kazan.itis.bikmukhametov.chat.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ChatRoute : NavKey {
    @Serializable
    data class Chat(val conversationId: String) : ChatRoute
}
