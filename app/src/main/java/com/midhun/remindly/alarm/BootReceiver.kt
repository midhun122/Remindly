package com.midhun.remindly.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Android forgets all alarms when the phone restarts, the app is updated, the clock or timezone
 * changes, or the exact-alarm permission is revoked. This puts them back.
 * It only sets alarms again; it never plays sound (Android blocks that from boot).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED ->
                AlarmScheduler.rescheduleAll(context)
        }
    }
}
