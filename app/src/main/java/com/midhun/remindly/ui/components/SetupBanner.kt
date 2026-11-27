package com.midhun.remindly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Shown on Today while the phone still blocks alarms. Tapping it opens Settings. */
@Composable
fun SetupBanner(onSetup: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.errorContainer)
            .clickable(onClickLabel = "Open settings", onClick = onSetup)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Alarms need a quick setup",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onErrorContainer
            )
            Text(
                text = "Allow notifications and alarms so your reminders can ring.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onErrorContainer
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Set up",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onErrorContainer
        )
    }
}
