package com.gtnoo.mnemosyne.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.DateRange
import com.gtnoo.mnemosyne.domain.service.ChartPoint
import com.gtnoo.mnemosyne.domain.service.ScatterPoint
import com.gtnoo.mnemosyne.presentation.people.BarChart
import com.gtnoo.mnemosyne.ui.components.SectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizationDashboardScreen(
    viewModel: VisualizationDashboardViewModel = hiltViewModel()
) {
    val calmTrend by viewModel.calmTrend.collectAsStateWithLifecycle()
    val happinessTrend by viewModel.happinessTrend.collectAsStateWithLifecycle()
    val energyTrend by viewModel.energyTrend.collectAsStateWithLifecycle()
    val sleepVsCalm by viewModel.sleepVsCalm.collectAsStateWithLifecycle()
    val outcomeSummary by viewModel.outcomeSummary.collectAsStateWithLifecycle()
    val moodByPlace by viewModel.moodByPlace.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()

    var selectedRangeIndex by remember { mutableIntStateOf(0) }
    val ranges = listOf("30 days" to DateRange.lastMonth(), "90 days" to DateRange.lastThreeMonths(), "1 year" to DateRange.lastYear())

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ranges.forEachIndexed { index, (label, _) ->
                        SegmentedButton(
                            selected = selectedRangeIndex == index,
                            onClick = {
                                selectedRangeIndex = index
                                viewModel.setRange(ranges[index].second)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, ranges.size)
                        ) { Text(label) }
                    }
                }
            }

            item { SectionLabel("Calm Over Time") }
            item {
                BarChart(data = calmTrend.map { it.date to it.value },
                    yLabel = "Calm (0-10)", color = MaterialTheme.colorScheme.primary)
            }

            item { SectionLabel("Happiness Over Time") }
            item {
                BarChart(data = happinessTrend.map { it.date to it.value },
                    yLabel = "Happiness (0-10)", color = MaterialTheme.colorScheme.secondary)
            }

            item { SectionLabel("Energy Over Time") }
            item {
                BarChart(data = energyTrend.map { it.date to it.value },
                    yLabel = "Energy (0-10)", color = MaterialTheme.colorScheme.tertiary)
            }

            item { SectionLabel("Sleep vs. Calm") }
            item { ScatterChart(data = sleepVsCalm, xLabel = "Hours Slept", yLabel = "Calm") }

            if (outcomeSummary.isNotEmpty()) {
                item { SectionLabel("Outcome Summary") }
                item { OutcomeSummaryCard(summary = outcomeSummary) }
            }

            if (moodByPlace.isNotEmpty()) {
                item { SectionLabel("Average Mood by Place") }
                item { MoodByPlaceCard(data = moodByPlace) }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ScatterChart(
    data: List<ScatterPoint>,
    xLabel: String,
    yLabel: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Card(modifier = modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(160.dp), Alignment.Center) {
                Text("No data available", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                val maxX = data.maxOf { it.x }.coerceAtLeast(1.0)
                val maxY = data.maxOf { it.y }.coerceAtLeast(1.0)
                data.forEach { point ->
                    val x = (point.x / maxX * size.width).toFloat()
                    val y = size.height - (point.y / maxY * size.height).toFloat()
                    drawCircle(color = primaryColor.copy(alpha = 0.7f), radius = 8f, center = Offset(x, y))
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("X: $xLabel", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Y: $yLabel", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OutcomeSummaryCard(summary: Map<String, Int>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            summary.entries.forEach { (label, count) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(count.toString(), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MoodByPlaceCard(data: List<Pair<String, Double>>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.forEach { (place, mood) ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(place, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { (mood / 10.0).toFloat() },
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(String.format("%.1f", mood), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
