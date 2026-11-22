package com.midhun.remindly.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun millisToDateTime(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

// Always 12-hour with AM/PM, e.g. "1:48 PM". English AM/PM keeps the whole app consistent.
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private val timeWithSecondsFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH)

fun formatTime(dateTime: LocalDateTime): String = timeFormatter.format(dateTime)

fun formatTimeWithSeconds(millis: Long): String =
    timeWithSecondsFormatter.format(millisToDateTime(millis))

fun formatTime(millis: Long): String = formatTime(millisToDateTime(millis))

fun formatDateShort(date: LocalDate): String =
    DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()).format(date)

fun formatDateShort(millis: Long): String = formatDateShort(millisToDateTime(millis).toLocalDate())

/** "Today", "Tomorrow", "Yesterday", otherwise "Mon, 24 Nov". */
fun dayLabel(millis: Long, now: Long): String {
    val date = millisToDateTime(millis).toLocalDate()
    val today = millisToDateTime(now).toLocalDate()
    return when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> formatDateShort(date)
    }
}

/** "in 12 min", "in 2 h 15 min", or null when it is past or more than a day away. */
fun relativeIn(target: Long, now: Long): String? {
    val minutes = (target - now) / 60_000
    return when {
        minutes < 0 -> null
        minutes < 1 -> "now"
        minutes < 60 -> "in $minutes min"
        minutes < 24 * 60 -> {
            val h = minutes / 60
            val m = minutes % 60
            if (m == 0L) "in $h h" else "in $h h $m min"
        }
        else -> null
    }
}
