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

    init {
        // Observe state to play sounds on step transitions
        viewModelScope.launch {
            state.collect { newState ->
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
        soundManager.playStart(viewModelScope)

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