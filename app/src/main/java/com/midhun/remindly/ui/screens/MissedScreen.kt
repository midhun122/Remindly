package com.midhun.remindly.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.ui.components.EmptyState
import com.midhun.remindly.ui.components.ScreenTitle
import com.midhun.remindly.ui.components.cardSurface
import com.midhun.remindly.util.dayLabel
import com.midhun.remindly.util.formatTime

/** Reminders whose time passed without being completed. Nothing here is lost silently. */
@Composable
fun MissedScreen(
    now: Long,
    missed: List<Reminder>,
    onDone: (Reminder) -> Unit,
    onReschedule: (Reminder) -> Unit,
    onDismiss: (Reminder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                ScreenTitle(
                    title = "Missed",
                    subtitle = when (missed.size) {
                        0 -> "Nothing slipped past you."
                        1 -> "1 reminder needs a decision."
                        else -> "${missed.size} reminders need a decision."
                    }
                )
            }
        }

        if (missed.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Check,
                    title = "All clear",
                    body = "Reminders you don't finish will wait here until you complete, reschedule or dismiss them."
                )
            }
        }

        items(missed, key = { it.id }) { reminder ->
            MissedCard(
                reminder = reminder,
                now = now,
                onDone = { onDone(reminder) },
                onReschedule = { onReschedule(reminder) },
                onDismiss = { onDismiss(reminder) }
            )
        }
    }
}

@Composable
private fun MissedCard(
    reminder: Reminder,
    now: Long,
    onDone: () -> Unit,
    onReschedule: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Missed ${dayLabel(reminder.triggerAtMillis, now).lowercase()}, ${formatTime(reminder.triggerAtMillis)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.error
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss reminder",
                    tint = colors.onSurfaceVariant
                )
            }
        }

        Text(
            text = reminder.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onReschedule,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, colors.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface)
            ) {
                Text("Reschedule", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
