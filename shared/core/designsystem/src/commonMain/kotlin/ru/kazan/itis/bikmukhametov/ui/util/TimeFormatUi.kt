package ru.kazan.itis.bikmukhametov.ui.util

/**
 * Форматирует дату/время для отображения в UI (карточки чатов, списки).
 * Без использования deprecated/experimental API (Instant, Clock и т.п.):
 * только epoch-миллисекунды и ручной разбор ISO 8601.
 *
 * - Сегодня → "12:30"
 * - Вчера → строка из ресурсов
 * - В течение недели → короткое имя дня недели из [TimeFormatStrings]
 * - Старше → "09.03.24"
 *
 * Сравнение по UTC-дням; время выводится как в исходной строке (обычно UTC).
 *
 * @param isoOrEpoch строка ISO 8601 (например "2024-03-09T12:30:00Z") или число epoch-миллисекунд
 * @param nowMs текущее время в epoch-миллисекундах (по умолчанию — платформа через [currentTimeMillis])
 */
fun formatTimeForUi(
    isoOrEpoch: String,
    strings: TimeFormatStrings,
    nowMs: Long = currentTimeMillis(),
): String {
    val epochMs = parseToEpochMillis(isoOrEpoch) ?: return isoOrEpoch
    val parsed = epochMillisToComponents(epochMs)

    val nowDay = nowMs / MILLIS_PER_DAY
    val thenDay = epochMs / MILLIS_PER_DAY
    val diffDays = nowDay - thenDay

    return when {
        diffDays == 0L -> "${parsed.hour.toString().padStart(2, '0')}:${
            parsed.minute.toString().padStart(2, '0')
        }"

        diffDays == 1L -> strings.yesterday
        diffDays in 2L..6L -> dayOfWeekShort(parsed.dayOfWeek, strings)
        else -> "${parsed.dayOfMonth.toString().padStart(2, '0')}.${
            parsed.month.toString().padStart(2, '0')
        }.${(parsed.year % 100).toString().padStart(2, '0')}"
    }
}

/**
 * Возвращает только время в формате "HH:MM" (для отображения в пузыре сообщения).
 */
fun formatTimeOnly(isoOrEpoch: String): String {
    val epochMs = parseToEpochMillis(isoOrEpoch) ?: return isoOrEpoch
    val parsed = epochMillisToComponents(epochMs)
    return "${parsed.hour.toString().padStart(2, '0')}:${parsed.minute.toString().padStart(2, '0')}"
}

/**
 * Возвращает метку даты для разделителя в чате:
 * - Сегодня → из ресурсов
 * - Вчера → из ресурсов
 * - Тот же год → "16 марта" (месяц из ресурсов)
 * - Другой год → "16 марта 2023"
 */
fun formatDateLabel(
    isoOrEpoch: String,
    strings: TimeFormatStrings,
    nowMs: Long = currentTimeMillis(),
): String {
    val epochMs = parseToEpochMillis(isoOrEpoch) ?: return isoOrEpoch
    val parsed = epochMillisToComponents(epochMs)

    val nowDay = nowMs / MILLIS_PER_DAY
    val thenDay = epochMs / MILLIS_PER_DAY
    val diffDays = nowDay - thenDay
    val nowYear = epochMillisToComponents(nowMs).year

    return when {
        diffDays == 0L -> strings.today
        diffDays == 1L -> strings.yesterday
        parsed.year == nowYear -> "${parsed.dayOfMonth} ${monthName(parsed.month, strings)}"
        else -> "${parsed.dayOfMonth} ${monthName(parsed.month, strings)} ${parsed.year}"
    }
}

/**
 * Возвращает номер дня (epochMs / MILLIS_PER_DAY) для группировки сообщений по датам.
 * При ошибке парсинга возвращает [Long.MIN_VALUE].
 */
fun epochDayOf(isoOrEpoch: String): Long {
    val epochMs = parseToEpochMillis(isoOrEpoch) ?: return Long.MIN_VALUE
    return epochMs / MILLIS_PER_DAY
}

private fun monthName(month: Int, strings: TimeFormatStrings): String {
    return if (month in 1..12) {
        strings.monthsGenitive[month - 1]
    } else {
        strings.unknown
    }
}

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

/**
 * Текущее время в epoch-миллисекундах (UTC).
 * Реализация — expect/actual по платформам.
 */
expect fun currentTimeMillis(): Long

/**
 * Парсит ISO 8601 или строку из цифр (epoch-мс) в epoch-миллисекунды.
 */
private fun parseToEpochMillis(isoOrEpoch: String): Long? {
    if (isoOrEpoch.all { it.isDigit() }) return isoOrEpoch.toLongOrNull()
    return parseIso8601ToEpochMillis(isoOrEpoch)
}

/**
 * Разбор ISO 8601 (например "2024-03-09T12:30:00Z" или "2024-03-09T12:30:00.123+00:00")
 * в epoch-миллисекунды (UTC). Упрощённый парсер без внешних библиотек.
 */
private fun parseIso8601ToEpochMillis(iso: String): Long? {
    val tIndex = iso.indexOf('T')
    if (tIndex <= 0) return null
    val datePart = iso.take(tIndex)
    var timePart = iso.substring(tIndex + 1)
    // убираем суффикс времени (Z, +00:00, -05:30 и т.д.)
    val zIdx = timePart.indexOf('Z')
    val plusIdx = timePart.indexOf('+')
    val minusIdx = timePart.indexOf('-', 1)
    val endIdx = when {
        zIdx >= 0 -> zIdx
        plusIdx >= 0 -> plusIdx
        minusIdx >= 0 -> minusIdx
        else -> timePart.length
    }
    timePart = timePart.take(endIdx)
    val dateNumbers = datePart.split('-').mapNotNull { it.toIntOrNull() }
    if (dateNumbers.size != 3) return null
    val (y, m, d) = dateNumbers
    val timeNumbers = timePart.split(':').flatMap { it.split('.') }.mapNotNull { it.toIntOrNull() }
    if (timeNumbers.size < 3) return null
    val hour = timeNumbers[0]
    val minute = timeNumbers[1]
    val second = timeNumbers[2]
    return utcToEpochMillis(y, m, d, hour, minute, second)
}

/**
 * Преобразование (год, месяц, день, час, минута, секунда) UTC в epoch-миллисекунды.
 * Упрощённая реализация (без учёта високосных секунд).
 */
private fun utcToEpochMillis(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    hour: Int,
    minute: Int,
    second: Int
): Long {
    val days = dateToEpochDays(year, month, dayOfMonth)
    return days * MILLIS_PER_DAY +
            hour * 3600L * 1000 +
            minute * 60L * 1000 +
            second * 1000L
}

private fun dateToEpochDays(year: Int, month: Int, dayOfMonth: Int): Long {
    var y = year
    var m = month
    if (m <= 2) {
        y--
        m += 12
    }
    val era = (y / 400).toLong()
    val yoe = (y % 400).toLong()
    val doy = (153 * (m - 3) + 2) / 5 + dayOfMonth - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private data class DateComponents(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday
)

private fun epochMillisToComponents(epochMs: Long): DateComponents {
    val day = (epochMs / MILLIS_PER_DAY).toInt()
    val timeMs = (epochMs % MILLIS_PER_DAY).toInt()
    if (timeMs < 0) return epochMillisToComponents(epochMs + MILLIS_PER_DAY)
    val (y, m, d) = epochDaysToDate(day)
    val hour = timeMs / (3600 * 1000)
    val minute = (timeMs % (3600 * 1000)) / (60 * 1000)
    val dayOfWeek = ((day + 3) % 7).let { if (it <= 0) it + 7 else it }
    return DateComponents(y, m, d, hour, minute, dayOfWeek)
}

/**
 * ISO 8601 UTC для оптимистичных записей (сортировка и [formatTimeOnly] совместимы с сообщениями API).
 */
fun epochMillisToIso8601Utc(ms: Long): String {
    val day = (ms / MILLIS_PER_DAY).toInt()
    val timeMs = (ms % MILLIS_PER_DAY).toInt()
    if (timeMs < 0) return epochMillisToIso8601Utc(ms + MILLIS_PER_DAY)
    val (y, m, dom) = epochDaysToDate(day)
    val hour = timeMs / (3600 * 1000)
    val rem = timeMs % (3600 * 1000)
    val minute = rem / (60 * 1000)
    val second = (rem % (60 * 1000)) / 1000
    return "${y.toString().padStart(4, '0')}-" +
            "${m.toString().padStart(2, '0')}-" +
            "${dom.toString().padStart(2, '0')}T" +
            "${hour.toString().padStart(2, '0')}:" +
            "${minute.toString().padStart(2, '0')}:" +
            "${second.toString().padStart(2, '0')}.000Z"
}

private fun epochDaysToDate(epochDays: Int): Triple<Int, Int, Int> {
    var day = epochDays + 719468
    val era = (if (day >= 0) day else day - 146096) / 146097
    val doe = day - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = (era * 400 + yoe).toInt()
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + (if (mp < 10) 3 else -9)
    val year = y + (if (m <= 2) 1 else 0)
    return Triple(year, m, d)
}

private fun dayOfWeekShort(dayOfWeek: Int, strings: TimeFormatStrings): String {
    val idx = dayOfWeek - 1
    return strings.weekdayShort.getOrNull(idx) ?: strings.unknown
}
