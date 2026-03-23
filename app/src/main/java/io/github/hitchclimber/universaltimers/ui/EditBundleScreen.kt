package io.github.hitchclimber.universaltimers.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBlock
import io.github.hitchclimber.universaltimers.data.TimerBundle
import io.github.hitchclimber.universaltimers.data.TimerStep
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBundleScreen(
    initial: TimerBundle? = null,
    onSave: (TimerBundle) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var blocks by remember {
        mutableStateOf(
            initial?.blocks ?: listOf(
                TimerBlock(
                    steps = listOf(
                        TimerStep(type = StepType.WORK, baseDurationMs = 30_000u),
                    ),
                    repetitions = 1u,
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "New Bundle" else "Edit Bundle") },
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
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Bundle name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Bundle name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Blocks
            blocks.forEachIndexed { blockIndex, block ->
                EditBlockCard(
                    blockIndex = blockIndex,
                    block = block,
                    canDelete = blocks.size > 1,
                    onBlockChanged = { newBlock ->
                        blocks = blocks.toMutableList().also { it[blockIndex] = newBlock }
                    },
                    onDelete = {
                        blocks = blocks.toMutableList().also { it.removeAt(blockIndex) }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Add block
            OutlinedButton(
                onClick = {
                    blocks = blocks + TimerBlock(
                        steps = listOf(
                            TimerStep(type = StepType.WORK, baseDurationMs = 30_000u),
                        ),
                        repetitions = 1u,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Block")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(
                        TimerBundle(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            name = name.ifBlank { "Untitled" },
                            blocks = blocks,
                        )
                    )
                },
                enabled = blocks.isNotEmpty() && blocks.all { it.steps.isNotEmpty() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EditBlockCard(
    blockIndex: Int,
    block: TimerBlock,
    canDelete: Boolean,
    onBlockChanged: (TimerBlock) -> Unit,
    onDelete: () -> Unit,
) {
    // Determine if there's a REST step
    val workStep = block.steps.firstOrNull { it.type == StepType.WORK }
        ?: block.steps.first()
    val restStep = block.steps.firstOrNull { it.type == StepType.REST }
    val hasRest = restStep != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Block ${blockIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete block")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reps slider (discrete tick marks for countable sets)
            LabeledSlider(
                label = "Sets",
                value = block.repetitions.toInt(),
                range = 1f..30f,
                discrete = true,
                onValueChange = { onBlockChanged(block.copy(repetitions = it.toUShort())) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // WORK duration slider
            LabeledSlider(
                label = "Work",
                value = workStep.baseDurationMs.toInt() / 1000,
                range = 1f..300f,
                suffix = "s",
                onValueChange = { sec ->
                    val newWork = workStep.copy(baseDurationMs = (sec * 1000).toUShort())
                    onBlockChanged(block.copy(steps = rebuildSteps(newWork, if (hasRest) restStep else null)))
                },
            )

            // Work delta
            DeltaWheel(
                label = "Work delta",
                valueSec = workStep.deltaMs.toInt() / 1000,
                onValueChange = { sec ->
                    val newWork = workStep.copy(deltaMs = (sec * 1000).toShort())
                    onBlockChanged(block.copy(steps = rebuildSteps(newWork, if (hasRest) restStep else null)))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // REST toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Rest", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = hasRest,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            val newRest = TimerStep(type = StepType.REST, baseDurationMs = 15_000u)
                            val newReps = maxOf(2, block.repetitions.toInt()).toUShort()
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest), repetitions = newReps))
                        } else {
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, null)))
                        }
                    },
                )
            }

            if (hasRest && restStep != null) {
                Spacer(modifier = Modifier.height(4.dp))

                LabeledSlider(
                    label = "Rest",
                    value = restStep.baseDurationMs.toInt() / 1000,
                    range = 1f..300f,
                    suffix = "s",
                    onValueChange = { sec ->
                        val newRest = restStep.copy(baseDurationMs = (sec * 1000).toUShort())
                        onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                    },
                )

                DeltaWheel(
                    label = "Rest delta",
                    valueSec = restStep.deltaMs.toInt() / 1000,
                    onValueChange = { sec ->
                        val newRest = restStep.copy(deltaMs = (sec * 1000).toShort())
                        onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                    },
                )

                // Min rest (only show if delta is negative)
                if (restStep.deltaMs < 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledSlider(
                        label = "Min rest",
                        value = restStep.minMs.toInt() / 1000,
                        range = 0f..((restStep.baseDurationMs.toInt() / 1000).toFloat()),
                        suffix = "s",
                        onValueChange = { sec ->
                            val newRest = restStep.copy(minMs = (sec * 1000).toUShort())
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                        },
                    )
                }
            }
        }
    }
}

/**
 * Slider with a label on the left and current value on the right.
 * @param discrete If true, shows tick marks (good for small ranges like sets 1-30).
 */
@Composable
private fun LabeledSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    discrete: Boolean = false,
    onValueChange: (Int) -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    var localPos by remember { mutableFloatStateOf(value.toFloat()) }
    // Sync from parent when not dragging
    if (!dragging) {
        localPos = value.toFloat()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(72.dp),
        )
        Slider(
            value = localPos,
            onValueChange = {
                dragging = true
                localPos = it
            },
            onValueChangeFinished = {
                dragging = false
                onValueChange(localPos.roundToInt())
            },
            valueRange = range,
            steps = if (discrete) (range.endInclusive - range.start).toInt() - 1 else 0,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${localPos.roundToInt()}$suffix",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(52.dp),
        )
    }
}

/**
 * Vertical wheel-style control for delta values.
 * Up arrow = decrease (more negative), Down arrow = increase (more positive).
 */
@Composable
private fun DeltaWheel(
    label: String,
    valueSec: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        // Up = more negative (less time per rep)
        IconButton(
            onClick = { onValueChange(valueSec - 1) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Decrease delta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = if (valueSec == 0) "0s" else "${if (valueSec > 0) "+" else ""}${valueSec}s",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp),
        )

        // Down = more positive (more time per rep)
        IconButton(
            onClick = { onValueChange(valueSec + 1) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Increase delta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Rebuild step list as [WORK, REST?] */
private fun rebuildSteps(work: TimerStep, rest: TimerStep?): List<TimerStep> =
    if (rest != null) listOf(work, rest) else listOf(work)
