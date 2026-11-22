package com.midhun.remindly.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/** What must be true on this phone for an alarm to ring properly. Seed of the Reliability Guard. */
data class AlarmReadiness(
    val notifications: Boolean,
    val exactAlarms: Boolean,
    val fullScreen: Boolean
) {
    val allGood: Boolean get() = notifications && exactAlarms && fullScreen
}

object AlarmPermissions {

    fun readiness(context: Context): AlarmReadiness {
        val manager = context.getSystemService(NotificationManager::class.java)

        val appNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val channel = manager.getNotificationChannel(AlarmNotifications.CHANNEL_RINGING)
        val channelBlocked = channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE

        val fullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            manager.canUseFullScreenIntent()
        } else {
            true
        }

        return AlarmReadiness(
            notifications = appNotifications && !channelBlocked,
            exactAlarms = AlarmScheduler.canScheduleExact(context),
            fullScreen = fullScreen
        )
    }

    /** Alarm volume as a percentage (0 to 100). Changes whenever the user moves the slider. */
    fun alarmVolumePercent(context: Context): Int {
        val audio = context.getSystemService(AudioManager::class.java)
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        if (max <= 0) return 0
        return audio.getStreamVolume(AudioManager.STREAM_ALARM) * 100 / max
    }

    /** Below this the alarm may be too quiet to hear in a noisy room. A guideline, not a rule. */
    const val MIN_COMFORTABLE_VOLUME_PERCENT = 50

    private fun packageUri(context: Context): Uri = Uri.parse("package:${context.packageName}")

    fun appNotificationSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun exactAlarmSettingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri(context))
        } else {
            appDetailsIntent(context)
        }

    fun fullScreenSettingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri(context))
        } else {
            appDetailsIntent(context)
        }

    fun soundSettingsIntent(): Intent = Intent(Settings.ACTION_SOUND_SETTINGS)

    fun appDetailsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context))
}
