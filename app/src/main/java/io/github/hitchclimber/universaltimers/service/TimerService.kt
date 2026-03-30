package io.github.hitchclimber.universaltimers.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.hitchclimber.universaltimers.MainActivity
import io.github.hitchclimber.universaltimers.R
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.timer.TimerEngineHolder
import io.github.hitchclimber.universaltimers.timer.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the timer alive when the app is backgrounded.
 *
 * It observes [TimerEngineHolder.state] and updates the persistent notification
 * every second. When the timer finishes it posts a high-importance completion
 * notification and stops itself.
 */
class TimerService : Service() {

    companion object {
        const val CHANNEL_ONGOING = "timer_ongoing"
        const val CHANNEL_COMPLETE = "timer_complete"
        private const val NOTIFICATION_ID_ONGOING = 1
        private const val NOTIFICATION_ID_COMPLETE = 2

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    /** True when the app's UI is visible to the user. */
    private var appInForeground = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            appInForeground = true
        }

        override fun onStop(owner: LifecycleOwner) {
            appInForeground = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val notification = buildOngoingNotification(TimerEngineHolder.state.value)
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID_ONGOING,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    },
                )
                startObserving()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        observerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Notification channels ──────────────────────────────────────────

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val ongoing = NotificationChannel(
                CHANNEL_ONGOING,
                "Timer Progress",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows the current timer progress while running"
                setShowBadge(false)
            }

            val complete = NotificationChannel(
                CHANNEL_COMPLETE,
                "Timer Complete",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifies when a timer finishes"
            }

            manager.createNotificationChannel(ongoing)
            manager.createNotificationChannel(complete)
        }
    }

    // ── State observer ─────────────────────────────────────────────────

    private fun startObserving() {
        observerJob?.cancel()
        observerJob = serviceScope.launch {
            var lastNotificationSec = -1L
            TimerEngineHolder.state.collect { state ->
                if (state.isFinished) {
                    if (!appInForeground) {
                        showCompletionNotification()
                    }
                    stopSelf()
                    return@collect
                }
                if (!state.isRunning && !state.isPaused && !state.isCountingDown) {
                    stopSelf()
                    return@collect
                }
                if (!appInForeground) {
                    // Throttle notification updates to once per second —
                    // the StateFlow emits every 100 ms but updating the
                    // notification that often is wasteful.
                    val currentSec = state.remainingMs / 1000
                    if (currentSec != lastNotificationSec) {
                        lastNotificationSec = currentSec
                        updateOngoingNotification(state)
                    }
                }
            }
        }
    }

    // ── Notification builders ──────────────────────────────────────────

    private fun contentPendingIntent(): PendingIntent {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildOngoingNotification(state: TimerState): Notification {
        val stepLabel = if (state.isRunning || state.isPaused) {
            val typeTag = if (state.currentStepType == StepType.WORK) "WORK" else "REST"
            "$typeTag - ${state.currentStepLabel}"
        } else {
            "Timer"
        }

        val timeText = formatTime(state.remainingMs)
        val pauseLabel = if (state.isPaused) " (Paused)" else ""

        return NotificationCompat.Builder(this, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$stepLabel$pauseLabel")
            .setContentText(timeText)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent())
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateOngoingNotification(state: TimerState) {
        val notification = buildOngoingNotification(state)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_ONGOING, notification)
    }

    private fun showCompletionNotification() {
        val bundleName = TimerEngineHolder.currentBundle?.name ?: "Timer"
        val notification = NotificationCompat.Builder(this, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Timer Complete")
            .setContentText("$bundleName has finished!")
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_COMPLETE, notification)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms + 999) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}