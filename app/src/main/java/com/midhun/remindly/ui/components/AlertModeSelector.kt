package com.midhun.remindly.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.ReminderMode

/** Two-way switch in the add sheet: ring like an alarm, or just send a notification. */
@Composable
fun AlertModeSelector(
    selected: ReminderMode,
    onSelect: (ReminderMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val options = listOf(
        Triple(ReminderMode.ALARM, "Alarm", Icons.Outlined.Alarm),
        Triple(ReminderMode.NOTIFICATION, "Notification", Icons.Outlined.Notifications)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant)
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (mode, label, icon) ->
                val isSelected = mode == selected
                val container by animateColorAsState(
                    targetValue = if (isSelected) colors.primary else Color.Transparent,
                    label = "modeOption"
                )
                val content = if (isSelected) colors.onPrimary else colors.onSurfaceVariant

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(container)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onSelect(mode) }
                        )
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.labelLarge, color = content)
                }
            }
        }
        Text(
            text = if (selected == ReminderMode.ALARM) {
                "Rings loudly and opens over the lock screen."
            } else {
                "A quiet banner with the usual notification sound."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}
