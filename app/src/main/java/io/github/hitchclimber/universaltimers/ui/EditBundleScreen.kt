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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var countdownEnabled by remember { mutableStateOf(initial?.countdownEnabled ?: false) }
    var blocks by remember {
        mutableStateOf(
            initial?.blocks ?: listOf(
                TimerBlock(
                    steps = listOf(TimerStep(type = StepType.WORK, baseDurationMs = 30_000)),
                    repetitions = 1,
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initial == null) "New Timer" else "Edit Timer")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Timer name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Countdown toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "3-second countdown",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Count down 3-2-1 before starting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = countdownEnabled,
                    onCheckedChange = { countdownEnabled = it },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        steps = listOf(TimerStep(type = StepType.WORK, baseDurationMs = 30_000)),
                        repetitions = 1,
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Block")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save
            Button(
                onClick = {
                    onSave(
                        TimerBundle(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            name = name.ifBlank { "Untitled" },
                            blocks = blocks,
                            countdownEnabled = countdownEnabled,
                        )
                    )
                },
                enabled = blocks.isNotEmpty() && blocks.all { it.steps.isNotEmpty() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text("Save", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
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
    val workStep = block.steps.firstOrNull { it.type == StepType.WORK } ?: block.steps.first()
    val restStep = block.steps.firstOrNull { it.type == StepType.REST }
    val hasRest = restStep != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Block ${blockIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete block",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sets
            LabeledSlider(
                label = "Sets",
                value = block.repetitions,
                range = 1f..30f,
                discrete = true,
                accentColor = MaterialTheme.colorScheme.secondary,
                onValueChange = { onBlockChanged(block.copy(repetitions = it)) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Work section
            SectionLabel("WORK", MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            LabeledSlider(
                label = "Duration",
                value = (workStep.baseDurationMs / 1000).toInt(),
                range = 1f..300f,
                suffix = "s",
                accentColor = MaterialTheme.colorScheme.primary,
                onValueChange = { sec ->
                    val newWork = workStep.copy(baseDurationMs = sec.toLong() * 1000)
                    onBlockChanged(block.copy(steps = rebuildSteps(newWork, if (hasRest) restStep else null)))
                },
            )

            DeltaWheel(
                label = "Delta",
                valueSec = (workStep.deltaMs / 1000).toInt(),
                onValueChange = { sec ->
                    val newWork = workStep.copy(deltaMs = sec.toLong() * 1000)
                    onBlockChanged(block.copy(steps = rebuildSteps(newWork, if (hasRest) restStep else null)))
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Rest toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SectionLabel("REST", MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = hasRest,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            val newRest = TimerStep(type = StepType.REST, baseDurationMs = 15_000)
                            val newReps = maxOf(2, block.repetitions)
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest), repetitions = newReps))
                        } else {
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, null)))
                        }
                    },
                )
            }

            if (hasRest) {
                Spacer(modifier = Modifier.height(8.dp))

                LabeledSlider(
                    label = "Duration",
                    value = (restStep.baseDurationMs / 1000).toInt(),
                    range = 1f..300f,
                    suffix = "s",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    onValueChange = { sec ->
                        val newRest = restStep.copy(baseDurationMs = sec.toLong() * 1000)
                        onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                    },
                )

                DeltaWheel(
                    label = "Delta",
                    valueSec = (restStep.deltaMs / 1000).toInt(),
                    onValueChange = { sec ->
                        val newRest = restStep.copy(deltaMs = sec.toLong() * 1000)
                        onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                    },
                )

                if (restStep.deltaMs < 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledSlider(
                        label = "Min",
                        value = (restStep.minMs / 1000).toInt(),
                        range = 0f..((restStep.baseDurationMs / 1000).toFloat()),
                        suffix = "s",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onValueChange = { sec ->
                            val newRest = restStep.copy(minMs = sec.toLong() * 1000)
                            onBlockChanged(block.copy(steps = rebuildSteps(workStep, newRest)))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    discrete: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onValueChange: (Int) -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    var localPos by remember { mutableFloatStateOf(value.toFloat()) }
    if (!dragging) {
        localPos = value.toFloat()
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${localPos.roundToInt()}$suffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )
        }
        val min = range.start.toInt()
        val max = range.endInclusive.toInt()

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                enabled = value > min,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Decrease",
                    tint = accentColor.copy(alpha = if (value > min) 1f else 0.3f),
                    modifier = Modifier.size(20.dp),
                )
            }

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
                steps = if (discrete) (max - min) - 1 else 0,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    activeTickColor = accentColor,
                ),
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                enabled = value < max,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Increase",
                    tint = accentColor.copy(alpha = if (value < max) 1f else 0.3f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

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
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = { onValueChange(valueSec - 1) },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Decrease",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (valueSec == 0) "0s" else "${if (valueSec > 0) "+" else ""}${valueSec}s",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        IconButton(
            onClick = { onValueChange(valueSec + 1) },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun rebuildSteps(work: TimerStep, rest: TimerStep?): List<TimerStep> =
    if (rest != null) listOf(work, rest) else listOf(work)
