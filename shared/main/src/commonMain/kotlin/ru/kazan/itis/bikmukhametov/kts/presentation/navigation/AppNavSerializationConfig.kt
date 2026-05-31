package ru.kazan.itis.bikmukhametov.kts.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import ru.kazan.itis.bikmukhametov.chat.impl.presentation.navigation.ChatRoute
import ru.kazan.itis.bikmukhametov.impl.presentation.navigation.AuthRoute
import ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.presentation.navigation.InterlocutorInfoRoute
import ru.kazan.itis.bikmukhametov.main.impl.presentation.navigation.MainRoute
import ru.kazan.itis.bikmukhametov.profile.impl.presentation.navigation.ProfileRoute

class AppNavSerializationConfig {
    val serializersModule: SerializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<AppRoute>()
            subclassesOfSealed<AuthRoute>()
            subclassesOfSealed<MainRoute>()
            subclassesOfSealed<ProfileRoute>()
            subclassesOfSealed<ChatRoute>()
            subclassesOfSealed<InterlocutorInfoRoute>()
        }
    }

    val savedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
        serializersModule = this@AppNavSerializationConfig.serializersModule
    }
}
