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

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun startBundle(bundle: TimerBundle) {
        engine.start(
            scope = viewModelScope,
            bundle = bundle,
            onTick = { _state.value = it },
            onFinished = { /* state already set to isFinished by engine */ },
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
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
    }
}