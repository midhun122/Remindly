package com.midhun.remindly.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue

enum class AppTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Today("Today", Icons.Filled.Today, Icons.Outlined.Today),
    Upcoming("Upcoming", Icons.Filled.Event, Icons.Outlined.Event),
    Missed("Missed", Icons.Filled.NotificationImportant, Icons.Outlined.NotificationImportant),
    Settings("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

/**
 * Compact bottom bar: 60dp tall plus the system gesture/navigation inset.
 * Only the icon sits inside the yellow pill, the label stays below it.
 */
@Composable
fun RemindlyBottomBar(
    selected: AppTab,
    missedCount: Int,
    onSelect: (AppTab) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        HorizontalDivider(color = colors.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                BarItem(
                    tab = tab,
                    isSelected = tab == selected,
                    badgeCount = if (tab == AppTab.Missed) missedCount else 0,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    tab: AppTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else Color.Transparent,
        label = "tabPill"
    )
    val contentColor = if (isSelected) colors.onPrimary else colors.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = colors.error, contentColor = colors.onError) {
                            Text(badgeCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
        )
    }
}
