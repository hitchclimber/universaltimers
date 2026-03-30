package io.github.hitchclimber.universaltimers.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.service.TimerService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that bridges the shared [TimerEngineHolder] with the UI
 * and manages the foreground [TimerService] lifecycle.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager()

    val state: StateFlow<TimerState> = TimerEngineHolder.state

    /** Tracks the previous step key so we can detect step transitions. */
    private var lastStepKey: Triple<Int, Int, Int>? = null

    /** Tracks the last countdown value so we beep once per number. */
    private var lastCountdownValue: Int = 0

    /** Whether the previous state was counting down (to detect countdown → running transition). */
    private var wasCountingDown: Boolean = false

    init {
        // Observe state to play sounds on countdown ticks and step transitions
        viewModelScope.launch {
            state.collect { newState ->
                // Countdown beeps: one short beep per number (3, 2, 1)
                if (newState.isCountingDown && newState.countdownValue != lastCountdownValue) {
                    lastCountdownValue = newState.countdownValue
                    soundManager.playCountdownTick(viewModelScope)
                }

                // Long "go" beep when countdown ends and timer starts running
                if (wasCountingDown && !newState.isCountingDown && newState.isRunning) {
                    soundManager.playStart(viewModelScope)
                }
                wasCountingDown = newState.isCountingDown

                // Step transition sounds
                if (newState.isRunning && !newState.isFinished) {
                    val key = Triple(
                        newState.currentBlockIndex,
                        newState.currentRepetition,
                        newState.currentStepIndex,
                    )
                    if (lastStepKey != null && key != lastStepKey) {
                        soundManager.playStepTransition(viewModelScope)
                    }
                    lastStepKey = key
                }
            }
        }
    }

    fun startBundle(bundle: TimerBundle) {
        val context = getApplication<Application>()
        lastStepKey = null
        lastCountdownValue = 0
        wasCountingDown = false


        TimerEngineHolder.start(
            scope = viewModelScope,
            bundle = bundle,
            onFinished = {
                soundManager.playFinished(viewModelScope)
            },
        )
        TimerService.startService(context)
    }

    fun togglePause() {
        TimerEngineHolder.togglePause()
    }

    fun stop() {
        val context = getApplication<Application>()
        TimerEngineHolder.stop()
        TimerService.stopService(context)
        lastStepKey = null
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop the engine here -- the service keeps it alive
        // when the activity is destroyed but timer is still running.
        soundManager.release()
    }
}