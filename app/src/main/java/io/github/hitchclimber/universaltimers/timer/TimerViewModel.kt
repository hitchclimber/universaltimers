package io.github.hitchclimber.universaltimers.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hitchclimber.universaltimers.data.TimerBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel that bridges the [TimerEngine] with the UI.
 */
class TimerViewModel : ViewModel() {

    private val engine = TimerEngine()
    val soundManager = SoundManager()

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    /** Tracks the previous step key so we can detect step transitions. */
    private var lastStepKey: Triple<Int, Int, Int>? = null

    fun startBundle(bundle: TimerBundle) {
        lastStepKey = null
        soundManager.playStart(viewModelScope)

        engine.start(
            scope = viewModelScope,
            bundle = bundle,
            onTick = { newState ->
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
                _state.value = newState
            },
            onFinished = {
                soundManager.playFinished(viewModelScope)
            },
        )
    }

    fun togglePause() {
        if (_state.value.isPaused) {
            engine.resume()
        } else {
            engine.pause()
        }
    }

    fun stop() {
        engine.stop()
        _state.value = TimerState()
        lastStepKey = null
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
        soundManager.release()
    }
}