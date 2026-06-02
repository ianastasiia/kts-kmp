package ru.kazan.itis.bikmukhametov.network.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.NonCancellable.get
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.kazan.itis.bikmukhametov.network.BuildKonfig
import ru.kazan.itis.bikmukhametov.network.auth.datasource.AuthDataSource
import ru.kazan.itis.bikmukhametov.network.auth.datasource.AuthDataSourceImpl
import ru.kazan.itis.bikmukhametov.network.space.api.SpaceProvider
import ru.kazan.itis.bikmukhametov.network.auth.logout.LogoutEventBus
import ru.kazan.itis.bikmukhametov.network.auth.logout.LogoutService
import ru.kazan.itis.bikmukhametov.network.auth.logout.LogoutServiceImpl
import ru.kazan.itis.bikmukhametov.network.auth.session.SessionChecker
import ru.kazan.itis.bikmukhametov.network.auth.session.SessionCheckerImpl
import ru.kazan.itis.bikmukhametov.network.cookie.PersistentCookieStorage
import ru.kazan.itis.bikmukhametov.network.error.ErrorResponse
import ru.kazan.itis.bikmukhametov.network.space.impl.SpaceProviderImpl

/* модуль для сети */
val networkModule = module {

    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single { LogoutEventBus() }

//    single<SpaceProvider> { SpaceProviderImpl(get(named(PlatformDataStoreNames.SPACE)), get()) }

    single { PersistentCookieStorage(get()) }

    factory<LogoutService> { LogoutServiceImpl(get(), get()) }

    factory<AuthDataSource> { AuthDataSourceImpl(get()) }
    factory<SessionChecker> { SessionCheckerImpl(get()) }

    single {

        val spaceProvider = get<SpaceProvider>()
        val cookieStorage = get<PersistentCookieStorage>()
        val logoutBus = get<LogoutEventBus>()
        val appScope = get<CoroutineScope>()

        // Если платформа предоставила движок (Android — OkHttp с таймаутами), используем его.
        // На iOS движок подхватывается автоматически (Darwin).
        val engine = getOrNull<HttpClientEngine>()
        val clientBlock: HttpClientConfig<*>.() -> Unit = {

            install(HttpCookies) { storage = cookieStorage }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(WebSockets)

            defaultRequest {
                url(BuildKonfig.BASE_URL)

                contentType(ContentType.Application.Json)
                header("Accept", "application/json, text/plain, */*")

                // X-SPro-Cabinet, X-SPro-Project — из выбранного пространства (Topbar)
                spaceProvider.cabinet.value
                    ?.takeIf { it.isNotBlank() }
                    ?.let { header("X-SPro-Cabinet", it) }
                spaceProvider.project.value
                    ?.takeIf { it.isNotBlank() }
                    ?.let { header("X-SPro-Project", it) }

                header("X-SPro-Bucket", "prod")
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        val url = response.call.request.url
                        val path = url.encodedPath

                        val errorBodyText = response.bodyAsText()

                        val serverMessage = runCatching {
                            Json.decodeFromString<ErrorResponse>(errorBodyText).message
                        }.getOrNull() ?: "No message from server"

                        val requestHeaders = response.call.request.headers.entries()
                        Napier.e(tag = "Network") { "401 REQUEST HEADERS for $url: $requestHeaders" }

                        if (!path.contains("auth") && !path.contains("login")) {
                            Napier.e(tag = "Network") {
                                "AUTH ERROR - URL: $url, Status: ${response.status}, " +
                                        "Server Message: $serverMessage, Body: $errorBodyText"
                            }
                            appScope.launch {
                                cookieStorage.clear()
                                logoutBus.trigger()
                            }
                        } else {
                            Napier.w(tag = "Network") { "Login failed: $serverMessage" }
                        }
                    }
                }
            }
        }

        if (engine != null) HttpClient(engine, clientBlock) else HttpClient(clientBlock)
    }
}
