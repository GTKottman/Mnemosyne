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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Header: date + tags
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                e.entryDate.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            if (e.tags.isNotEmpty()) {
                                Text(
                                    e.tags.joinToString(", ") { "#$it" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    CollapsibleSection("My Feelings", initiallyExpanded = true) {
                        EmotionDetailSection(e.emotionData)
                    }
                }

                item {
                    CollapsibleSection("My Thoughts") {
                        ThoughtDetailSection(e.thoughtPatternData)
                    }
                }

                item {
                    CollapsibleSection("Body & State") {
                        HealthDetailSection(e.healthContextData, settings)
                    }
                }

                item {
                    CollapsibleSection("Inputs & Intake") {
                        IntakeDetailSection(e.intakeData)
                    }
                }

                item {
                    CollapsibleSection("Environment & Day Shape") {
                        EnvironmentDetailSection(e.environmentData)
                    }
                }

                if (e.placesVisited.isNotEmpty()) {
                    item {
                        CollapsibleSection("Places Visited") {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(e.placesVisited) { place -> PlaceChip(place = place) }
                            }
                        }
                    }
                }

                if (e.weatherSnapshots.isNotEmpty()) {
                    item {
                        CollapsibleSection("Weather") {
                            e.weatherSnapshots.forEach { snapshot ->
                                WeatherCard(snapshot, useFahrenheit = settings.useFahrenheit)
                            }
                        }
                    }
                }

                if (e.freeformNotes.isNotBlank()) {
                    item {
                        CollapsibleSection("Notes") {
                            Text(
                                e.freeformNotes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section composables
// ---------------------------------------------------------------------------

@Composable
private fun EmotionDetailSection(emotion: EmotionData) {
    StatValueRow(emotion.happiness, leftLabel = "Sadness", rightLabel = "Happiness")
    StatValueRow(emotion.safety, leftLabel = "Threat", rightLabel = "Safety")
    StatValueRow(emotion.calm, leftLabel = "Agitation", rightLabel = "Calm")
    StatValueRow(emotion.connection, leftLabel = "Isolation", rightLabel = "Connection")
    StatValueRow(emotion.clarity, leftLabel = "Confusion", rightLabel = "Clarity")
    StatValueRow(emotion.capacity, leftLabel = "Overwhelm", rightLabel = "Capacity")
    StatValueRow(emotion.hope, leftLabel = "Hopelessness", rightLabel = "Hope")
    StatValueRow(emotion.worthiness, leftLabel = "Shame", rightLabel = "Worthiness")
    StatValueRow(emotion.peace, leftLabel = "Anger", rightLabel = "Peace")
    StatValueRow(emotion.energy, leftLabel = "Exhaustion", rightLabel = "Energy")
    StatValueRow(emotion.agency, leftLabel = "Helplessness", rightLabel = "Agency")
    StatValueRow(emotion.presence, leftLabel = "Numbness", rightLabel = "Presence")

    Spacer(Modifier.height(4.dp))

    RadarChart(
        data = listOf(
            "Mood" to emotion.happiness,
            "Safety" to emotion.safety,
            "Calm" to emotion.calm,
            "Connect" to emotion.connection,
            "Clarity" to emotion.clarity,
            "Capacity" to emotion.capacity,
            "Hope" to emotion.hope,
            "Worth" to emotion.worthiness,
            "Peace" to emotion.peace,
            "Energy" to emotion.energy,
            "Agency" to emotion.agency,
            "Presence" to emotion.presence
        )
    )
}

@Composable
private fun ThoughtDetailSection(thought: ThoughtPatternData) {
    StatValueRow(thought.ruminationLevel, label = "Rumination Level")
    StatValueRow(thought.intrusiveThoughts, label = "Intrusive Thoughts")
    StatValueRow(thought.worryAnticipation, label = "Worry / Anticipation")
    StatValueRow(thought.catastrophizing, label = "Catastrophizing")
    StatValueRow(thought.reassuranceUrge, label = "Reassurance Urge")
    StatValueRow(thought.mentalClarity, label = "Mental Clarity")
    StatValueRow(thought.uncertaintyTolerance, label = "Uncertainty Tolerance")
    StatValueRow(thought.decisionFriction, label = "Decision Friction")
    StatValueRow(thought.cognitiveFlexibility, label = "Cognitive Flexibility")
    StatValueRow(thought.selfTalkTone, leftLabel = "Self-Critical", rightLabel = "Kind & Supportive")

    Spacer(Modifier.height(4.dp))

    RadarChart(
        data = listOf(
            "Ruminate" to thought.ruminationLevel,
            "Intrusive" to thought.intrusiveThoughts,
            "Worry" to thought.worryAnticipation,
            "Catastro." to thought.catastrophizing,
            "Reassure" to thought.reassuranceUrge,
            "Clarity" to thought.mentalClarity,
            "Uncertain" to thought.uncertaintyTolerance,
            "Decision" to thought.decisionFriction,
            "Flexible" to thought.cognitiveFlexibility,
            "Self-Talk" to thought.selfTalkTone
        )
    )
}

@Composable
private fun HealthDetailSection(health: HealthContextData, settings: AppSettings) {
    StatValueRow(health.energyLevel, leftLabel = "Low Energy", rightLabel = "High Energy")
    StatValueRow(health.socialBattery, leftLabel = "Drained", rightLabel = "Socially Available")
    StatValueRow(health.executiveFunction, leftLabel = "Can't Initiate", rightLabel = "Can Act & Organize")
    StatValueRow(health.mentalBandwidth, leftLabel = "Overloaded", rightLabel = "Spacious")
    StatValueRow(health.bodyDiscomfort, leftLabel = "Comfortable", rightLabel = "Discomfort")
    StatValueRow(health.sensoryEnvironmentalStrain, leftLabel = "Easy Environment", rightLabel = "Taxing Environment")
    StatValueRow(health.sleepQuality, leftLabel = "Poor / Restless", rightLabel = "Good / Restorative")
    StatValueRow(health.stressLoad, leftLabel = "Calm Day", rightLabel = "High-Pressure Day")

    val anyStress = settings.showWorkSchoolPressure || settings.showMoneyPressure ||
        settings.showRelationshipPressure || settings.showFamilyPressure ||
        settings.showHealthPressure || settings.showTimePressure

    if (anyStress) {
        Spacer(Modifier.height(4.dp))
        Text(
            "Stress Pressures",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (settings.showWorkSchoolPressure) StatValueRow(health.workSchoolPressure, label = "Work / School")
        if (settings.showMoneyPressure) StatValueRow(health.moneyPressure, label = "Money")
        if (settings.showRelationshipPressure) StatValueRow(health.relationshipPressure, label = "Relationship / Social")
        if (settings.showFamilyPressure) StatValueRow(health.familyPressure, label = "Family")
        if (settings.showHealthPressure) StatValueRow(health.healthPressure, label = "Health")
        if (settings.showTimePressure) StatValueRow(health.timePressure, label = "Time")
    }

    Spacer(Modifier.height(4.dp))

    val radarData = buildList {
        add("Energy" to health.energyLevel)
        add("Social" to health.socialBattery)
        add("Exec Fn" to health.executiveFunction)
        add("Bandwidth" to health.mentalBandwidth)
        add("Discomfort" to health.bodyDiscomfort)
        add("Sensory" to health.sensoryEnvironmentalStrain)
        add("Sleep" to health.sleepQuality)
        add("Stress" to health.stressLoad)
        if (settings.showWorkSchoolPressure) add("Work" to health.workSchoolPressure)
        if (settings.showMoneyPressure) add("Money" to health.moneyPressure)
        if (settings.showRelationshipPressure) add("Relation" to health.relationshipPressure)
        if (settings.showFamilyPressure) add("Family" to health.familyPressure)
        if (settings.showHealthPressure) add("Health" to health.healthPressure)
        if (settings.showTimePressure) add("Time" to health.timePressure)
    }

    RadarChart(data = radarData)
}

@Composable
private fun IntakeDetailSection(intake: IntakeData) {
    // Non-scale fields shown as plain stat rows
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            "Hours Slept",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${intake.hoursSlept}h",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            "Caffeine",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${intake.caffeineIntakeMg} mg",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }

    StatValueRow(intake.foodAdequacy, leftLabel = "Didn't Eat", rightLabel = "Well Nourished")
    StatValueRow(intake.hydration, leftLabel = "Dehydrated", rightLabel = "Well Hydrated")
    StatValueRow(intake.movementLevel, leftLabel = "No Movement", rightLabel = "Heavy Exercise")

    Spacer(Modifier.height(4.dp))

    RadarChart(
        data = listOf(
            "Food" to intake.foodAdequacy,
            "Hydration" to intake.hydration,
            "Movement" to intake.movementLevel
        )
    )
}

@Composable
private fun EnvironmentDetailSection(env: EnvironmentData) {
    StatValueRow(env.timeOutsideSunlight, leftLabel = "Indoor All Day", rightLabel = "Outside a Lot")
    StatValueRow(env.screenLoad, leftLabel = "Low Screen Time", rightLabel = "High Screen Time")
    StatValueRow(env.socialExposure, leftLabel = "No Exposure", rightLabel = "Lots of Exposure")
    StatValueRow(env.noveltyDisruption, leftLabel = "Routine Day", rightLabel = "Disrupted / Unusual")
    StatValueRow(env.physicalSpaceQuality, leftLabel = "Messy / Chaotic", rightLabel = "Clean / Supportive")
    StatValueRow(env.weatherImpact, leftLabel = "No Effect", rightLabel = "Strongly Affected")

    Spacer(Modifier.height(4.dp))

    RadarChart(
        data = listOf(
            "Sunlight" to env.timeOutsideSunlight,
            "Screens" to env.screenLoad,
            "Social" to env.socialExposure,
            "Novelty" to env.noveltyDisruption,
            "Space" to env.physicalSpaceQuality,
            "Weather" to env.weatherImpact
        )
    )
}
