package com.midhun.remindly.alarm

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.midhun.remindly.ui.theme.InkOnYellow
import com.midhun.remindly.ui.theme.RemindlyTheme
import com.midhun.remindly.ui.theme.Yellow
import com.midhun.remindly.util.formatTime

/**
 * The full-screen alarm. Android shows it over the lock screen when the full-screen permission is
 * granted; otherwise the same actions are available from the notification.
 */
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )

        val fromIntent = intent.toAlarmSpec()

        setContent {
            val ringing by AlarmState.ringing.collectAsState()

            // When the alarm stops (dismissed, snoozed, or timed out) this screen closes itself.
            LaunchedEffect(ringing) {
                if (ringing == null) finish()
            }

            RemindlyTheme(darkTheme = false) {
                AlarmScreen(
                    spec = ringing ?: fromIntent,
                    onDismiss = {
                        startService(AlarmService.dismissIntent(this))
                        finish()
                    },
                    onSnooze = {
                        startService(
                            AlarmService.snoozeIntent(this, AlarmService.DEFAULT_SNOOZE_MINUTES)
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
private fun AlarmScreen(
    spec: AlarmSpec?,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Yellow)
            .systemBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Alarm",
            style = MaterialTheme.typography.titleMedium,
            color = InkOnYellow.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (spec != null) formatTime(spec.triggerAtMillis) else "",
            style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp),
            color = InkOnYellow
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = spec?.title ?: "Reminder",
            style = MaterialTheme.typography.headlineMedium,
            color = InkOnYellow,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = InkOnYellow, contentColor = Yellow)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.5.dp, InkOnYellow),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InkOnYellow)
            ) {
                Text("Snooze ${AlarmService.DEFAULT_SNOOZE_MINUTES} min", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "Stops by itself after 60 seconds",
                style = MaterialTheme.typography.bodySmall,
                color = InkOnYellow.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
