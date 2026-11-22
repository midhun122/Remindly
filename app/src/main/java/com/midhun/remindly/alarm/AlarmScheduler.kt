package com.midhun.remindly.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.midhun.remindly.MainActivity

/**
 * Schedules alarms with AlarmManager.setAlarmClock, the strongest option Android offers:
 * it fires on time even in Doze and shows the alarm icon in the status bar.
 *
 * Only the NEXT occurrence is ever scheduled (no repeating alarms); recurrence will be handled by
 * scheduling the following one after each alarm rings.
 */
object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val ACTION_ALARM = "com.midhun.remindly.action.ALARM"
    const val TEST_ALARM_ID = "test-alarm"

    /** Android 12+ requires the user to allow "Alarms & reminders"; Android 14+ denies it by default. */
    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    /** Returns false (and schedules nothing) if the phone doesn't currently allow exact alarms. */
    fun schedule(context: Context, spec: AlarmSpec): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (!canScheduleExact(context)) return false

        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(spec.triggerAtMillis, showIntent),
                operation(context, spec)
            )
            AlarmStorage.save(context, spec)
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing", e)
            false
        }
    }

    fun cancel(context: Context, id: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(operation(context, AlarmSpec(id, "", 0L)))
        AlarmStorage.remove(context, id)
    }

    /**
     * Sets every saved alarm again. Called after reboot, app update, clock/timezone change and
     * when the exact-alarm permission is granted again. Alarms whose time already passed while
     * the phone was off are shown as "missed" instead of being silently lost.
     */
    fun rescheduleAll(context: Context) {
        val now = System.currentTimeMillis()
        AlarmStorage.all(context).forEach { spec ->
            if (spec.triggerAtMillis > now) {
                schedule(context, spec)
            } else {
                AlarmStorage.remove(context, spec.id)
                AlarmNotifications.postMissed(context, spec)
            }
        }
    }

    private fun operation(context: Context, spec: AlarmSpec): PendingIntent {
        // The unique data URI keeps every alarm's PendingIntent separate, one per alarm id.
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_ALARM)
            .setData(Uri.parse("remindly://alarm/${spec.id}"))
        spec.putInto(intent)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
