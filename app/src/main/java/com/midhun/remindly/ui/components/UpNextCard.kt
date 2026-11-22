package com.midhun.remindly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.util.dayLabel
import com.midhun.remindly.util.formatTime
import com.midhun.remindly.util.relativeIn

/**
 * The one yellow card on the screen: the reminder that is due next.
 * Dark text on yellow in both themes, with Done and Snooze right on the card.
 */
@Composable
fun UpNextCard(
    reminder: Reminder?,
    now: Long,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    if (reminder == null) {
        EmptyState(
            icon = Icons.Filled.Check,
            title = "You're all caught up",
            body = "Nothing is waiting. Add a reminder when something comes up.",
            modifier = modifier
        )
        return
    }

    val whenText = relativeIn(reminder.triggerAtMillis, now)
        ?: dayLabel(reminder.triggerAtMillis, now)
    val dayAndTime = if (dayLabel(reminder.triggerAtMillis, now) == "Today") {
        formatTime(reminder.triggerAtMillis)
    } else {
        "${dayLabel(reminder.triggerAtMillis, now)}, ${formatTime(reminder.triggerAtMillis)}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.primary)
            .clickable(onClickLabel = "Edit reminder", onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Up next",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = whenText,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary
            )
        }

        Text(
            text = reminder.title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = colors.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = dayAndTime,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimary.copy(alpha = 0.8f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onDone,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.onPrimary,
                    contentColor = colors.primary
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                border = BorderStroke(1.dp, colors.onPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onPrimary)
            ) {
                Icon(Icons.Outlined.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Snooze 10 min", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
