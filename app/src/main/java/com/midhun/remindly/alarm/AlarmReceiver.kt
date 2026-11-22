package com.midhun.remindly.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Android calls this at the exact alarm time, even if the app is closed.
 * It does almost nothing itself: it hands over to the foreground service, which plays the sound.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val spec = intent.toAlarmSpec() ?: return
        AlarmStorage.remove(context, spec.id)

        try {
            ContextCompat.startForegroundService(context, AlarmService.startIntent(context, spec))
        } catch (e: Exception) {
            // If Android refuses to start the service, at least leave a visible notification behind.
            Log.e("AlarmReceiver", "Could not start the alarm service", e)
            AlarmNotifications.postMissed(context, spec)
        }
    }
}
