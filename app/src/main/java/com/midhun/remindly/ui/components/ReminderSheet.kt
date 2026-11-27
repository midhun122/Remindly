package com.midhun.remindly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.data.ReminderMode
import com.midhun.remindly.util.dayLabel
import com.midhun.remindly.util.formatTime
import com.midhun.remindly.util.millisToDateTime
import com.midhun.remindly.util.toEpochMillis
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * One sheet for adding, editing and rescheduling.
 *  - [existing] == null: new reminder
 *  - [existing] is overdue and not completed: reschedule (starts at the next full hour)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSheet(
    existing: Reminder?,
    now: Long,
    onDismiss: () -> Unit,
    onSave: (title: String, triggerAtMillis: Long, mode: ReminderMode) -> Unit,
    onDelete: (() -> Unit)?
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isReschedule = existing != null && !existing.completed && existing.triggerAtMillis < now
    val nowDateTime = millisToDateTime(now).withSecond(0).withNano(0)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var dateTime by remember {
        mutableStateOf(
            if (existing == null || isReschedule) {
                nowDateTime.plusHours(1).withMinute(0)
            } else {
                millisToDateTime(existing.triggerAtMillis)
            }
        )
    }
    var mode by remember { mutableStateOf(existing?.mode ?: ReminderMode.ALARM) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isPast = dateTime.toEpochMillis() <= now
    val canSave = title.isNotBlank() && (!isPast || existing?.completed == true)
    val save = { if (canSave) onSave(title.trim(), dateTime.toEpochMillis(), mode) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (existing == null) focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when {
                    existing == null -> "New reminder"
                    isReschedule -> "Reschedule"
                    else -> "Edit reminder"
                },
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )

            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "What do you want to remember?",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurfaceVariant
                        )
                    },
                    textStyle = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = colors.onSurface,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    )
                )
                HorizontalDivider(color = colors.outlineVariant)
            }

            val quickPicks = listOf(
                "In 1 hour" to nowDateTime.plusHours(1),
                "This evening" to nowDateTime.toLocalDate().atTime(18, 0).let {
                    if (it.isAfter(nowDateTime)) it else it.plusDays(1)
                },
                "Tomorrow morning" to nowDateTime.toLocalDate().plusDays(1).atTime(9, 0)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPicks.forEach { (label, target) ->
                    AssistChip(
                        onClick = { dateTime = target },
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(labelColor = colors.onSurface)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerField(
                    icon = Icons.Outlined.Event,
                    text = dayLabel(dateTime.toEpochMillis(), now),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                PickerField(
                    icon = Icons.Outlined.Schedule,
                    text = formatTime(dateTime),
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            AlertModeSelector(selected = mode, onSelect = { mode = it })

            if (isPast && existing?.completed != true) {
                Text(
                    text = "Choose a time in the future.",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.error
                )
            }

            Button(
                onClick = { save() },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.surfaceVariant,
                    disabledContentColor = colors.onSurfaceVariant
                )
            ) {
                Text(
                    text = when {
                        existing == null -> "Add reminder"
                        isReschedule -> "Reschedule"
                        else -> "Save changes"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (onDelete != null) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete reminder", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showDatePicker) {
        RemindlyDateDialog(
            initial = dateTime.toLocalDate(),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                dateTime = LocalDateTime.of(date, dateTime.toLocalTime())
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        RemindlyTimeDialog(
            initial = dateTime,
            is24Hour = false,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                dateTime = dateTime.withHour(hour).withMinute(minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun PickerField(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceVariant)
            .border(1.dp, colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindlyTimeDialog(
    initial: LocalDateTime,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = is24Hour
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select time",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                TimePicker(
                    state = state,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface)
                    ) { Text("Cancel") }
                    TextButton(
                        onClick = { onConfirm(state.hour, state.minute) },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface)
                    ) { Text("OK") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindlyDateDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // The date picker works in UTC midnight, so convert carefully in both directions.
    val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis
        }
    )
    val pickerColors = DatePickerDefaults.colors(
        containerColor = colors.surface,
        todayContentColor = colors.onSurface,
        todayDateBorderColor = colors.onSurface
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val picked = state.selectedDateMillis
                    if (picked != null) {
                        onConfirm(Instant.ofEpochMilli(picked).atZone(ZoneOffset.UTC).toLocalDate())
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface)
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface)
            ) { Text("Cancel") }
        },
        colors = pickerColors
    ) {
        DatePicker(state = state, colors = pickerColors)
    }
}
