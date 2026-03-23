package io.github.hitchclimber.universaltimers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBlock
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.timer.TimerState

/**
 * Combined timer + overview screen.
 * Top half: current countdown, controls (start/pause/stop).
 * Bottom half: step list for the current block showing what's done, active, and upcoming.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    bundle: TimerBundle,
    state: TimerState,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bundle.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Top half: Timer display + controls ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (state.isFinished) {
                    Text("Done!", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Back") }
                } else if (state.isRunning) {
                    // Set indicator (block progress)
                    Text(
                        text = "Set ${state.currentRepetition + 1} / ${bundle.blocks.getOrNull(state.currentBlockIndex)?.repetitions?.toInt() ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current step type label
                    Text(
                        text = state.currentStepLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = stepColor(state.currentStepType),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big countdown
                    Text(
                        text = formatTime(state.remainingMs),
                        style = MaterialTheme.typography.displayLarge,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    if (state.totalStepMs > 0) {
                        LinearProgressIndicator(
                            progress = { 1f - (state.remainingMs.toFloat() / state.totalStepMs) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = onPauseResume) {
                            Text(if (state.isPaused) "Resume" else "Pause")
                        }
                        Button(onClick = onStop) {
                            Text("Stop")
                        }
                    }
                } else {
                    // Idle — show total time and start button
                    val totalMs = computeTotalMs(bundle)
                    Text(
                        text = formatDuration(totalMs),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onStart) {
                        Text("Start", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            HorizontalDivider()

            // ── Bottom half: Block step overview ──
            val activeBlockIndex = if (state.isRunning) state.currentBlockIndex else 0
            val currentBlock = bundle.blocks.getOrNull(activeBlockIndex)

            if (currentBlock != null) {
                StepOverview(
                    block = currentBlock,
                    blockIndex = activeBlockIndex,
                    totalBlocks = bundle.blocks.size,
                    activeStepIndex = if (state.isRunning) state.currentStepIndex else -1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepOverview(
    block: TimerBlock,
    blockIndex: Int,
    totalBlocks: Int,
    activeStepIndex: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Block header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Block ${blockIndex + 1}" + if (totalBlocks > 1) " / $totalBlocks" else "",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${block.repetitions} reps",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            itemsIndexed(block.steps) { index, step ->
                val isActive = index == activeStepIndex
                val isDone = index < activeStepIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isActive) Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) else Modifier
                        )
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                ) {
                    // Step indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> stepColor(step.type)
                                    isDone -> MaterialTheme.colorScheme.outlineVariant
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Type
                    Text(
                        text = step.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) stepColor(step.type)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(52.dp),
                    )

                    // Label
                    if (step.label.isNotEmpty()) {
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Duration
                    Text(
                        text = "${step.baseDurationMs.toInt() / 1000}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Delta hint
                    if (step.deltaMs != 0.toShort()) {
                        Text(
                            text = " (${step.deltaMs.toInt() / 1000}s/rep)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun stepColor(type: StepType) =
    if (type == StepType.WORK) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.tertiary

private fun computeTotalMs(bundle: TimerBundle): Long {
    var total = 0L
    for (block in bundle.blocks) {
        for (rep in 0 until block.repetitions.toInt()) {
            for (step in block.steps) {
                val base = step.baseDurationMs.toLong()
                val delta = step.deltaMs.toLong() * rep
                total += maxOf(base + delta, step.minMs.toLong())
            }
        }
    }
    return total
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms + 999) / 1000 // round up so "0" only shows when truly done
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return if (min > 0) "${min}m ${sec}s" else "${sec}s"
}
