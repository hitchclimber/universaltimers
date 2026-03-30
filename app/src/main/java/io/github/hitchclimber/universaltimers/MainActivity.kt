package io.github.hitchclimber.universaltimers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hitchclimber.universaltimers.data.AppDatabase
import io.github.hitchclimber.universaltimers.data.BundleRepository
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.timer.TimerViewModel
import io.github.hitchclimber.universaltimers.ui.EditBundleScreen
import io.github.hitchclimber.universaltimers.ui.HomeScreen
import io.github.hitchclimber.universaltimers.ui.TimerScreen
import io.github.hitchclimber.universaltimers.ui.theme.CatppuccinTheme
import kotlinx.coroutines.launch

private enum class Screen { HOME, TIMER, EDIT }

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result received; no special handling needed.
            // The service will simply be silent if the user denied.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        val db = AppDatabase.getInstance(this)
        val repo = BundleRepository(db.bundleDao())
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val savedDark = prefs.getString("dark_mode", null)
            var darkOverride by remember {
                mutableStateOf(
                    when (savedDark) {
                        "true" -> true
                        "false" -> false
                        else -> null
                    }
                )
            }
            val isDark = darkOverride ?: systemDark

            // Sound preference (default: ON)
            var soundEnabled by remember {
                mutableStateOf(prefs.getBoolean("sound_enabled", true))
            }

            // Keep-screen-on preference (default: OFF)
            var keepScreenOn by remember {
                mutableStateOf(prefs.getBoolean("keep_screen_on", false))
            }

            CatppuccinTheme(darkTheme = isDark) {
                val timerVm: TimerViewModel = viewModel()
                val timerState by timerVm.state.collectAsState()
                val scope = rememberCoroutineScope()

                // Keep SoundManager in sync with the preference
                timerVm.soundManager.enabled = soundEnabled

                val bundles by repo.observeAll().collectAsState(initial = emptyList())
                var selectedBundle by remember { mutableStateOf<TimerBundle?>(null) }
                var screen by remember { mutableStateOf(Screen.HOME) }

                when (screen) {
                    Screen.HOME -> {
                        HomeScreen(
                            bundles = bundles,
                            isDark = isDark,
                            onToggleTheme = {
                                val newDark = !isDark
                                darkOverride = newDark
                                prefs.edit { putString("dark_mode", newDark.toString()) }
                            },
                            onBundleClick = {
                                selectedBundle = it
                                screen = Screen.TIMER
                            },
                            onDeleteBundle = { scope.launch { repo.delete(it) } },
                            onAddClick = {
                                selectedBundle = null
                                screen = Screen.EDIT
                            },
                        )
                    }

                    Screen.TIMER -> {
                        val bundle = selectedBundle ?: return@CatppuccinTheme
                        TimerScreen(
                            bundle = bundle,
                            state = timerState,
                            isSoundEnabled = soundEnabled,
                            isKeepScreenOn = keepScreenOn,
                            onToggleSound = {
                                val newValue = !soundEnabled
                                soundEnabled = newValue
                                prefs.edit { putBoolean("sound_enabled", newValue) }
                            },
                            onToggleKeepScreenOn = {
                                val newValue = !keepScreenOn
                                keepScreenOn = newValue
                                prefs.edit { putBoolean("keep_screen_on", newValue) }
                            },
                            onStart = { timerVm.startBundle(bundle) },
                            onPauseResume = { timerVm.togglePause() },
                            onStop = { timerVm.stop() },
                            onBack = {
                                timerVm.stop()
                                screen = Screen.HOME
                            },
                        )
                    }

                    Screen.EDIT -> {
                        EditBundleScreen(
                            initial = selectedBundle,
                            onSave = { saved ->
                                scope.launch { repo.save(saved) }
                                screen = Screen.HOME
                            },
                            onBack = { screen = Screen.HOME },
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
