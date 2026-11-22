package com.midhun.remindly.data

import java.util.UUID

/**
 * One reminder. The time is a real timestamp (milliseconds since 1970, UTC) so it can be
 * sorted, compared and later handed to AlarmManager. Display text is built from it.
 */
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val triggerAtMillis: Long,
    val completed: Boolean = false
)

private const val HOUR = 60 * 60 * 1000L

// Captured once per app process so the sample data stays stable while the app is open.
private val seedNow = System.currentTimeMillis() / 60_000 * 60_000

/** Shown only on the very first launch, until the user adds or changes something. */
val seedReminders: List<Reminder> = listOf(
    Reminder("seed-1", "Pay electricity bill", seedNow - 20 * HOUR),
    Reminder("seed-2", "Study DBMS", seedNow + 1 * HOUR),
    Reminder("seed-3", "Workout", seedNow + 3 * HOUR),
    Reminder("seed-4", "Review project", seedNow + 5 * HOUR),
    Reminder("seed-5", "Call the electrician", seedNow + 26 * HOUR)
)
