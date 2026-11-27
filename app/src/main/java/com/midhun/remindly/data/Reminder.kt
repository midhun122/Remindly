package com.midhun.remindly.data

import java.util.UUID

/** How a reminder gets your attention. */
enum class ReminderMode {
    /** Rings like an alarm and opens over the lock screen. */
    ALARM,

    /** A normal notification banner with sound and vibration. */
    NOTIFICATION
}

/**
 * One reminder. The time is a real timestamp (milliseconds since 1970, UTC) so it can be
 * sorted, compared and handed to AlarmManager. Display text is built from it.
 */
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val triggerAtMillis: Long,
    val completed: Boolean = false,
    val mode: ReminderMode = ReminderMode.ALARM
)
