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
import com.gtnoo.mnemosyne.ui.components.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyEntryFormScreen(
    entryId: String?,
    date: String?,
    onBack: () -> Unit,
    viewModel: DailyEntryFormViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()
    val allPlaces by viewModel.allPlaces.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showPersonPicker by rememberSaveable { mutableStateOf(false) }
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

            // Section 2: People Involved
            item {
                CollapsibleSection("People Involved", initiallyExpanded = true) {
                    if (entry.interactions.isEmpty()) {
                        Text("No people added yet.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(entry.interactions) { interaction ->
                                PersonChip(
                                    person = interaction.person,
                                    onRemove = { viewModel.removePersonInteraction(interaction.person.id) }
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { showPersonPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Person")
                    }
                }
            }

            // Section 3: Per-person interaction blocks
            items(entry.interactions) { interaction ->
                CollapsibleSection("Interaction with ${interaction.person.displayName}") {
                    InteractionBlock(
                        interaction = interaction,
                        onUpdate = { viewModel.updateInteraction(it) }
                    )
                }
            }

            // Section 4: My Feelings
            item {
                CollapsibleSection("My Feelings", initiallyExpanded = true) {
                    EmotionSection(entry.emotionData) { viewModel.updateEmotionData(it) }
                }
            }

            // Section 5: My Thoughts
            item {
                CollapsibleSection("My Thoughts") {
                    ThoughtSection(entry.thoughtPatternData) { viewModel.updateThoughtData(it) }
                }
            }

            // Section 6: Body & Life Context
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

            // Section 8: Weather
            if (entry.weatherSnapshots.isNotEmpty()) {
                item {
                    CollapsibleSection("Weather", initiallyExpanded = true) {
                        entry.weatherSnapshots.forEach { snapshot ->
                            WeatherCard(snapshot = snapshot)
                        }
                    }
                }
            }

            // Section 9: Notes & Tags
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

    if (showPersonPicker) {
        PersonPickerDialog(
            people = allPeople.filter { p -> entry.interactions.none { it.person.id == p.id } },
            onSelect = { person -> viewModel.addPersonInteraction(person); showPersonPicker = false },
            onDismiss = { showPersonPicker = false }
        )
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
    SliderField("Hopeful", emotion.hopeful) { onUpdate(emotion.copy(hopeful = it)) }
    SliderField("Anxious", emotion.anxious) { onUpdate(emotion.copy(anxious = it)) }
    SliderField("Sad", emotion.sad) { onUpdate(emotion.copy(sad = it)) }
    SliderField("Happy", emotion.happy) { onUpdate(emotion.copy(happy = it)) }
    SliderField("Calm", emotion.calm) { onUpdate(emotion.copy(calm = it)) }
    SliderField("Lonely", emotion.lonely) { onUpdate(emotion.copy(lonely = it)) }
    SliderField("Excited", emotion.excited) { onUpdate(emotion.copy(excited = it)) }
    SliderField("Confused", emotion.confused) { onUpdate(emotion.copy(confused = it)) }
    SliderField("Secure", emotion.secure) { onUpdate(emotion.copy(secure = it)) }
    SliderField("Connected", emotion.connected) { onUpdate(emotion.copy(connected = it)) }
    SliderField("Overwhelmed", emotion.overwhelmed) { onUpdate(emotion.copy(overwhelmed = it)) }
    SliderField("Regulated", emotion.regulated) { onUpdate(emotion.copy(regulated = it)) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InteractionBlock(interaction: PersonInteraction, onUpdate: (PersonInteraction) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Communication", "In Person", "Interpretation", "Outcome")

    PrimaryScrollableTabRow(selectedTabIndex = selectedTab) {
        tabs.forEachIndexed { idx, title ->
            Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(title) })
        }
    }

    Spacer(Modifier.height(8.dp))

    when (selectedTab) {
        0 -> CommunicationBlock(interaction.communicationData) { onUpdate(interaction.copy(communicationData = it)) }
        1 -> InPersonBlock(interaction.inPersonData) { onUpdate(interaction.copy(inPersonData = it)) }
        2 -> RelationshipBlock(interaction.relationshipSignalData) { onUpdate(interaction.copy(relationshipSignalData = it)) }
        3 -> OutcomeBlock(interaction.outcomeData) { onUpdate(interaction.copy(outcomeData = it)) }
    }

    OutlinedTextField(
        value = interaction.notes,
        onValueChange = { onUpdate(interaction.copy(notes = it)) },
        label = { Text("Notes about this interaction") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        maxLines = 3
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunicationBlock(data: CommunicationData, onUpdate: (CommunicationData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("They initiated contact", data.theyInitiatedContact) { onUpdate(data.copy(theyInitiatedContact = it)) }
        SwitchField("I initiated contact", data.iInitiatedContact) { onUpdate(data.copy(iInitiatedContact = it)) }
        SwitchField("They replied", data.theyReplied) { onUpdate(data.copy(theyReplied = it)) }
        SwitchField("Left me on read", data.leftOnRead) { onUpdate(data.copy(leftOnRead = it)) }
        SwitchField("I was left on read", data.iWasLeftOnRead) { onUpdate(data.copy(iWasLeftOnRead = it)) }
        SwitchField("Kept conversation going", data.keptConversationGoing) { onUpdate(data.copy(keptConversationGoing = it)) }
        SwitchField("Conversation ended abruptly", data.conversationEndedAbruptly) { onUpdate(data.copy(conversationEndedAbruptly = it)) }
        SwitchField("Used emoji", data.usedEmoji) { onUpdate(data.copy(usedEmoji = it)) }
        SwitchField("Asked a question", data.askedQuestion) { onUpdate(data.copy(askedQuestion = it)) }
        OutlinedTextField(
            value = if (data.totalMessageCount == 0) "" else data.totalMessageCount.toString(),
            onValueChange = { onUpdate(data.copy(totalMessageCount = it.toIntOrNull() ?: 0)) },
            label = { Text("Total messages") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
    }
}

@Composable
private fun InPersonBlock(data: InPersonData, onUpdate: (InPersonData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("Saw in person", data.sawInPerson) { onUpdate(data.copy(sawInPerson = it)) }
        SwitchField("Talked in person", data.talkedInPerson) { onUpdate(data.copy(talkedInPerson = it)) }
        SwitchField("They approached me", data.theyApproachedMe) { onUpdate(data.copy(theyApproachedMe = it)) }
        SwitchField("I approached them", data.iApproachedThem) { onUpdate(data.copy(iApproachedThem = it)) }
        SwitchField("Walked together", data.walkedTogether) { onUpdate(data.copy(walkedTogether = it)) }
        SwitchField("They smiled", data.theySmiled) { onUpdate(data.copy(theySmiled = it)) }
        SwitchField("They laughed", data.theyLaughed) { onUpdate(data.copy(theyLaughed = it)) }
        SliderField("Eye contact (0-10)", data.eyeContactLevel) { onUpdate(data.copy(eyeContactLevel = it)) }
        SliderField("Physical proximity (0-10)", data.physicalProximity) { onUpdate(data.copy(physicalProximity = it)) }
        SliderField("Felt natural (0-10)", data.feltNatural) { onUpdate(data.copy(feltNatural = it)) }
        SliderField("Felt awkward (0-10)", data.feltAwkward) { onUpdate(data.copy(feltAwkward = it)) }
    }
}

@Composable
private fun RelationshipBlock(data: RelationshipSignalData, onUpdate: (RelationshipSignalData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("These are your interpretations, not objective facts.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        SliderField("Felt Prioritized", data.feltPrioritized) { onUpdate(data.copy(feltPrioritized = it)) }
        SliderField("Felt Ignored", data.feltIgnored) { onUpdate(data.copy(feltIgnored = it)) }
        SliderField("Felt Chosen", data.feltChosen) { onUpdate(data.copy(feltChosen = it)) }
        SliderField("Felt Optional", data.feltOptional) { onUpdate(data.copy(feltOptional = it)) }
        SliderField("Felt Safe with Them", data.feltSafeWithThem) { onUpdate(data.copy(feltSafeWithThem = it)) }
        SliderField("Felt Confused by Them", data.feltConfusedByThem) { onUpdate(data.copy(feltConfusedByThem = it)) }
        SliderField("Felt They Were Warm", data.feltTheyWereWarm) { onUpdate(data.copy(feltTheyWereWarm = it)) }
        SliderField("Felt They Were Distant", data.feltTheyWereDistant) { onUpdate(data.copy(feltTheyWereDistant = it)) }
        SliderField("Felt Mutual Interest", data.feltMutualInterest) { onUpdate(data.copy(feltMutualInterest = it)) }
        SliderField("Felt Connection Stable", data.feltConnectionStable) { onUpdate(data.copy(feltConnectionStable = it)) }
    }
}

@Composable
private fun OutcomeBlock(data: OutcomeData, onUpdate: (OutcomeData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("Felt better after", data.feltBetterAfter) { onUpdate(data.copy(feltBetterAfter = it)) }
        SwitchField("Felt worse after", data.feltWorseAfter) { onUpdate(data.copy(feltWorseAfter = it)) }
        SwitchField("Situation resolved", data.situationResolved) { onUpdate(data.copy(situationResolved = it)) }
        SwitchField("Talked again later", data.talkedAgainLater) { onUpdate(data.copy(talkedAgainLater = it)) }
        SwitchField("They followed up", data.theyFollowedUp) { onUpdate(data.copy(theyFollowedUp = it)) }
        SwitchField("My prediction was correct", data.myPredictionWasCorrect) { onUpdate(data.copy(myPredictionWasCorrect = it)) }
        SwitchField("Anxiety was a false alarm", data.anxietyWasFalseAlarm) { onUpdate(data.copy(anxietyWasFalseAlarm = it)) }
        SliderField("Connection improved", data.connectionImproved) { onUpdate(data.copy(connectionImproved = it)) }
        SliderField("Connection declined", data.connectionDeclined) { onUpdate(data.copy(connectionDeclined = it)) }
    }
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
private fun PersonPickerDialog(people: List<Person>, onSelect: (Person) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person") },
        text = {
            if (people.isEmpty()) {
                Text("All your people are already in this entry. Go to People to add more.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    people.forEach { person ->
                        TextButton(onClick = { onSelect(person) }, modifier = Modifier.fillMaxWidth()) {
                            Text(person.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
