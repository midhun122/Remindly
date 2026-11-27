package com.midhun.remindly.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.Reminder
import com.midhun.remindly.data.ReminderMode
import com.midhun.remindly.util.dayLabel
import com.midhun.remindly.util.formatTime

/** A single reminder. Tap the circle to complete it, tap anywhere else to edit it. */
@Composable
fun ReminderRow(
    reminder: Reminder,
    now: Long,
    showDay: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val done = reminder.completed
    val time = formatTime(reminder.triggerAtMillis)
    val whenText = if (showDay) "${dayLabel(reminder.triggerAtMillis, now)}, $time" else time

    Row(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface()
            .clickable(onClickLabel = "Edit reminder", onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(checked = done, onToggle = onToggle)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (done) colors.onSurfaceVariant else colors.onSurface,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = whenText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        Icon(
            imageVector = if (reminder.mode == ReminderMode.ALARM) Icons.Outlined.Alarm
            else Icons.Outlined.Notifications,
            contentDescription = if (reminder.mode == ReminderMode.ALARM) "Alarm" else "Notification",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Round completion control with a 48dp touch target. */
@Composable
fun CheckCircle(checked: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val fill by animateColorAsState(
        targetValue = if (checked) colors.primary else Color.Transparent,
        label = "checkFill"
    )
    val ring by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.outline,
        label = "checkRing"
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, ring, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
