package com.gtnoo.mnemosyne.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.DailyEntry
import com.gtnoo.mnemosyne.domain.model.FilterCriteria
import com.gtnoo.mnemosyne.ui.components.EntryCard
import com.gtnoo.mnemosyne.ui.components.SectionLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryHistoryScreen(
    onEntryClick: (String) -> Unit,
    onNewEntry: () -> Unit,
    viewModel: EntryHistoryViewModel = hiltViewModel()
) {
    val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                            contentDescription = "Filters"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEntry) {
                Icon(Icons.Default.Add, contentDescription = "New Entry")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showFilters) {
                FilterPanel(filter = filter, onFilterChange = { viewModel.updateFilter(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                HorizontalDivider()
            }

            if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventNote, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No entries found", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add your first entry", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val grouped = filteredEntries.groupBy { it.entryDate }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.entries.sortedByDescending { it.key }.forEach { (date, entries) ->
                        item {
                            SectionLabel(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)))
                        }
                        items(entries, key = { it.id }) { entry ->
                            EntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    filter: FilterCriteria,
    onFilterChange: (FilterCriteria) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Filters", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filter.onlyDaysWithInteractions,
                onClick = { onFilterChange(filter.copy(onlyDaysWithInteractions = !filter.onlyDaysWithInteractions)) },
                label = { Text("Has interactions") }
            )
            FilterChip(
                selected = filter.onlyHighRuminationDays,
                onClick = { onFilterChange(filter.copy(onlyHighRuminationDays = !filter.onlyHighRuminationDays)) },
                label = { Text("High rumination") }
            )
            FilterChip(
                selected = filter.onlyWeatherAffectedDays,
                onClick = { onFilterChange(filter.copy(onlyWeatherAffectedDays = !filter.onlyWeatherAffectedDays)) },
                label = { Text("Has weather") }
            )
        }
        if (filter != FilterCriteria()) {
            TextButton(onClick = { onFilterChange(FilterCriteria()) }) { Text("Clear filters") }
        }
    }
}
