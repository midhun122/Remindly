package com.midhun.remindly.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.midhun.remindly.data.ThemeMode
import com.midhun.remindly.ui.components.AlarmCheckCard
import com.midhun.remindly.ui.components.ScreenTitle
import com.midhun.remindly.ui.components.cardSurface

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    resumeTick: Int
) {
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Spacer(Modifier.height(4.dp))
                ScreenTitle(title = "Settings", subtitle = "Make Remindly work your way.")
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardSurface()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                ThemeSelector(selected = themeMode, onSelect = onThemeModeChange)
                Text(
                    text = "System follows your phone's light or dark setting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        item {
            AlarmCheckCard(resumeTick = resumeTick)
        }

        item {
            SettingRow(
                icon = Icons.Outlined.Info,
                title = "About Remindly",
                subtitle = "Version 1.0. Built for follow-through.",
                tag = null
            )
        }
    }
}

@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val options = listOf(
        Triple(ThemeMode.SYSTEM, "System", Icons.Outlined.SettingsBrightness),
        Triple(ThemeMode.LIGHT, "Light", Icons.Outlined.LightMode),
        Triple(ThemeMode.DARK, "Dark", Icons.Outlined.DarkMode)
    )

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
                label = "themeOption"
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
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String?
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (tag != null) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
