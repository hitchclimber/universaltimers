package io.github.hitchclimber.universaltimers.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.hitchclimber.universaltimers.R
import io.github.hitchclimber.universaltimers.data.StepType
import io.github.hitchclimber.universaltimers.data.TimerBundle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bundles: List<TimerBundle>,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onBundleClick: (TimerBundle) -> Unit,
    onDeleteBundle: (TimerBundle) -> Unit,
    onAddClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Universal Timers") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                            ),
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        if (bundles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No timers yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(bundles, key = { it.id }) { bundle ->
                    SwipeToDeleteItem(
                        onDelete = { onDeleteBundle(bundle) },
                    ) {
                        BundleCard(
                            bundle = bundle,
                            onClick = { onBundleClick(bundle) },
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.error
                else Color.Transparent,
                label = "swipe-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError,
                )
            }
        },
    ) {
        content()
    }
}

@Composable
private fun BundleCard(
    bundle: TimerBundle,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = bundle.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val totalMs = computeBundleTotalMs(bundle)
                val totalSteps = bundle.blocks.sumOf { it.steps.size * it.repetitions }
                val totalSets = bundle.blocks.sumOf { it.repetitions }

                InfoChip(
                    label = formatDurationCompact(totalMs),
                    color = MaterialTheme.colorScheme.primary,
                )
                InfoChip(
                    label = "$totalSets sets",
                    color = MaterialTheme.colorScheme.tertiary,
                )

                // Show work/rest breakdown for first block
                bundle.blocks.firstOrNull()?.let { block ->
                    val work = block.steps.firstOrNull { it.type == StepType.WORK }
                    val rest = block.steps.firstOrNull { it.type == StepType.REST }
                    if (work != null) {
                        Text(
                            text = "${work.baseDurationMs / 1000}s" +
                                    if (rest != null) " / ${rest.baseDurationMs / 1000}s" else "",
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
private fun InfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

private fun computeBundleTotalMs(bundle: TimerBundle): Long {
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

private fun formatDurationCompact(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return when {
        min > 0 && sec > 0 -> "${min}m ${sec}s"
        min > 0 -> "${min}m"
        else -> "${sec}s"
    }
}
