package io.github.hitchclimber.universaltimers.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.service.TimerService
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel that bridges the shared [TimerEngineHolder] with the UI
 * and manages the foreground [TimerService] lifecycle.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    val state: StateFlow<TimerState> = TimerEngineHolder.state

    fun startBundle(bundle: TimerBundle) {
        val context = getApplication<Application>()
        TimerEngineHolder.start(
            scope = viewModelScope,
            bundle = bundle,
            onFinished = {
                // Service observes isFinished and handles its own shutdown
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
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop the engine here -- the service keeps it alive
        // when the activity is destroyed but timer is still running.
    }
}