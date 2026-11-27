package com.midhun.remindly.alarm

import android.content.Intent

/**
 * What the alarm system needs to know: which alarm, what to show, when to ring.
 * [loud] = true rings like an alarm; false is a normal notification.
 */
data class AlarmSpec(
    val id: String,
    val title: String,
    val triggerAtMillis: Long,
    val loud: Boolean = true
)

private const val EXTRA_ID = "com.midhun.remindly.extra.ID"
private const val EXTRA_TITLE = "com.midhun.remindly.extra.TITLE"
private const val EXTRA_TRIGGER_AT = "com.midhun.remindly.extra.TRIGGER_AT"
private const val EXTRA_LOUD = "com.midhun.remindly.extra.LOUD"

fun AlarmSpec.putInto(intent: Intent): Intent = intent
    .putExtra(EXTRA_ID, id)
    .putExtra(EXTRA_TITLE, title)
    .putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)
    .putExtra(EXTRA_LOUD, loud)

fun Intent.toAlarmSpec(): AlarmSpec? {
    val id = getStringExtra(EXTRA_ID) ?: return null
    return AlarmSpec(
        id = id,
        title = getStringExtra(EXTRA_TITLE) ?: "Reminder",
        triggerAtMillis = getLongExtra(EXTRA_TRIGGER_AT, 0L),
        loud = getBooleanExtra(EXTRA_LOUD, true)
    )
}
