package com.midhun.remindly.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Android calls this at the exact alarm time, even if the app is closed.
 * Alarm reminders hand over to the foreground service, which plays the sound.
 * Notification reminders just post a normal notification.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val spec = intent.toAlarmSpec() ?: return
        AlarmStorage.remove(context, spec.id)

        if (!spec.loud) {
            AlarmNotifications.postReminder(context, spec)
            return
        }

        try {
            ContextCompat.startForegroundService(context, AlarmService.startIntent(context, spec))
        } catch (e: Exception) {
            // If Android refuses to start the service, at least leave a visible notification behind.
            Log.e("AlarmReceiver", "Could not start the alarm service", e)
            AlarmNotifications.postMissed(context, spec)
        }
    }
}
