package com.gtnoo.mnemosyne.presentation.people

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.ImportantDateService
import com.gtnoo.mnemosyne.domain.service.PersonService
import com.gtnoo.mnemosyne.notification.ImportantDateScheduler
import com.gtnoo.mnemosyne.ui.components.SectionLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val placeRepository: PlaceRepository,
    private val importantDateService: ImportantDateService,
    @ApplicationContext private val appContext: android.content.Context
) : ViewModel() {
    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    private val _linkedPlace = MutableStateFlow<SavedPlace?>(null)
    val linkedPlace: StateFlow<SavedPlace?> = _linkedPlace.asStateFlow()

    private val _summary = MutableStateFlow<PersonSummary?>(null)
    val summary: StateFlow<PersonSummary?> = _summary.asStateFlow()

    private val _importantDates = MutableStateFlow<List<ImportantDate>>(emptyList())
    val importantDates: StateFlow<List<ImportantDate>> = _importantDates.asStateFlow()

    private var observeJob: kotlinx.coroutines.Job? = null

    fun load(personId: String) {
        viewModelScope.launch {
            val p = personRepository.getById(personId) ?: return@launch
            _person.value = p
            _linkedPlace.value = p.usualPlaceId?.let { placeRepository.getById(it) }
            val range = DateRange.lastThreeMonths()
            _summary.value = personService.getSummaryForPerson(p, range)
        }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            importantDateService.observeByPersonId(personId).collect { dates ->
                _importantDates.value = dates
            }
        }
    }

    fun toggleFavorite() {
        val p = _person.value ?: return
        viewModelScope.launch {
            val updated = p.copy(isFavorite = !p.isFavorite)
            personService.updatePerson(updated)
            _person.value = updated
        }
    }

    fun saveImportantDate(date: ImportantDate) {
        viewModelScope.launch {
            importantDateService.saveImportantDate(date)
            ImportantDateScheduler.schedule(appContext)
        }
    }

    fun deleteImportantDate(id: String) {
        viewModelScope.launch {
            importantDateService.deleteImportantDate(id)
            ImportantDateScheduler.schedule(appContext)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onAnalytics: (String) -> Unit,
    onEntryClick: (String) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val person by viewModel.person.collectAsStateWithLifecycle()
    val linkedPlace by viewModel.linkedPlace.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val importantDates by viewModel.importantDates.collectAsStateWithLifecycle()

    var editingDate by remember { mutableStateOf<ImportantDate?>(null) }
    var showAddDate by remember { mutableStateOf(false) }

    LaunchedEffect(personId) { viewModel.load(personId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(person?.displayName ?: "Person") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    person?.let { p ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (p.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (p.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onAnalytics(personId) }) {
                        Icon(Icons.Default.BarChart, "Analytics")
                    }
                    IconButton(onClick = { onEdit(personId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        val p = person
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
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null,
                                    modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(p.displayName, style = MaterialTheme.typography.headlineSmall)
                                    Text(p.relationshipType.label, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            if (p.notes.isNotBlank()) {
                                Text(p.notes, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            linkedPlace?.let { place ->
                                if (place.latitude != null && place.longitude != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PinDrop,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = when {
                                                place.city.isNotBlank() && place.state.isNotBlank() ->
                                                    "${place.city}, ${place.state}"
                                                place.city.isNotBlank() -> place.city
                                                else -> "Location pinned"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            Text("Added ${p.addedOn.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel("Important Dates")
                        TextButton(onClick = { showAddDate = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
                items(importantDates, key = { it.id }) { date ->
                    ImportantDateCard(
                        date = date,
                        onClick = { editingDate = date }
                    )
                }

                summary?.let { s ->
                    item { SectionLabel("Summary") }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBlock("Interactions", s.totalInteractions.toString())
                                StatBlock("Avg Connection", String.format("%.1f", s.averageConnectionScore))
                                StatBlock("Days Since", s.daysSinceLastContact?.toString() ?: "N/A")
                            }
                        }
                    }
                    if (s.connectionTrend.isNotEmpty()) {
                        item { SectionLabel("Connection Trend") }
                        item {
                            Card {
                                ConnectionSparkLine(trend = s.connectionTrend, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingDate != null) {
        ImportantDateDialog(
            existing = editingDate,
            personId = personId,
            onDismiss = { editingDate = null },
            onSave = { saved ->
                viewModel.saveImportantDate(saved)
                editingDate = null
            },
            onDelete = if (editingDate?.kind != ImportantDateKind.BIRTHDAY) {
                { id ->
                    viewModel.deleteImportantDate(id)
                    editingDate = null
                }
            } else null
        )
    }

    if (showAddDate) {
        ImportantDateDialog(
            existing = null,
            personId = personId,
            onDismiss = { showAddDate = false },
            onSave = { saved ->
                viewModel.saveImportantDate(saved)
                showAddDate = false
            }
        )
    }
}

@Composable
private fun ImportantDateCard(date: ImportantDate, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(date.label, style = MaterialTheme.typography.titleSmall)
                Text(
                    date.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Not set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (date.isRecurring && date.date != null) {
                    Text("Repeats yearly", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (date.notifyEnabled) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = "Reminder enabled",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConnectionSparkLine(trend: List<Pair<java.time.LocalDate, Double>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (trend.isEmpty()) {
            Text("No data yet", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val max = trend.maxOf { it.second }.coerceAtLeast(10.0)
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trend.takeLast(14).forEach { (_, value) ->
                    val heightFraction = (value / max).toFloat()
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 2.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(heightFraction.coerceAtLeast(0.05f)),
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {}
                    }
                }
            }
            Text("Last ${trend.takeLast(14).size} entries",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}
