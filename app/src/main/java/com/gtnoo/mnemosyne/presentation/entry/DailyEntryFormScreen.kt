package com.gtnoo.mnemosyne.presentation.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.presentation.settings.SettingsViewModel
import com.gtnoo.mnemosyne.ui.components.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEntryFormScreen(
    entryId: String?,
    date: String?,
    onBack: () -> Unit,
    onNewInteraction: (date: String) -> Unit,
    onInteractionClick: (interactionId: String) -> Unit,
    viewModel: DailyEntryFormViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val allPlaces by viewModel.allPlaces.collectAsStateWithLifecycle()
    val interactions by viewModel.interactionsForDate.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showPlacePicker by rememberSaveable { mutableStateOf(false) }
    var tagInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(entryId, date) {
        when {
            !entryId.isNullOrBlank() -> viewModel.loadEntry(entryId)
            !date.isNullOrBlank() -> viewModel.initForDate(LocalDate.parse(date))
            else -> viewModel.initForDate(LocalDate.now())
        }
    }

    LaunchedEffect(saved) { if (saved) onBack() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (entryId.isNullOrBlank()) "New Entry" else "Edit Entry") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.saveEntry() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Section 1: Date & Context
            item {
                CollapsibleSection("Date & Context", initiallyExpanded = true) {
                    ContextSection(
                        entryDate = entry.entryDate,
                        context = entry.context,
                        onDateUpdate = { viewModel.updateDate(it) },
                        onContextUpdate = { viewModel.updateContext(it) }
                    )
                }
            }

            // Section 2: Interactions (read-only, sourced from InteractionRepository by date)
            item {
                CollapsibleSection("Interactions", initiallyExpanded = true) {
                    if (interactions.isEmpty()) {
                        Text(
                            "No interactions logged for this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(interactions) { interaction ->
                                AssistChip(
                                    onClick = { onInteractionClick(interaction.id) },
                                    label = { Text(interaction.person.displayName) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onNewInteraction(entry.entryDate.toString()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Interaction for this Date")
                    }
                }
            }

            // Section 3: My Feelings
            item {
                CollapsibleSection("My Feelings", initiallyExpanded = true) {
                    EmotionSection(entry.emotionData) { viewModel.updateEmotionData(it) }
                }
            }

            // Section 4: My Thoughts
            item {
                CollapsibleSection("My Thoughts") {
                    ThoughtSection(entry.thoughtPatternData) { viewModel.updateThoughtData(it) }
                }
            }

            // Section 5: Body & Life Context
            item {
                CollapsibleSection("Body & Life Context") {
                    HealthSection(entry.healthContextData) { viewModel.updateHealthData(it) }
                }
            }

            // Section 7: Places Visited
            item {
                CollapsibleSection("Places Visited") {
                    if (entry.placesVisited.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(entry.placesVisited) { place ->
                                PlaceChip(place = place, onRemove = { viewModel.removePlace(place.id) })
                            }
                        }
                    }
                    OutlinedButton(onClick = { showPlacePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Place")
                    }
                }
            }

            // Section 9: Weather
            if (entry.weatherSnapshots.isNotEmpty()) {
                item {
                    CollapsibleSection("Weather", initiallyExpanded = true) {
                        entry.weatherSnapshots.forEach { snapshot ->
                            WeatherCard(snapshot = snapshot, useFahrenheit = settings.useFahrenheit)
                        }
                    }
                }
            }

            // Section 10: Notes & Tags
            item {
                CollapsibleSection("Notes & Tags", initiallyExpanded = true) {
                    OutlinedTextField(
                        value = entry.freeformNotes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                    TagInput(
                        tags = entry.tags,
                        input = tagInput,
                        onInputChange = { tagInput = it },
                        onAddTag = {
                            if (tagInput.isNotBlank() && !entry.tags.contains(tagInput.trim())) {
                                viewModel.updateTags(entry.tags + tagInput.trim())
                                tagInput = ""
                            }
                        },
                        onRemoveTag = { tag -> viewModel.updateTags(entry.tags - tag) }
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.saveEntry() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save Entry") }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showPlacePicker) {
        PlacePickerDialog(
            places = allPlaces.filter { pl -> entry.placesVisited.none { it.id == pl.id } },
            onSelect = { place -> viewModel.addPlace(place); showPlacePicker = false },
            onDismiss = { showPlacePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextSection(
    entryDate: LocalDate,
    context: EntryContext,
    onDateUpdate: (LocalDate) -> Unit,
    onContextUpdate: (EntryContext) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = entryDate.toEpochDay() * 86_400_000L
    )

    OutlinedTextField(
        value = entryDate.toString(),
        onValueChange = {},
        readOnly = true,
        label = { Text("Date") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateUpdate(LocalDate.ofEpochDay(millis / 86_400_000L))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    SwitchField("School Day", context.schoolDay) { onContextUpdate(context.copy(schoolDay = it)) }
    SwitchField("Work Day", context.workDay) { onContextUpdate(context.copy(workDay = it)) }
    SwitchField("Weekend", context.weekend) { onContextUpdate(context.copy(weekend = it)) }
    SwitchField("Holiday", context.holiday) { onContextUpdate(context.copy(holiday = it)) }
    SwitchField("Vacation", context.vacation) { onContextUpdate(context.copy(vacation = it)) }
}

@Composable
private fun EmotionSection(emotion: EmotionData, onUpdate: (EmotionData) -> Unit) {
    SliderField("", emotion.happiness, leftLabel = "Sadness", rightLabel = "Happiness") { onUpdate(emotion.copy(happiness = it)) }
    SliderField("", emotion.safety, leftLabel = "Threat", rightLabel = "Safety") { onUpdate(emotion.copy(safety = it)) }
    SliderField("", emotion.calm, leftLabel = "Agitation", rightLabel = "Calm") { onUpdate(emotion.copy(calm = it)) }
    SliderField("", emotion.connection, leftLabel = "Isolation", rightLabel = "Connection") { onUpdate(emotion.copy(connection = it)) }
    SliderField("", emotion.clarity, leftLabel = "Confusion", rightLabel = "Clarity") { onUpdate(emotion.copy(clarity = it)) }
    SliderField("", emotion.capacity, leftLabel = "Overwhelm", rightLabel = "Capacity") { onUpdate(emotion.copy(capacity = it)) }
    SliderField("", emotion.hope, leftLabel = "Hopelessness", rightLabel = "Hope") { onUpdate(emotion.copy(hope = it)) }
    SliderField("", emotion.worthiness, leftLabel = "Shame", rightLabel = "Worthiness") { onUpdate(emotion.copy(worthiness = it)) }
    SliderField("", emotion.peace, leftLabel = "Anger", rightLabel = "Peace") { onUpdate(emotion.copy(peace = it)) }
    SliderField("", emotion.energy, leftLabel = "Exhaustion", rightLabel = "Energy") { onUpdate(emotion.copy(energy = it)) }
    SliderField("", emotion.agency, leftLabel = "Helplessness", rightLabel = "Agency") { onUpdate(emotion.copy(agency = it)) }
    SliderField("", emotion.presence, leftLabel = "Numbness", rightLabel = "Presence") { onUpdate(emotion.copy(presence = it)) }
}

@Composable
private fun ThoughtSection(thought: ThoughtPatternData, onUpdate: (ThoughtPatternData) -> Unit) {
    SliderField("Rumination Level", thought.ruminationLevel) { onUpdate(thought.copy(ruminationLevel = it)) }
    SliderField("Need for Reassurance", thought.needForReassurance) { onUpdate(thought.copy(needForReassurance = it)) }
    SliderField("Clarity", thought.clarityLevel) { onUpdate(thought.copy(clarityLevel = it)) }
    SliderField("Uncertainty", thought.uncertaintyLevel) { onUpdate(thought.copy(uncertaintyLevel = it)) }
    SliderField("Wanted to Pull Away", thought.wantedToPullAway) { onUpdate(thought.copy(wantedToPullAway = it)) }
    SliderField("Wanted to Reach Out", thought.wantedToReachOut) { onUpdate(thought.copy(wantedToReachOut = it)) }
    SwitchField("Checked Phone Repeatedly", thought.checkedPhoneRepeatedly) { onUpdate(thought.copy(checkedPhoneRepeatedly = it)) }
    SwitchField("Reread Messages", thought.rereadMessages) { onUpdate(thought.copy(rereadMessages = it)) }
    SwitchField("Imagined Negative Outcome", thought.imaginedNegativeOutcome) { onUpdate(thought.copy(imaginedNegativeOutcome = it)) }
    SwitchField("Imagined Positive Outcome", thought.imaginedPositiveOutcome) { onUpdate(thought.copy(imaginedPositiveOutcome = it)) }
}

@Composable
private fun HealthSection(health: HealthContextData, onUpdate: (HealthContextData) -> Unit) {
    SliderField("Energy Level", health.energyLevel) { onUpdate(health.copy(energyLevel = it)) }
    SliderField("Social Battery", health.socialBattery) { onUpdate(health.copy(socialBattery = it)) }
    SliderField("Executive Function", health.executiveFunction) { onUpdate(health.copy(executiveFunction = it)) }
    SliderField("School Stress", health.schoolStress) { onUpdate(health.copy(schoolStress = it)) }
    SliderField("Work Stress", health.workStress) { onUpdate(health.copy(workStress = it)) }
    SliderField("Sensory Overload", health.sensoryOverload) { onUpdate(health.copy(sensoryOverload = it)) }
    SliderField("Body Discomfort", health.bodyDiscomfort) { onUpdate(health.copy(bodyDiscomfort = it)) }
    OutlinedTextField(
        value = if (health.hoursSlept == 0.0) "" else health.hoursSlept.toString(),
        onValueChange = { onUpdate(health.copy(hoursSlept = it.toDoubleOrNull() ?: 0.0)) },
        label = { Text("Hours Slept") },
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    OutlinedTextField(
        value = if (health.caffeineIntakeMg == 0) "" else health.caffeineIntakeMg.toString(),
        onValueChange = { onUpdate(health.copy(caffeineIntakeMg = it.toIntOrNull() ?: 0)) },
        label = { Text("Caffeine (mg)") },
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    SwitchField("Ate Enough", health.ateEnough) { onUpdate(health.copy(ateEnough = it)) }
}

@Composable
private fun TagInput(
    tags: List<String>, input: String,
    onInputChange: (String) -> Unit, onAddTag: () -> Unit, onRemoveTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input, onValueChange = onInputChange,
                label = { Text("Add tag") }, modifier = Modifier.weight(1f), singleLine = true
            )
            IconButton(onClick = onAddTag) { Icon(Icons.Default.Add, "Add tag") }
        }
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tags) { tag ->
                    InputChip(selected = false, onClick = {}, label = { Text(tag) },
                        trailingIcon = {
                            IconButton(onClick = { onRemoveTag(tag) }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacePickerDialog(places: List<SavedPlace>, onSelect: (SavedPlace) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Place") },
        text = {
            if (places.isEmpty()) {
                Text("No more places to add. Go to Places to add more.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    places.forEach { place ->
                        TextButton(onClick = { onSelect(place) }, modifier = Modifier.fillMaxWidth()) {
                            Text("${place.label} (${place.placeType.label})")
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
