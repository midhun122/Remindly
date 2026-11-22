package com.midhun.remindly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.ui.components.EmptyState
import com.midhun.remindly.ui.components.ReminderRow
import com.midhun.remindly.ui.components.ScreenTitle
import com.midhun.remindly.util.dayLabel
import com.midhun.remindly.util.millisToDateTime

@Composable
fun UpcomingScreen(
    now: Long,
    upcoming: List<Reminder>,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // Sorted by time first, so grouping keeps the days in order.
    val grouped = upcoming
        .sortedBy { it.triggerAtMillis }
        .groupBy { millisToDateTime(it.triggerAtMillis).toLocalDate() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                ScreenTitle(
                    title = "Upcoming",
                    subtitle = if (upcoming.isEmpty()) "Nothing planned beyond today."
                    else "${upcoming.size} planned after today"
                )
            }
        }

        if (upcoming.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Event,
                    title = "Your next days are clear",
                    body = "Reminders you set for tomorrow or later show up here."
                )
            }
        }

        grouped.forEach { (date, reminders) ->
            item(key = "day-$date") {
                Text(
                    text = dayLabel(reminders.first().triggerAtMillis, now),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            items(reminders, key = { it.id }) { reminder ->
                ReminderRow(
                    reminder = reminder,
                    now = now,
                    showDay = false,
                    onToggle = { onToggle(reminder) },
                    onClick = { onEdit(reminder) }
                )
            }
        }
    }
}
