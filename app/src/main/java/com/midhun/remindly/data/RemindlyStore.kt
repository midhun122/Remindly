package com.midhun.remindly.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private val Context.remindlyDataStore by preferencesDataStore(name = "remindly")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** What a save changed: the list before and after. */
data class ReminderChange(val before: List<Reminder>, val after: List<Reminder>)

/**
 * Saves the theme choice and the reminders on the phone.
 * Reminders are stored as JSON for now; this is replaced by a Room database in the next phase.
 */
class RemindlyStore(context: Context) {

    private val dataStore = context.applicationContext.remindlyDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            val saved = prefs[THEME_KEY]
            ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
        }
        .distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.name }
    }

    val reminders: Flow<List<Reminder>> = dataStore.data
        .map { prefs -> decode(prefs[REMINDERS_KEY]) }
        .distinctUntilChanged()

    /**
     * Reads the current list, applies [transform], and saves the result in one atomic step.
     * Returns the list before and after, so alarms can be scheduled or cancelled to match.
     */
    suspend fun updateReminders(transform: (List<Reminder>) -> List<Reminder>): ReminderChange {
        var before: List<Reminder> = emptyList()
        var after: List<Reminder> = emptyList()
        dataStore.edit { prefs ->
            before = decode(prefs[REMINDERS_KEY])
            after = transform(before)
            prefs[REMINDERS_KEY] = encode(after)
        }
        return ReminderChange(before, after)
    }

    private fun decode(raw: String?): List<Reminder> {
        if (raw == null) return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                Reminder(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    triggerAtMillis = o.getLong("at"),
                    completed = o.optBoolean("done", false),
                    mode = ReminderMode.entries.firstOrNull { it.name == o.optString("mode") }
                        ?: ReminderMode.ALARM
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    private fun encode(list: List<Reminder>): String {
        val array = JSONArray()
        list.forEach { r ->
            array.put(
                JSONObject()
                    .put("id", r.id)
                    .put("title", r.title)
                    .put("at", r.triggerAtMillis)
                    .put("done", r.completed)
                    .put("mode", r.mode.name)
            )
        }
        return array.toString()
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode")
        val REMINDERS_KEY = stringPreferencesKey("reminders_json")
    }
}
