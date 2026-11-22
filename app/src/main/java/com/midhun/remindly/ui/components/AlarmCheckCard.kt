package com.midhun.remindly.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.midhun.remindly.alarm.AlarmPermissions
import com.midhun.remindly.alarm.AlarmScheduler
import com.midhun.remindly.alarm.AlarmSpec
import com.midhun.remindly.alarm.AlarmStorage
import com.midhun.remindly.util.formatTimeWithSeconds
import kotlinx.coroutines.delay

/**
 * Phase 0 alarm spike, and the first version of Reliability Guard:
 * shows what this phone still needs, and lets you ring a real test alarm.
 * [resumeTick] changes whenever the app comes back to the front, so statuses refresh after
 * the user returns from a settings screen.
 */
@Composable
fun AlarmCheckCard(resumeTick: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val readiness = remember(resumeTick) { AlarmPermissions.readiness(context) }
    var pending by remember(resumeTick) {
        mutableStateOf(AlarmStorage.all(context).firstOrNull { it.id == AlarmScheduler.TEST_ALARM_ID })
    }
    var askedForNotifications by rememberSaveable { mutableStateOf(false) }

    // The alarm volume changes with the volume buttons, which don't pause or resume the app,
    // so while this card is on screen it re-reads the level every second.
    var volumePercent by remember { mutableIntStateOf(AlarmPermissions.alarmVolumePercent(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            volumePercent = AlarmPermissions.alarmVolumePercent(context)
            delay(1000)
        }
    }
    val volumeOk = volumePercent >= AlarmPermissions.MIN_COMFORTABLE_VOLUME_PERCENT
    val allGood = readiness.allGood && volumeOk

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { askedForNotifications = true }

    fun fixNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !askedForNotifications) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.startActivity(AlarmPermissions.appNotificationSettingsIntent(context))
        }
    }

    fun scheduleTest(seconds: Int) {
        when {
            !readiness.notifications ->
                Toast.makeText(context, "Allow notifications first", Toast.LENGTH_LONG).show()

            !readiness.exactAlarms ->
                Toast.makeText(context, "Allow alarms & reminders first", Toast.LENGTH_LONG).show()

            else -> {
                val spec = AlarmSpec(
                    id = AlarmScheduler.TEST_ALARM_ID,
                    title = "Test alarm",
                    triggerAtMillis = System.currentTimeMillis() + seconds * 1000L
                )
                if (AlarmScheduler.schedule(context, spec)) {
                    pending = spec
                } else {
                    Toast.makeText(context, "Could not schedule the alarm", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Alarm check",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Text(
                text = if (allGood) "This phone is ready to ring alarms."
                else "Fix the items below so alarms can ring.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        CheckRow(
            title = "Notifications",
            detail = "Needed to show and control the ringing alarm.",
            ok = readiness.notifications,
            actionLabel = "Allow",
            onAction = { fixNotifications() }
        )
        CheckRow(
            title = "Alarms & reminders",
            detail = "Lets Remindly ring at the exact minute.",
            ok = readiness.exactAlarms,
            actionLabel = "Allow",
            onAction = { context.startActivity(AlarmPermissions.exactAlarmSettingsIntent(context)) }
        )
        CheckRow(
            title = "Full-screen alarm",
            detail = "Lets the alarm open over the lock screen.",
            ok = readiness.fullScreen,
            actionLabel = "Allow",
            onAction = { context.startActivity(AlarmPermissions.fullScreenSettingsIntent(context)) }
        )
        CheckRow(
            title = "Alarm volume",
            detail = if (volumePercent == 0) {
                "Alarm volume is off, so the alarm would be silent."
            } else {
                "Alarm volume is at $volumePercent%. It may be too quiet to hear."
            },
            okDetail = "Alarm volume is at $volumePercent%.",
            ok = volumeOk,
            actionLabel = "Open",
            onAction = { context.startActivity(AlarmPermissions.soundSettingsIntent()) }
        )

        Text(
            text = "Some phones (Xiaomi, Oppo, Vivo, Realme, Samsung) stop apps in the background. " +
                "If a test alarm doesn't ring, open the app settings and set battery to No restrictions.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        OutlinedButton(
            onClick = { context.startActivity(AlarmPermissions.appDetailsIntent(context)) },
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text("Open app settings", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Test it",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface
        )
        Text(
            text = "Start a test alarm, then lock your phone and swipe Remindly away from recent apps.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { scheduleTest(10) },
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) { Text("Ring in 10 seconds", style = MaterialTheme.typography.labelLarge) }
            Button(
                onClick = { scheduleTest(60) },
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) { Text("In 1 minute", style = MaterialTheme.typography.labelLarge) }
        }

        val scheduled = pending
        if (scheduled != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Test alarm set for ${formatTimeWithSeconds(scheduled.triggerAtMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        AlarmScheduler.cancel(context, AlarmScheduler.TEST_ALARM_ID)
                        pending = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface)
                ) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun CheckRow(
    title: String,
    detail: String,
    ok: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    okDetail: String? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (ok) colors.primary else colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            if (ok) {
                Icon(Icons.Filled.Check, contentDescription = "Ready", tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Needs attention", tint = colors.error, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Text(
                text = if (ok) (okDetail ?: "Ready") else detail,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        if (!ok) {
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 14.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            }
        }
    }
}
