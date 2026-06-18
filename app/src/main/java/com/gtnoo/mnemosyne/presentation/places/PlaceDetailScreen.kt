package com.gtnoo.mnemosyne.presentation.places

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.EntryService
import com.gtnoo.mnemosyne.domain.service.PlaceService
import com.gtnoo.mnemosyne.presentation.people.BarChart
import com.gtnoo.mnemosyne.ui.components.EntryCard
import com.gtnoo.mnemosyne.ui.components.SectionLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val placeService: PlaceService,
    private val entryService: EntryService
) : ViewModel() {
    private val _place = MutableStateFlow<SavedPlace?>(null)
    val place: StateFlow<SavedPlace?> = _place.asStateFlow()

    private val _summary = MutableStateFlow<PlaceSummary?>(null)
    val summary: StateFlow<PlaceSummary?> = _summary.asStateFlow()

    private val _recentEntries = MutableStateFlow<List<DailyEntry>>(emptyList())
    val recentEntries: StateFlow<List<DailyEntry>> = _recentEntries.asStateFlow()

    fun load(placeId: String) {
        viewModelScope.launch {
            val p = placeRepository.getById(placeId) ?: return@launch
            _place.value = p
            _summary.value = placeService.getSummaryForPlace(p, DateRange.lastThreeMonths())
            _recentEntries.value = entryService.getEntriesForPlace(placeId).take(20)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onEntryClick: (String) -> Unit,
    viewModel: PlaceDetailViewModel = hiltViewModel()
) {
    val place by viewModel.place.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()

    LaunchedEffect(placeId) { viewModel.load(placeId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(place?.label ?: "Place") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { onEdit(placeId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        val p = place
        if (p == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null,
                                    modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(p.label, style = MaterialTheme.typography.headlineSmall)
                                    Text(p.placeType.label, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            val location = listOfNotNull(p.city.takeIf { it.isNotBlank() }, p.state.takeIf { it.isNotBlank() }).joinToString(", ")
                            if (location.isNotBlank()) Text(location, style = MaterialTheme.typography.bodySmall)
                            if (p.notes.isNotBlank()) Text(p.notes, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                summary?.let { s ->
                    item { SectionLabel("Summary") }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PlaceStatBlock("Visits", s.totalVisits.toString())
                                PlaceStatBlock("Avg Mood", String.format("%.1f", s.averageMood))
                                PlaceStatBlock("Avg Energy", String.format("%.1f", s.averageEnergy))
                            }
                        }
                    }
                    if (s.moodTrend.isNotEmpty()) {
                        item { SectionLabel("Mood Trend at This Place") }
                        item {
                            BarChart(
                                data = s.moodTrend,
                                yLabel = "Happiness (0-10)",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                if (recentEntries.isNotEmpty()) {
                    item { SectionLabel("Recent Visits") }
                    items(recentEntries, key = { it.id }) { entry ->
                        EntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun PlaceStatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
