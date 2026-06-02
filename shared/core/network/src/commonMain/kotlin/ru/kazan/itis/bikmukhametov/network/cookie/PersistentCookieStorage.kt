package ru.kazan.itis.bikmukhametov.network.cookie

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kazan.itis.bikmukhametov.database.cookie.CookiePersistence

/**
 * Хранит куки в DataStore через CookiePersistence.
 *
 * На каждый get/addCookie:
 * - читает строку Cookie из DataStore
 * - парсит/обновляет
 * - записывает обратно.
 */
internal class PersistentCookieStorage(
    private val persistence: CookiePersistence
) : CookiesStorage {
    private val mutex = Mutex()

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val header = persistence.getCookieHeader().orEmpty()
        val cookies = parseCookieHeader(header).filter { isCookieValid(it) }
        val domain = rootDomain(requestUrl.host)
        val withDomain = cookies.map { it.copy(domain = domain) }
        Napier.d(tag = "CookieStorage") {
            "get(${requestUrl.host}), domain=$domain: returning ${withDomain.map { it.name }}"
        }
        withDomain
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = mutex.withLock {
        val currentHeader = persistence.getCookieHeader().orEmpty()
        val currentCookies = parseCookieHeader(currentHeader)
            .associateBy { it.name }
            .toMutableMap()

        val domain = rootDomain(requestUrl.host)
        val normalized = cookie.copy(domain = domain)

        Napier.d(tag = "CookieStorage") {
            "addCookie: name=${normalized.name}, domain=$domain"
        }

        currentCookies[normalized.name] = normalized

        val serialized = serializeCookies(currentCookies.values)
        persistence.setCookieHeader(serialized)

        Napier.d(tag = "CookieStorage") {
            "stored to DataStore: keys=${currentCookies.keys}"
        }
    }

    /* Вызывать при логауте / 401 — очищает DataStore. */
    suspend fun clear() = mutex.withLock {
        persistence.clear()
        Napier.d(tag = "CookieStorage") { "cleared" }
    }

    override fun close() = Unit

    private fun parseCookieHeader(header: String): List<Cookie> {
        if (header.isBlank()) return emptyList()
        return header.split(SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { part ->
                val eq = part.indexOf('=')
                if (eq <= 0) return@mapNotNull null
                val name = part.take(eq).trim()
                val value = part.substring(eq + 1).trim()
                if (name.isEmpty()) return@mapNotNull null
                Cookie(name = name, value = value, encoding = CookieEncoding.RAW)
            }
    }

    private fun serializeCookies(cookies: Collection<Cookie>): String =
        cookies.joinToString(SEPARATOR) { "${it.name}=${it.value}" }

    private fun isCookieValid(cookie: Cookie): Boolean {
        val expires = cookie.expires ?: return true
        return expires.timestamp > currentTimeMillis()
    }

    /* Домен верхнего уровня (metac-92.smartbotpro.ru → smartbotpro.ru), чтобы куки шли на все поддомены. */
    private fun rootDomain(host: String): String {
        val parts = host.split('.')
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }

    companion object {
        private const val SEPARATOR = "; "
    }
}
