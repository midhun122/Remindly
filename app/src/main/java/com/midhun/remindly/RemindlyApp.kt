package com.midhun.remindly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.data.RemindlyStore
import com.midhun.remindly.data.ThemeMode
import com.midhun.remindly.ui.components.AppTab
import com.midhun.remindly.ui.components.ReminderSheet
import com.midhun.remindly.ui.components.RemindlyBottomBar
import com.midhun.remindly.ui.screens.MissedScreen
import com.midhun.remindly.ui.screens.SettingsScreen
import com.midhun.remindly.ui.screens.TodayScreen
import com.midhun.remindly.ui.screens.UpcomingScreen
import com.midhun.remindly.util.millisToDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface EditorState {
    data object New : EditorState
    data class Edit(val reminder: Reminder) : EditorState
}

private const val SNOOZE_MILLIS = 10 * 60 * 1000L

@Composable
fun RemindlyApp(
    store: RemindlyStore,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    resumeTick: Int
) {
    val loaded by store.reminders.collectAsState(initial = null)
    val reminders = loaded

    if (reminders == null) {
        // Reminders are still loading from the phone; show a blank themed screen for a moment.
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    } else {
        RemindlyScaffold(
            store = store,
            reminders = reminders,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            resumeTick = resumeTick
        )
    }
}

@Composable
private fun RemindlyScaffold(
    store: RemindlyStore,
    reminders: List<Reminder>,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    resumeTick: Int
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var tab by rememberSaveable { mutableStateOf(AppTab.Today) }
    var editor by remember { mutableStateOf<EditorState?>(null) }

    // "now" refreshes every 30 seconds so reminders move to Missed and countdowns stay correct.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = System.currentTimeMillis()
        }
    }

    val missed = remember(reminders, now) {
        reminders
            .filter { !it.completed && it.triggerAtMillis < now }
            .sortedBy { it.triggerAtMillis }
    }
    val todayList = remember(reminders, now) {
        val today = millisToDateTime(now).toLocalDate()
        reminders
            .filter { r ->
                millisToDateTime(r.triggerAtMillis).toLocalDate() == today &&
                    (r.completed || r.triggerAtMillis >= now)
            }
            .sortedWith(compareBy<Reminder>({ it.completed }, { it.triggerAtMillis }))
    }
    val upcoming = remember(reminders, now) {
        val today = millisToDateTime(now).toLocalDate()
        reminders.filter { r ->
            !r.completed &&
                r.triggerAtMillis >= now &&
                millisToDateTime(r.triggerAtMillis).toLocalDate().isAfter(today)
        }
    }
    val upNext = remember(reminders, now) {
        reminders
            .filter { !it.completed && it.triggerAtMillis >= now }
            .minByOrNull { it.triggerAtMillis }
    }

    // ----- actions: each one changes the saved list; the screens update automatically -----
    fun update(transform: (List<Reminder>) -> List<Reminder>) {
        scope.launch { store.updateReminders(transform) }
    }

    fun toggle(target: Reminder) = update { list ->
        list.map { if (it.id == target.id) it.copy(completed = !it.completed) else it }
    }

    fun snooze(target: Reminder) = update { list ->
        list.map {
            if (it.id == target.id) it.copy(triggerAtMillis = it.triggerAtMillis + SNOOZE_MILLIS) else it
        }
    }

    fun remove(target: Reminder) {
        scope.launch {
            store.updateReminders { list -> list.filterNot { it.id == target.id } }
            val result = snackbar.showSnackbar(
                message = "Reminder removed",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                store.updateReminders { list -> list + target }
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            RemindlyBottomBar(
                selected = tab,
                missedCount = missed.size,
                onSelect = { tab = it }
            )
        },
        floatingActionButton = {
            if (tab == AppTab.Today || tab == AppTab.Upcoming) {
                ExtendedFloatingActionButton(
                    onClick = { editor = EditorState.New },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    ),
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New reminder", style = MaterialTheme.typography.labelLarge) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                AppTab.Today -> TodayScreen(
                    now = now,
                    todayReminders = todayList,
                    upNext = upNext,
                    onToggle = { toggle(it) },
                    onEdit = { editor = EditorState.Edit(it) },
                    onSnooze = { snooze(it) }
                )

                AppTab.Upcoming -> UpcomingScreen(
                    now = now,
                    upcoming = upcoming,
                    onToggle = { toggle(it) },
                    onEdit = { editor = EditorState.Edit(it) }
                )

                AppTab.Missed -> MissedScreen(
                    now = now,
                    missed = missed,
                    onDone = { toggle(it) },
                    onReschedule = { editor = EditorState.Edit(it) },
                    onDismiss = { remove(it) }
                )

                AppTab.Settings -> SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    resumeTick = resumeTick
                )
            }
        }
    }

    val currentEditor = editor
    if (currentEditor != null) {
        val existing = (currentEditor as? EditorState.Edit)?.reminder
        ReminderSheet(
            existing = existing,
            now = now,
            onDismiss = { editor = null },
            onSave = { title, triggerAt ->
                if (existing == null) {
                    update { list -> list + Reminder(title = title, triggerAtMillis = triggerAt) }
                } else {
                    update { list ->
                        list.map {
                            if (it.id == existing.id) it.copy(title = title, triggerAtMillis = triggerAt) else it
                        }
                    }
                }
                editor = null
            },
            onDelete = if (existing != null) {
                {
                    remove(existing)
                    editor = null
                }
            } else {
                null
            }
        )
    }
}
