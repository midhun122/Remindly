package com.midhun.remindly

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.midhun.remindly.data.RemindlyStore
import com.midhun.remindly.data.ThemeMode
import com.midhun.remindly.ui.theme.RemindlyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    // Goes up every time the app returns to the front, so permission statuses on the
    // Settings screen refresh after the user comes back from a system settings page.
    private var resumeTick by mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        resumeTick++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = RemindlyStore(applicationContext)
        // Read the saved theme before the first frame so the app never flashes the wrong theme.
        val initialMode = runBlocking { store.themeMode.first() }

        setContent {
            val scope = rememberCoroutineScope()
            val themeMode by store.themeMode.collectAsState(initial = initialMode)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Draw behind the status and navigation bars, with icons that match the theme.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                )
                onDispose {}
            }

            RemindlyTheme(darkTheme = darkTheme) {
                RemindlyApp(
                    store = store,
                    themeMode = themeMode,
                    onThemeModeChange = { mode -> scope.launch { store.setThemeMode(mode) } },
                    resumeTick = resumeTick
                )
            }
        }
    }
}
