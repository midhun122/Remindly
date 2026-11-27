package com.midhun.remindly.alarm

import android.content.Context
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.data.ReminderMode

fun Reminder.toAlarmSpec(): AlarmSpec = AlarmSpec(
    id = id,
    title = title,
    triggerAtMillis = triggerAtMillis,
    loud = mode == ReminderMode.ALARM
)

/**
 * Keeps AlarmManager in step with the reminder list:
 *  - a pending reminder in the future gets an alarm,
 *  - a completed, past or deleted reminder has its alarm cancelled.
 * Safe to call as often as you like; scheduling the same alarm again just replaces it.
 */
object ReminderAlarms {
    fun sync(
        context: Context,
        before: List<Reminder>,
        after: List<Reminder>,
        now: Long = System.currentTimeMillis()
    ) {
        val remaining = after.map { it.id }.toSet()
        before.filter { it.id !in remaining }.forEach { AlarmScheduler.cancel(context, it.id) }

        after.forEach { reminder ->
            if (!reminder.completed && reminder.triggerAtMillis > now) {
                AlarmScheduler.schedule(context, reminder.toAlarmSpec())
            } else {
                AlarmScheduler.cancel(context, reminder.id)
            }
        }
    }
}
