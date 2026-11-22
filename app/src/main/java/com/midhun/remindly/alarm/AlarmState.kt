package com.midhun.remindly.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Which alarm is ringing right now (or null). The service sets it, the alarm screen watches it
 * and closes itself when the alarm stops. Service and screen live in the same process.
 */
object AlarmState {
    private val _ringing = MutableStateFlow<AlarmSpec?>(null)
    val ringing: StateFlow<AlarmSpec?> = _ringing.asStateFlow()

    fun set(spec: AlarmSpec?) {
        _ringing.value = spec
    }
}
