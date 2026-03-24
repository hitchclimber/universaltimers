package io.github.hitchclimber.universaltimers.timer

import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.data.TimerStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TICK_MS = 100L

/**
 * Represents the current state of the timer playback.
 */
data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    /** Index of the current block within the bundle */
    val currentBlockIndex: Int = 0,
    /** Current repetition within the current block (0-based) */
    val currentRepetition: Int = 0,
    /** Index of the current step within the current block */
    val currentStepIndex: Int = 0,
    /** Label of the current step */
    val currentStepLabel: String = "",
    /** Type of the current step (WORK/REST) */
    val currentStepType: StepType = StepType.WORK,
    /** Remaining time for the current step in milliseconds */
    val remainingMs: Long = 0L,
    /** Total duration of the current step in milliseconds (after delta applied) */
    val totalStepMs: Long = 0L,
)

/**
 * Core timer engine that drives countdown logic.
 *
 * Iterates through a [TimerBundle]'s blocks, repetitions, and steps,
 * counting down each step's duration and applying deltas on repeats.
 *
 * Emits [TimerState] updates via a callback.
 */
class TimerEngine {
    private var job: Job? = null

    // Pause support: a mutex that the tick loop must hold.
    // pause() locks it (blocking the loop), resume() unlocks it.
    private val pauseMutex = Mutex()
    @Volatile private var paused = false

    /**
     * Start running the given bundle from the beginning.
     */
    fun start(
        scope: CoroutineScope,
        bundle: TimerBundle,
        onTick: (TimerState) -> Unit,
        onFinished: () -> Unit,
    ) {
        stop() // cancel any previous run
        paused = false

        job = scope.launch {
            for ((blockIndex, block) in bundle.blocks.withIndex()) {
                for (rep in 0 until block.repetitions) {
                    for ((stepIndex, step) in block.steps.withIndex()) {
                        // Skip the final REST step if no more work follows
                        val isLastStep = stepIndex == block.steps.lastIndex
                        val isLastRep = rep == block.repetitions - 1
                        val isLastBlock = blockIndex == bundle.blocks.lastIndex
                        if (step.type == StepType.REST && isLastStep && isLastRep && isLastBlock) {
                            continue
                        }

                        val durationMs = computeDuration(step, rep)

                        val baseState = TimerState(
                            isRunning = true,
                            currentBlockIndex = blockIndex,
                            currentRepetition = rep,
                            currentStepIndex = stepIndex,
                            currentStepLabel = step.label.ifEmpty { step.type.name },
                            currentStepType = step.type,
                            totalStepMs = durationMs,
                        )

                        countDown(durationMs, baseState, onTick)
                    }
                }
            }
            onTick(TimerState(isFinished = true))
            onFinished()
        }
    }

    fun pause() {
        if (!paused && job?.isActive == true) {
            paused = true
            pauseMutex.tryLock() // acquire lock so tick loop blocks
        }
    }

    fun resume() {
        if (paused) {
            paused = false
            if (pauseMutex.isLocked) {
                pauseMutex.unlock()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        paused = false
        // make sure mutex is unlocked for next run
        if (pauseMutex.isLocked) {
            try { pauseMutex.unlock() } catch (_: IllegalStateException) {}
        }
    }

    /**
     * Compute effective duration for a step at a given repetition.
     * Applies delta per repetition, clamped to minMs.
     */
    private fun computeDuration(step: TimerStep, repetition: Int): Long {
        val base = step.baseDurationMs
        val delta = step.deltaMs * repetition
        val computed = base + delta
        return maxOf(computed, step.minMs)
    }

    /**
     * Count down [durationMs], emitting state via [onTick] every [TICK_MS].
     * Respects pause by waiting on [pauseMutex].
     */
    private suspend fun countDown(
        durationMs: Long,
        baseState: TimerState,
        onTick: (TimerState) -> Unit,
    ) {
        var remaining = durationMs
        // Emit initial state for this step
        onTick(baseState.copy(remainingMs = remaining, isPaused = paused))

        while (remaining > 0) {
            // If paused, this will block until resume() unlocks the mutex
            if (paused) {
                onTick(baseState.copy(remainingMs = remaining, isPaused = true))
                pauseMutex.withLock { /* just wait for unlock */ }
                // Re-emit as running after resume
                onTick(baseState.copy(remainingMs = remaining, isPaused = false))
            }

            delay(TICK_MS)
            remaining = maxOf(0, remaining - TICK_MS)
            onTick(baseState.copy(remainingMs = remaining, isPaused = paused))
        }
    }
}