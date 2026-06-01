package ru.kazan.itis.bikmukhametov.kts.presentation.navigation

import androidx.navigation3.runtime.NavKey
import ru.kazan.itis.bikmukhametov.chat.impl.presentation.navigation.ChatRoute
import ru.kazan.itis.bikmukhametov.impl.presentation.navigation.AuthRoute
import ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.presentation.navigation.InterlocutorInfoRoute
import ru.kazan.itis.bikmukhametov.main.impl.presentation.navigation.MainRoute
import ru.kazan.itis.bikmukhametov.profile.impl.presentation.navigation.ProfileRoute

fun NavKey.toLaunchEventName(): String = when (this) {
    AppRoute.Onboarding -> "launch_onboarding"
    AuthRoute.Login -> "launch_login"
    MainRoute.Main -> "launch_home"
    ProfileRoute.Profile -> "launch_profile"
    is ChatRoute.Chat -> "launch_chat"
    is InterlocutorInfoRoute.Details -> "launch_interlocutor_info"
    else -> "launch_unknown"
}

fun NavKey.toLaunchEventParams(): Map<String, String> = when (this) {
    is ChatRoute.Chat -> mapOf("conversation_id" to conversationId)
    is InterlocutorInfoRoute.Details -> mapOf("conversation_id" to conversationId)
    else -> emptyMap()
}
