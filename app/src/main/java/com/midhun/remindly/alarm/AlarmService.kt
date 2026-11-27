package com.midhun.remindly.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat

/**
 * Plays the alarm. It runs as a foreground service (type: media playback) so Android keeps it
 * alive and allows sound while the app is in the background, plays on the ALARM audio stream,
 * vibrates, and stops by itself after 60 seconds.
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var current: AlarmSpec? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { onTimeout() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val spec = intent.toAlarmSpec()
                if (spec != null) {
                    startRinging(spec)
                } else {
                    stopSelf()
                }
            }

            ACTION_DONE -> done()
            ACTION_DISMISS -> dismiss()
            ACTION_SNOOZE -> snooze(intent.getIntExtra(EXTRA_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES))
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(spec: AlarmSpec) {
        stopPlayback()
        current = spec

        // The alarm screen watches this, so set it before the notification launches that screen.
        AlarmState.set(spec)

        try {
            val notification = AlarmNotifications.ringing(this, spec)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    AlarmNotifications.RINGING_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(AlarmNotifications.RINGING_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not start as a foreground service", e)
            AlarmState.set(null)
            AlarmNotifications.postMissed(this, spec)
            stopSelf()
            return
        }

        acquireWakeLock()
        startSound()
        startVibration()

        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, RING_DURATION_MILLIS)
    }

    /** The task is finished: mark the reminder done, then stop ringing. */
    private fun done() {
        current?.let { ReminderActions.markDone(this, it.id) }
        dismiss()
    }

    /**
     * Silence: stop the noise only. The reminder stays pending, so it will show up in Missed
     * until it is marked done or rescheduled.
     */
    private fun dismiss() {
        stopPlayback()
        AlarmState.set(null)
        NotificationManagerCompat.from(this).cancel(AlarmNotifications.RINGING_NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snooze(minutes: Int) {
        current?.let { ReminderActions.snooze(this, it, minutes) }
        dismiss()
    }

    /** Nobody answered within 60 seconds: stop the noise, but leave a notification behind. */
    private fun onTimeout() {
        val spec = current
        stopPlayback()
        AlarmState.set(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (spec != null) AlarmNotifications.postMissed(this, spec)
        stopSelf()
    }

    private fun startSound() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        try {
            player = MediaPlayer().apply {
                // USAGE_ALARM matters: it follows the alarm volume, and Android 17 allows it from
                // the background when the app holds the exact-alarm permission.
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not play the alarm sound", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = vib
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 700, 500), 0), attributes)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Remindly:alarm").apply {
            acquire(RING_DURATION_MILLIS + 10_000L)
        }
    }

    private fun stopPlayback() {
        handler.removeCallbacks(autoStop)
        player?.let {
            try {
                it.stop()
            } catch (e: IllegalStateException) {
                // Already stopped; nothing to do.
            }
            it.release()
        }
        player = null
        vibrator?.cancel()
        vibrator = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        stopPlayback()
        AlarmState.set(null)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AlarmService"
        const val RING_DURATION_MILLIS = 60_000L
        const val DEFAULT_SNOOZE_MINUTES = 10

        private const val ACTION_START = "com.midhun.remindly.action.START_RINGING"
        private const val ACTION_DISMISS = "com.midhun.remindly.action.DISMISS"
        private const val ACTION_DONE = "com.midhun.remindly.action.DONE"
        private const val ACTION_SNOOZE = "com.midhun.remindly.action.SNOOZE"
        private const val EXTRA_SNOOZE_MINUTES = "com.midhun.remindly.extra.SNOOZE_MINUTES"

        fun startIntent(context: Context, spec: AlarmSpec): Intent =
            spec.putInto(Intent(context, AlarmService::class.java).setAction(ACTION_START))

        /** Silence the alarm but keep the reminder pending. */
        fun dismissIntent(context: Context): Intent =
            Intent(context, AlarmService::class.java).setAction(ACTION_DISMISS)

        /** Mark the reminder done and stop the alarm. */
        fun doneIntent(context: Context): Intent =
            Intent(context, AlarmService::class.java).setAction(ACTION_DONE)

        fun snoozeIntent(context: Context, minutes: Int): Intent =
            Intent(context, AlarmService::class.java)
                .setAction(ACTION_SNOOZE)
                .putExtra(EXTRA_SNOOZE_MINUTES, minutes)
    }
}
