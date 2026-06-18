package com.gtnoo.mnemosyne.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewEntry: () -> Unit,
    onNewInteraction: () -> Unit,
    onEntryClick: (String) -> Unit,
    onPersonClick: (String) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onImportExport: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayEntries by viewModel.todayEntries.collectAsStateWithLifecycle()
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()
    val favoriteSummaries by viewModel.favoriteSummaries.collectAsStateWithLifecycle()

    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(onClick = onNewInteraction) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Log Interaction")
                }
                ExtendedFloatingActionButton(
                    onClick = onNewEntry,
                    icon = { Icon(Icons.Default.Add, "New Entry") },
                    text = { Text("New Entry") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Mnemosyne", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Light)
                        Text(
                            LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { showOverflowMenu = false; onSettings() }
                            )
                            DropdownMenuItem(
                                text = { Text("Import / Export") },
                                leadingIcon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                                onClick = { showOverflowMenu = false; onImportExport() }
                            )
                        }
                    }
                }
            }

            // Today's status
            item {
                if (todayEntries.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("No entry yet today", style = MaterialTheme.typography.titleSmall)
                            Text("How are you doing?", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onNewEntry, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Log Entry")
                                }
                                OutlinedButton(onClick = onNewInteraction, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Log Interaction")
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Today", style = MaterialTheme.typography.titleSmall)
                            todayEntries.forEach { entry ->
                                EntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                            }
                        }
                    }
                }
            }

            // Favorite people
            if (favoriteSummaries.isNotEmpty()) {
                item { SectionLabel("People") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favoriteSummaries) { summary ->
                            FavoritePersonCard(summary = summary, onClick = { onPersonClick(summary.person.id) })
                        }
                    }
                }
            }

            // Recent entries
            if (recentEntries.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Recent")
                        TextButton(onClick = onHistory) { Text("See all") }
                    }
                }
                items(recentEntries, key = { it.id }) { entry ->
                    EntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FavoritePersonCard(
    summary: com.gtnoo.mnemosyne.domain.model.PersonSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(140.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null,
                modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Text(summary.person.displayName, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(summary.person.relationshipType.label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            summary.daysSinceLastContact?.let { days ->
                Text(if (days == 0) "Today" else "$days days ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (days > 7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
