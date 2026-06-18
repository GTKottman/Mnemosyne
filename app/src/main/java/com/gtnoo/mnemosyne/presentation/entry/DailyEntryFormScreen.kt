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
    viewModel: DailyEntryFormViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val allPlaces by viewModel.allPlaces.collectAsStateWithLifecycle()
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

            // Section 5: Body & State
            item {
                CollapsibleSection("Body & State") {
                    HealthSection(entry.healthContextData, settings) { viewModel.updateHealthData(it) }
                }
            }

            // Section 6: Inputs & Intake
            item {
                CollapsibleSection("Inputs & Intake") {
                    IntakeSection(entry.intakeData) { viewModel.updateIntakeData(it) }
                }
            }

            // Section 7: Environment & Day Shape
            item {
                CollapsibleSection("Environment & Day Shape") {
                    EnvironmentSection(entry.environmentData) { viewModel.updateEnvironmentData(it) }
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
    SliderField("Intrusive Thoughts", thought.intrusiveThoughts) { onUpdate(thought.copy(intrusiveThoughts = it)) }
    SliderField("Worry / Anticipation", thought.worryAnticipation) { onUpdate(thought.copy(worryAnticipation = it)) }
    SliderField("Catastrophizing", thought.catastrophizing) { onUpdate(thought.copy(catastrophizing = it)) }
    SliderField("Reassurance Urge", thought.reassuranceUrge) { onUpdate(thought.copy(reassuranceUrge = it)) }
    SliderField("Mental Clarity", thought.mentalClarity) { onUpdate(thought.copy(mentalClarity = it)) }
    SliderField("Uncertainty Tolerance", thought.uncertaintyTolerance) { onUpdate(thought.copy(uncertaintyTolerance = it)) }
    SliderField("Decision Friction", thought.decisionFriction) { onUpdate(thought.copy(decisionFriction = it)) }
    SliderField("Cognitive Flexibility", thought.cognitiveFlexibility) { onUpdate(thought.copy(cognitiveFlexibility = it)) }
    SliderField("", thought.selfTalkTone, leftLabel = "Self-Critical", rightLabel = "Kind & Supportive") { onUpdate(thought.copy(selfTalkTone = it)) }
}

@Composable
private fun HealthSection(health: HealthContextData, settings: AppSettings, onUpdate: (HealthContextData) -> Unit) {
    SliderField("", health.energyLevel, leftLabel = "Low Energy", rightLabel = "High Energy") { onUpdate(health.copy(energyLevel = it)) }
    SliderField("", health.socialBattery, leftLabel = "Drained", rightLabel = "Socially Available") { onUpdate(health.copy(socialBattery = it)) }
    SliderField("", health.executiveFunction, leftLabel = "Can't Initiate", rightLabel = "Can Act & Organize") { onUpdate(health.copy(executiveFunction = it)) }
    SliderField("", health.mentalBandwidth, leftLabel = "Overloaded", rightLabel = "Spacious") { onUpdate(health.copy(mentalBandwidth = it)) }
    SliderField("", health.bodyDiscomfort, leftLabel = "Comfortable", rightLabel = "Discomfort") { onUpdate(health.copy(bodyDiscomfort = it)) }
    SliderField("", health.sensoryEnvironmentalStrain, leftLabel = "Easy Environment", rightLabel = "Taxing Environment") { onUpdate(health.copy(sensoryEnvironmentalStrain = it)) }
    SliderField("", health.sleepQuality, leftLabel = "Poor / Restless", rightLabel = "Good / Restorative") { onUpdate(health.copy(sleepQuality = it)) }
    SliderField("", health.stressLoad, leftLabel = "Calm Day", rightLabel = "High-Pressure Day") { onUpdate(health.copy(stressLoad = it)) }

    if (settings.showWorkSchoolPressure || settings.showMoneyPressure || settings.showRelationshipPressure ||
        settings.showFamilyPressure || settings.showHealthPressure || settings.showTimePressure) {
        Spacer(Modifier.height(4.dp))
        Text("Stress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (settings.showWorkSchoolPressure) {
            SliderField("Work / School Pressure", health.workSchoolPressure) { onUpdate(health.copy(workSchoolPressure = it)) }
        }
        if (settings.showMoneyPressure) {
            SliderField("Money Pressure", health.moneyPressure) { onUpdate(health.copy(moneyPressure = it)) }
        }
        if (settings.showRelationshipPressure) {
            SliderField("Relationship / Social Pressure", health.relationshipPressure) { onUpdate(health.copy(relationshipPressure = it)) }
        }
        if (settings.showFamilyPressure) {
            SliderField("Family Pressure", health.familyPressure) { onUpdate(health.copy(familyPressure = it)) }
        }
        if (settings.showHealthPressure) {
            SliderField("Health Pressure", health.healthPressure) { onUpdate(health.copy(healthPressure = it)) }
        }
        if (settings.showTimePressure) {
            SliderField("Time Pressure", health.timePressure) { onUpdate(health.copy(timePressure = it)) }
        }
    }
}

@Composable
private fun IntakeSection(intake: IntakeData, onUpdate: (IntakeData) -> Unit) {
    OutlinedTextField(
        value = if (intake.hoursSlept == 0.0) "" else intake.hoursSlept.toString(),
        onValueChange = { onUpdate(intake.copy(hoursSlept = it.toDoubleOrNull() ?: 0.0)) },
        label = { Text("Hours Slept") },
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    OutlinedTextField(
        value = if (intake.caffeineIntakeMg == 0) "" else intake.caffeineIntakeMg.toString(),
        onValueChange = { onUpdate(intake.copy(caffeineIntakeMg = it.toIntOrNull() ?: 0)) },
        label = { Text("Caffeine (mg)") },
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
    SliderField("", intake.foodAdequacy, leftLabel = "Didn't Eat", rightLabel = "Well Nourished") { onUpdate(intake.copy(foodAdequacy = it)) }
    SliderField("", intake.hydration, leftLabel = "Dehydrated", rightLabel = "Well Hydrated") { onUpdate(intake.copy(hydration = it)) }
    SliderField("", intake.movementLevel, leftLabel = "No Movement", rightLabel = "Heavy Exercise") { onUpdate(intake.copy(movementLevel = it)) }
}

@Composable
private fun EnvironmentSection(environment: EnvironmentData, onUpdate: (EnvironmentData) -> Unit) {
    SliderField("", environment.timeOutsideSunlight, leftLabel = "Indoor All Day", rightLabel = "Outside a Lot") { onUpdate(environment.copy(timeOutsideSunlight = it)) }
    SliderField("", environment.screenLoad, leftLabel = "Low Screen Time", rightLabel = "High Screen Time") { onUpdate(environment.copy(screenLoad = it)) }
    SliderField("", environment.socialExposure, leftLabel = "No Exposure", rightLabel = "Lots of Exposure") { onUpdate(environment.copy(socialExposure = it)) }
    SliderField("", environment.noveltyDisruption, leftLabel = "Routine Day", rightLabel = "Disrupted / Unusual") { onUpdate(environment.copy(noveltyDisruption = it)) }
    SliderField("", environment.physicalSpaceQuality, leftLabel = "Messy / Chaotic", rightLabel = "Clean / Supportive") { onUpdate(environment.copy(physicalSpaceQuality = it)) }
    SliderField("", environment.weatherImpact, leftLabel = "No Effect", rightLabel = "Strongly Affected") { onUpdate(environment.copy(weatherImpact = it)) }
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
