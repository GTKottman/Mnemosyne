package com.gtnoo.mnemosyne.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.service.EntryService
import com.gtnoo.mnemosyne.presentation.settings.SettingsViewModel
import com.gtnoo.mnemosyne.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val entryService: EntryService
) : ViewModel() {
    private val _entry = MutableStateFlow<DailyEntry?>(null)
    val entry: StateFlow<DailyEntry?> = _entry.asStateFlow()

    fun loadEntry(id: String) {
        viewModelScope.launch { _entry.value = entryService.getEntry(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onSimilarity: (String) -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    LaunchedEffect(entryId) { viewModel.loadEntry(entryId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Entry Detail") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { onSimilarity(entryId) }) {
                        Icon(Icons.Default.Compare, contentDescription = "Find similar")
                    }
                    IconButton(onClick = { onEdit(entryId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        val e = entry
        if (e == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(e.entryDate.toString(), style = MaterialTheme.typography.headlineSmall)
                            if (e.tags.isNotEmpty()) {
                                Text(e.tags.joinToString(", ") { "#$it" },
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item { SectionLabel("Emotions") }
                item { EmotionGrid(e.emotionData) }
                if (e.placesVisited.isNotEmpty()) {
                    item { SectionLabel("Places Visited") }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            e.placesVisited.forEach { place -> PlaceChip(place = place) }
                        }
                    }
                }
                if (e.weatherSnapshots.isNotEmpty()) {
                    item { SectionLabel("Weather") }
                    items(e.weatherSnapshots) { WeatherCard(it, useFahrenheit = settings.useFahrenheit) }
                }
                item { SectionLabel("Health Context") }
                item { HealthSummaryCard(e.healthContextData, e.intakeData) }
                if (e.freeformNotes.isNotBlank()) {
                    item { SectionLabel("Notes") }
                    item {
                        Card {
                            Text(e.freeformNotes, modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun EmotionGrid(emotion: EmotionData) {
    val items = listOf(
        "Mood" to emotion.happiness, "Safety" to emotion.safety, "Calm" to emotion.calm,
        "Connection" to emotion.connection, "Clarity" to emotion.clarity, "Capacity" to emotion.capacity,
        "Hope" to emotion.hope, "Worthiness" to emotion.worthiness, "Peace" to emotion.peace,
        "Energy" to emotion.energy, "Agency" to emotion.agency, "Presence" to emotion.presence
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { (label, value) ->
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)) {
                            Text(value.toString(), style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryCard(health: HealthContextData, intake: IntakeData) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Sleep", "${intake.hoursSlept}h")
            StatItem("Energy", "${health.energyLevel}/10")
            StatItem("Social", "${health.socialBattery}/10")
            StatItem("Exec Fn", "${health.executiveFunction}/10")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
