package ru.kazan.itis.bikmukhametov.kts.presentation.di

import org.koin.core.module.Module
import ru.kazan.itis.bikmukhametov.chat.impl.di.chatModule
import ru.kazan.itis.bikmukhametov.chat.impl.di.chatPlatformModule
import ru.kazan.itis.bikmukhametov.database.di.databasePlatformModules
import ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.di.interlocutorInfoModule
import ru.kazan.itis.bikmukhametov.impl.di.loginModule
import ru.kazan.itis.bikmukhametov.kts.presentation.navigation.AppNavSerializationConfig
import ru.kazan.itis.bikmukhametov.main.impl.di.mainModule
import ru.kazan.itis.bikmukhametov.network.di.networkModule
import ru.kazan.itis.bikmukhametov.profile.impl.di.profileModule
import org.koin.dsl.module

fun appModules() = listOf(
    module {
        single { AppNavSerializationConfig() }
    },
    networkModule,  // Сетевой модуль
    loginModule,    // Фича логина
    mainModule,     // Фича main
    profileModule,  // Фича профиля
    chatModule,     // Фича чата
    chatPlatformModule(), // платформенные зависимости чата (стрим вложений)
    interlocutorInfoModule,
)
