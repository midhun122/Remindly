package com.midhun.remindly.alarm

import android.content.Context
import com.midhun.remindly.data.RemindlyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * What the alarm screen and the notification buttons do to a reminder.
 * They work even when the app itself isn't open, and the app updates by itself if it is.
 */
object ReminderActions {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** The task is finished: mark it completed and cancel anything still scheduled for it. */
    fun markDone(context: Context, id: String, onFinished: () -> Unit = {}) {
        val appContext = context.applicationContext
        AlarmScheduler.cancel(appContext, id)
        scope.launch {
            try {
                RemindlyStore(appContext).updateReminders { list ->
                    list.map { if (it.id == id) it.copy(completed = true) else it }
                }
            } finally {
                onFinished()
            }
        }
    }

    /** Ring again in [minutes]. The reminder stays pending, with its new time. */
    fun snooze(context: Context, spec: AlarmSpec, minutes: Int, onFinished: () -> Unit = {}) {
        val appContext = context.applicationContext
        val later = spec.copy(triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L)
        AlarmScheduler.schedule(appContext, later)
        scope.launch {
            try {
                RemindlyStore(appContext).updateReminders { list ->
                    list.map {
                        if (it.id == spec.id) {
                            it.copy(triggerAtMillis = later.triggerAtMillis, completed = false)
                        } else {
                            it
                        }
                    }
                }
            } finally {
                onFinished()
            }
        }
    }
}
