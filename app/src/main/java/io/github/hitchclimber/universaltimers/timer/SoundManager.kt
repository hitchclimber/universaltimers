package io.github.hitchclimber.universaltimers.timer

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages acoustic signals for timer events using Android's built-in [ToneGenerator].
 *
 * Uses [AudioManager.STREAM_ALARM] so volume follows the system alarm setting.
 * All tones are brief and non-intrusive.
 */
class SoundManager {

    private var toneGenerator: ToneGenerator? = null
    var enabled: Boolean = true

    private fun ensureGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_ALARM, 80)
            } catch (_: RuntimeException) {
                null
            }
        }
        return toneGenerator
    }

    /** Short beep when the timer starts. */
    fun playStart(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    /** Distinct tone when transitioning between steps (WORK <-> REST). */
    fun playStepTransition(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        }
    }

    /** Series of 3 short beeps when the timer finishes. */
    fun playFinished(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            val gen = ensureGenerator() ?: return@launch
            repeat(3) { i ->
                gen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                if (i < 2) delay(250)
            }
        }
    }

    /** Short tick for countdown (future 3-second countdown feature). */
    fun playCountdownTick(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            ensureGenerator()?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
        }
    }

    /** Release the underlying [ToneGenerator] resources. */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}