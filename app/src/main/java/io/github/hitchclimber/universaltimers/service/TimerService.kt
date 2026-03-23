package io.github.hitchclimber.universaltimers.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Foreground service that keeps the timer alive when the app is backgrounded.
 *
 * Implement this once the basic timer works in-app. The service should:
 * - Own the [TimerEngine] (or receive commands from the ViewModel)
 * - Show a persistent notification with current step & time
 * - Update the notification on each tick
 * - Stop itself when the timer finishes or user stops it
 */
class TimerService : Service() {

    override fun onCreate() {
        super.onCreate()
        // TODO: Create notification channel
        // TODO: Initialize timer engine or bind to shared instance
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Parse intent extras (bundle ID, action: start/pause/resume/stop)
        // TODO: Call startForeground() with notification
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return binder if using bound service pattern, or null for started-only
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: Clean up timer, remove notification
    }
}