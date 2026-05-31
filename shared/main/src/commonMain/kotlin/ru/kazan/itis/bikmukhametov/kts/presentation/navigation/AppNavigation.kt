package ru.kazan.itis.bikmukhametov.kts.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.kazan.itis.bikmukhametov.analytics.AppAnalytics
import ru.kazan.itis.bikmukhametov.chat.impl.presentation.navigation.ChatRoute
import ru.kazan.itis.bikmukhametov.chat.impl.presentation.screen.ChatScreen
import ru.kazan.itis.bikmukhametov.database.onboarding.OnboardingCompletedRepository
import ru.kazan.itis.bikmukhametov.impl.presentation.navigation.AuthRoute
import ru.kazan.itis.bikmukhametov.impl.presentation.screen.LoginScreen
import ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.presentation.navigation.InterlocutorInfoRoute
import ru.kazan.itis.bikmukhametov.interlocutorinfo.impl.presentation.screen.InterlocutorInfoScreen
import ru.kazan.itis.bikmukhametov.main.impl.presentation.navigation.MainRoute
import ru.kazan.itis.bikmukhametov.main.impl.presentation.screen.MainScreen
import ru.kazan.itis.bikmukhametov.network.auth.logout.LogoutEventBus
import ru.kazan.itis.bikmukhametov.network.auth.session.SessionChecker
import ru.kazan.itis.bikmukhametov.onboarding.presentation.screens.OnboardingScreen
import ru.kazan.itis.bikmukhametov.profile.impl.presentation.navigation.ProfileRoute
import ru.kazan.itis.bikmukhametov.profile.impl.presentation.screen.ProfileScreen

@Composable
fun AppNavigation(
    startDestination: AppRoute = AppRoute.Onboarding,
) {
    val scope = rememberCoroutineScope()
    val navSerializationConfig = koinInject<AppNavSerializationConfig>()
    val backStack = rememberNavBackStack(
        navSerializationConfig.savedStateConfiguration,
        startDestination,
    )
    val analytics = koinInject<AppAnalytics>()
    val logoutEventBus = koinInject<LogoutEventBus>()
    val sessionChecker = koinInject<SessionChecker>()
    val onboardingRepository = koinInject<OnboardingCompletedRepository>()

    TrackScreenLaunches(backStack = backStack, analytics = analytics)

    /*
     * Один проход при старте: если онбординг уже пройден — либо Main (живая сессия), либо Login.
     * Раньше два LaunchedEffect(Unit) гонялись и могли открыть главный экран во время ввода на логине.
     */
    LaunchedEffect(navSerializationConfig, onboardingRepository, sessionChecker) {
        if (!onboardingRepository.isOnboardingCompleted()) return@LaunchedEffect

        val isValid = runCatching { sessionChecker.isSessionValid() }.getOrDefault(false)
        if (isValid) {
            backStack.setRoot(MainRoute.Main)
        } else {
            backStack.setRoot(AuthRoute.Login)
        }
    }

    // куки протухли — триггерим логаут и навигируем на логин
    LaunchedEffect(logoutEventBus) {
        logoutEventBus.logoutEvents.collect {
            backStack.setRoot(AuthRoute.Login)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.popRoute() },
        entryProvider = entryProvider {
            entry<AppRoute.Onboarding> {
                OnboardingScreen(
                    onOnboardingComplete = {
                        scope.launch {
                            onboardingRepository.setOnboardingCompleted(true)
                            backStack.setRoot(AuthRoute.Login)
                        }
                    },
                )
            }
            entry<AuthRoute.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.setRoot(MainRoute.Main)
                    },
                )
            }
            entry<MainRoute.Main> {
                MainScreen(
                    onProfileClick = {
                        backStack.navigateTo(ProfileRoute.Profile)
                    },
                    onChatClick = { conversationId ->
                        backStack.navigateTo(ChatRoute.Chat(conversationId = conversationId))
                    },
                )
            }
            entry<ChatRoute.Chat> { chatRoute ->
                ChatScreen(
                    conversationId = chatRoute.conversationId,
                    onBack = { backStack.popRoute() },
                    onUserInfoClick = { interlocutorName, channelKind, channelName, chatId, userId ->
                        backStack.navigateTo(
                            InterlocutorInfoRoute.Details(
                                conversationId = chatRoute.conversationId,
                                interlocutorName = interlocutorName,
                                channelKind = channelKind,
                                channelName = channelName,
                                chatId = chatId,
                                userId = userId,
                            ),
                        )
                    },
                )
            }
            entry<InterlocutorInfoRoute.Details> { infoRoute ->
                InterlocutorInfoScreen(
                    conversationId = infoRoute.conversationId,
                    interlocutorName = infoRoute.interlocutorName,
                    channelKind = infoRoute.channelKind,
                    channelName = infoRoute.channelName,
                    chatId = infoRoute.chatId,
                    userId = infoRoute.userId,
                    onClose = { backStack.popRoute() },
                )
            }
            entry<ProfileRoute.Profile> {
                ProfileScreen(
                    onChatsClick = {
                        backStack.replaceTop(MainRoute.Main)
                    },
                )
            }
        },
    )
}

private fun MutableList<NavKey>.popRoute() {
    if (isNotEmpty()) removeAt(lastIndex)
}

private fun MutableList<NavKey>.navigateTo(route: NavKey) {
    add(route)
}

private fun MutableList<NavKey>.replaceTop(route: NavKey) {
    if (isNotEmpty()) removeAt(lastIndex)
    add(route)
}

private fun MutableList<NavKey>.setRoot(route: NavKey) {
    clear()
    add(route)
}

@Composable
private fun TrackScreenLaunches(
    backStack: MutableList<NavKey>,
    analytics: AppAnalytics,
) {
    val analyticsState by rememberUpdatedState(analytics)

    LaunchedEffect(backStack) {
        snapshotFlow { backStack.lastOrNull() }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { route ->
                analyticsState.logEvent(
                    name = route.toLaunchEventName(),
                    params = route.toLaunchEventParams(),
                )
            }
    }
}
