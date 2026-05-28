package ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface InterlocutorInfoRoute : NavKey {
    @Serializable
    data class Details(
        val conversationId: String,
        val interlocutorName: String = "",
        val channelKind: String = "",
        val channelName: String = "",
        val chatId: String = "",
        val userId: String = "",
    ) : InterlocutorInfoRoute
}
