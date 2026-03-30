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

    /** Sustained tone (~1.5 s) when the timer starts running after the countdown. */
    fun playStart(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            val gen = ensureGenerator() ?: return@launch
            // DTMF tones are continuous and respect startTone/stopTone,
            // unlike TONE_PROP_* which are canned patterns.
            gen.startTone(ToneGenerator.TONE_DTMF_S)
            delay(1000)
            gen.stopTone()
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

    /** Short beep for each countdown number (3, 2, 1). */
    fun playCountdownTick(scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Main) {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    /** Release the underlying [ToneGenerator] resources. */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}