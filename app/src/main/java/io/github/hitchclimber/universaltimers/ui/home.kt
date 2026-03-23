package io.github.hitchclimber.universaltimers.ui

import io.github.hitchclimber.universaltimers.data.TimerBundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.hitchclimber.universaltimers.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bundles: List<TimerBundle>,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onBundleClick: (TimerBundle) -> Unit,
    onAddClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(bundles) { bundle ->
                Card(
                    onClick = { onBundleClick(bundle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = bundle.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
