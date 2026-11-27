package com.midhun.remindly.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.midhun.remindly.MainActivity
import com.midhun.remindly.R
import com.midhun.remindly.util.formatTime

object AlarmNotifications {
    const val CHANNEL_RINGING = "alarm_ringing"
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_MISSED = "alarm_missed"
    const val RINGING_NOTIFICATION_ID = 1001

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // The service plays the sound itself (on the alarm audio stream), so this channel is silent.
        val ringing = NotificationChannel(
            CHANNEL_RINGING,
            "Ringing alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shown while an alarm is ringing"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        // Normal reminders: a banner with the phone's notification sound and vibration.
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders set to notify instead of ring"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val missed = NotificationChannel(
            CHANNEL_MISSED,
            "Missed alarms",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Shown when an alarm rang but nobody answered it"
        }

        manager.createNotificationChannel(ringing)
        manager.createNotificationChannel(reminders)
        manager.createNotificationChannel(missed)
    }

    /** The notification that stays up while the alarm rings. It also launches the full-screen alarm. */
    fun ringing(context: Context, spec: AlarmSpec): Notification {
        ensureChannels(context)

        val screenIntent = spec.putInto(
            Intent(context, AlarmActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        val screenPending = PendingIntent.getActivity(
            context, 0, screenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val silencePending = PendingIntent.getService(
            context, 1, AlarmService.dismissIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozePending = PendingIntent.getService(
            context, 2, AlarmService.snoozeIntent(context, AlarmService.DEFAULT_SNOOZE_MINUTES),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val donePending = PendingIntent.getService(
            context, 3, AlarmService.doneIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_RINGING)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(spec.title)
            .setContentText("Alarm, ${formatTime(spec.triggerAtMillis)}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(screenPending)
            .setFullScreenIntent(screenPending, true)
            .addAction(0, "Done", donePending)
            .addAction(0, "Snooze", snoozePending)
            .addAction(0, "Silence", silencePending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    /** A normal reminder notification (for reminders set to "Notification" instead of "Alarm"). */
    @SuppressLint("MissingPermission")
    fun postReminder(context: Context, spec: AlarmSpec) {
        ensureChannels(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val donePending = PendingIntent.getBroadcast(
            context, 0, ReminderActionReceiver.doneIntent(context, spec),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozePending = PendingIntent.getBroadcast(
            context, 0, ReminderActionReceiver.snoozeIntent(context, spec),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(spec.title)
            .setContentText("Reminder, ${formatTime(spec.triggerAtMillis)}")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(0, "Done", donePending)
            .addAction(0, "Snooze ${ReminderActionReceiver.SNOOZE_MINUTES} min", snoozePending)
            .build()
        manager.notify(spec.id.hashCode(), notification)
    }

    /** A normal notification so an alarm nobody answered is never silently lost. */
    @SuppressLint("MissingPermission")
    fun postMissed(context: Context, spec: AlarmSpec) {
        ensureChannels(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_MISSED)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle("Missed: ${spec.title}")
            .setContentText("Rang at ${formatTime(spec.triggerAtMillis)}. Tap to open Remindly.")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        manager.notify(spec.id.hashCode(), notification)
    }
}
