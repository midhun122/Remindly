package com.midhun.remindly.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationManagerCompat

/** Handles the Done and Snooze buttons on a normal (non-alarm) reminder notification. */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val spec = intent.toAlarmSpec() ?: return
        NotificationManagerCompat.from(context).cancel(spec.id.hashCode())

        // goAsync keeps this receiver alive until the saved list has been updated.
        val pending = goAsync()
        when (intent.action) {
            ACTION_DONE -> ReminderActions.markDone(context, spec.id) { pending.finish() }
            ACTION_SNOOZE -> ReminderActions.snooze(context, spec, SNOOZE_MINUTES) { pending.finish() }
            else -> pending.finish()
        }
    }

    companion object {
        private const val ACTION_DONE = "com.midhun.remindly.action.NOTIFICATION_DONE"
        private const val ACTION_SNOOZE = "com.midhun.remindly.action.NOTIFICATION_SNOOZE"
        const val SNOOZE_MINUTES = 10

        fun doneIntent(context: Context, spec: AlarmSpec): Intent = spec.putInto(
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(ACTION_DONE)
                .setData(Uri.parse("remindly://action/done/${spec.id}"))
        )

        fun snoozeIntent(context: Context, spec: AlarmSpec): Intent = spec.putInto(
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(ACTION_SNOOZE)
                .setData(Uri.parse("remindly://action/snooze/${spec.id}"))
        )
    }
}
