package io.github.hitchclimber.universaltimers.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hitchclimber.universaltimers.R
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBlock
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.timer.TimerState

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
    val stepColor by animateColorAsState(
        targetValue = stepTypeColor(state.currentStepType),
        animationSpec = tween(300),
        label = "step-color",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bundle.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Top: Timer display + controls ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (state.isFinished) {
                    FinishedView(onReset = onStop)
                } else if (state.isRunning) {
                    RunningView(
                        state = state,
                        bundle = bundle,
                        stepColor = stepColor,
                        onPauseResume = onPauseResume,
                        onStop = onStop,
                    )
                } else {
                    IdleView(bundle = bundle, onStart = onStart)
                }
            }

            // ── Bottom: Step overview ──
            val activeBlockIndex = if (state.isRunning) state.currentBlockIndex else 0
            val currentBlock = bundle.blocks.getOrNull(activeBlockIndex)

            if (currentBlock != null) {
                StepOverview(
                    block = currentBlock,
                    blockIndex = activeBlockIndex,
                    totalBlocks = bundle.blocks.size,
                    activeStepIndex = if (state.isRunning) state.currentStepIndex else -1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f),
                )
            }
        }
    }
}

@Composable
private fun IdleView(bundle: TimerBundle, onStart: () -> Unit) {
    val totalMs = computeTotalMs(bundle)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatDuration(totalMs),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.height(32.dp))

        FilledIconButton(
            onClick = onStart,
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun RunningView(
    state: TimerState,
    bundle: TimerBundle,
    stepColor: Color,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    val totalReps = bundle.blocks.getOrNull(state.currentBlockIndex)?.repetitions ?: 0

    // Set indicator
    Text(
        text = "Set ${state.currentRepetition + 1} / $totalReps",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Step type chip
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(stepColor.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Text(
            text = state.currentStepLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = stepColor,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Circular progress + countdown
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        val progress by animateFloatAsState(
            targetValue = if (state.totalStepMs > 0)
                1f - (state.remainingMs.toFloat() / state.totalStepMs) else 0f,
            animationSpec = tween(150),
            label = "progress",
        )

        val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val arcOffset = Offset(strokeWidth / 2, strokeWidth / 2)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // Progress
            drawArc(
                color = stepColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(state.remainingMs),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Controls
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Stop
        FilledTonalIconButton(
            onClick = onStop,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Icon(
                painterResource(R.drawable.ic_stop),
                contentDescription = "Stop",
                modifier = Modifier.size(24.dp),
            )
        }

        // Pause/Resume
        FilledIconButton(
            onClick = onPauseResume,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = stepColor,
                contentColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Icon(
                painterResource(
                    if (state.isPaused) R.drawable.ic_play else R.drawable.ic_pause
                ),
                contentDescription = if (state.isPaused) "Resume" else "Pause",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun FinishedView(onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Done!",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledIconButton(
            onClick = onBack,
            modifier = Modifier.size(64.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Block ${blockIndex + 1}" + if (totalBlocks > 1) " / $totalBlocks" else "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${block.repetitions} sets",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(block.steps) { index, step ->
                val isActive = index == activeStepIndex
                val isDone = index < activeStepIndex
                val color = stepTypeColor(step.type)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isActive) Modifier.background(color.copy(alpha = 0.12f))
                            else Modifier
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                ) {
                    // Indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> color
                                    isDone -> MaterialTheme.colorScheme.outlineVariant
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = step.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) color else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(52.dp),
                    )

                    if (step.label.isNotEmpty()) {
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Text(
                        text = "${step.baseDurationMs / 1000}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (step.deltaMs != 0L) {
                        Text(
                            text = " (${step.deltaMs / 1000}s/rep)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun stepTypeColor(type: StepType) =
    if (type == StepType.WORK) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.tertiary

private fun computeTotalMs(bundle: TimerBundle): Long {
    var total = 0L
    for (block in bundle.blocks) {
        for (rep in 0 until block.repetitions) {
            for (step in block.steps) {
                total += maxOf(step.baseDurationMs + step.deltaMs * rep, step.minMs)
            }
        }
    }
    return total
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms + 999) / 1000
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
