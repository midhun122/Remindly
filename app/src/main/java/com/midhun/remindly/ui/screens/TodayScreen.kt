package com.midhun.remindly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.ui.components.EmptyState
import com.midhun.remindly.ui.components.ReminderRow
import com.midhun.remindly.ui.components.SectionHeader
import com.midhun.remindly.ui.components.UpNextCard
import com.midhun.remindly.util.formatDateShort

@Composable
fun TodayScreen(
    now: Long,
    todayReminders: List<Reminder>,
    upNext: Reminder?,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onSnooze: (Reminder) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val doneCount = todayReminders.count { it.completed }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "remindly.",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp
                    ),
                    color = colors.onBackground
                )
                Text(
                    text = formatDateShort(now),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)) {
                Text(
                    text = "Let's make it count.",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Spacer(
                    Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
            }
        }

        item {
            UpNextCard(
                reminder = upNext,
                now = now,
                onDone = { upNext?.let(onToggle) },
                onSnooze = { upNext?.let(onSnooze) },
                onClick = { upNext?.let(onEdit) }
            )
        }

        item {
            Column {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    title = "Today",
                    trailing = if (todayReminders.isEmpty()) null
                    else "$doneCount of ${todayReminders.size} done"
                )
            }
        }

        if (todayReminders.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Today,
                    title = "Nothing left today",
                    body = "Tap New reminder to add something for later."
                )
            }
        } else {
            items(todayReminders, key = { it.id }) { reminder ->
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
