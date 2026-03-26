package io.github.hitchclimber.universaltimers.timer

import io.github.hitchclimber.universaltimers.data.TimerBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Application-scoped singleton that holds the shared [TimerEngine] and its state.
 *
 * Both the [TimerViewModel] and [TimerService][io.github.hitchclimber.universaltimers.service.TimerService]
 * observe the same [state] flow, so the UI always reflects the true timer position
 * regardless of whether the activity is in the foreground.
 */
object TimerEngineHolder {

    private val engine = TimerEngine()

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    /** The bundle currently being timed, if any. */
    var currentBundle: TimerBundle? = null
        private set

    private var _onFinished: (() -> Unit)? = null

    fun start(
        scope: CoroutineScope,
        bundle: TimerBundle,
        onFinished: () -> Unit = {},
    ) {
        currentBundle = bundle
        _onFinished = onFinished
        engine.start(
            scope = scope,
            bundle = bundle,
            onTick = { _state.value = it },
            onFinished = {
                _onFinished?.invoke()
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
        currentBundle = null
        _onFinished = null
    }

    /** True when a timer is actively counting (running or paused). */
    val isActive: Boolean
        get() = _state.value.isRunning || _state.value.isPaused
}