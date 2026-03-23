package io.github.hitchclimber.universaltimers

import android.os.Bundle
import androidx.activity.ComponentActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)
        val repo = BundleRepository(db.bundleDao())

        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkOverride by remember { mutableStateOf<Boolean?>(null) }
            val isDark = darkOverride ?: systemDark

            CatppuccinTheme(darkTheme = isDark) {
                val timerVm: TimerViewModel = viewModel()
                val timerState by timerVm.state.collectAsState()
                val scope = rememberCoroutineScope()

                val bundles by repo.observeAll().collectAsState(initial = emptyList())
                var selectedBundle by remember { mutableStateOf<TimerBundle?>(null) }
                var screen by remember { mutableStateOf(Screen.HOME) }

                when (screen) {
                    Screen.HOME -> {
                        HomeScreen(
                            bundles = bundles,
                            isDark = isDark,
                            onToggleTheme = { darkOverride = !isDark },
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
}
