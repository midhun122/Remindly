package com.midhun.remindly.alarm

import android.content.Intent

/** What the alarm system needs to know: which alarm, what to show, when to ring. */
data class AlarmSpec(
    val id: String,
    val title: String,
    val triggerAtMillis: Long
)

private const val EXTRA_ID = "com.midhun.remindly.extra.ID"
private const val EXTRA_TITLE = "com.midhun.remindly.extra.TITLE"
private const val EXTRA_TRIGGER_AT = "com.midhun.remindly.extra.TRIGGER_AT"

fun AlarmSpec.putInto(intent: Intent): Intent = intent
    .putExtra(EXTRA_ID, id)
    .putExtra(EXTRA_TITLE, title)
    .putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)

fun Intent.toAlarmSpec(): AlarmSpec? {
    val id = getStringExtra(EXTRA_ID) ?: return null
    return AlarmSpec(
        id = id,
        title = getStringExtra(EXTRA_TITLE) ?: "Reminder",
        triggerAtMillis = getLongExtra(EXTRA_TRIGGER_AT, 0L)
    )
}
