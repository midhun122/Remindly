package com.midhun.remindly.alarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Remembers which alarms are scheduled, so they can be set again after a reboot, an app update
 * or a permission change. (AlarmManager forgets everything in those cases.)
 */
object AlarmStorage {
    private const val PREFS = "remindly_alarms"
    private const val KEY = "scheduled"

    @Synchronized
    fun all(context: Context): List<AlarmSpec> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                AlarmSpec(o.getString("id"), o.getString("title"), o.getLong("at"), o.optBoolean("loud", true))
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    @Synchronized
    fun save(context: Context, spec: AlarmSpec) {
        write(context, all(context).filterNot { it.id == spec.id } + spec)
    }

    @Synchronized
    fun remove(context: Context, id: String) {
        write(context, all(context).filterNot { it.id == id })
    }

    private fun write(context: Context, list: List<AlarmSpec>) {
        val array = JSONArray()
        list.forEach {
            array.put(
                JSONObject().put("id", it.id).put("title", it.title).put("at", it.triggerAtMillis).put("loud", it.loud)
            )
        }
        prefs(context).edit().putString(KEY, array.toString()).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
