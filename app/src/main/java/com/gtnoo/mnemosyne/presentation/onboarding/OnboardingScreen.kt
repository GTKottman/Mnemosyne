package com.gtnoo.mnemosyne.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.RelationshipType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var step by remember { mutableStateOf(0) }

    // Home place fields
    var homePlaceLabel by remember { mutableStateOf("Home") }
    var homePlaceCity by remember { mutableStateOf("") }
    var homePlaceState by remember { mutableStateOf("") }
    var homePlaceLat by remember { mutableStateOf("") }
    var homePlaceLng by remember { mutableStateOf("") }

    // First person fields
    var personName by remember { mutableStateOf("") }
    var personType by remember { mutableStateOf(RelationshipType.FRIEND) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(completed) { if (completed) onComplete() }

    when (step) {
        0 -> WelcomeStep(onNext = { step = 1 }, onSkip = { viewModel.skipOnboarding() })
        1 -> HomePlaceStep(
            label = homePlaceLabel, city = homePlaceCity, state = homePlaceState,
            lat = homePlaceLat, lng = homePlaceLng,
            onLabelChange = { homePlaceLabel = it }, onCityChange = { homePlaceCity = it },
            onStateChange = { homePlaceState = it }, onLatChange = { homePlaceLat = it },
            onLngChange = { homePlaceLng = it },
            onNext = { step = 2 },
            onBack = { step = 0 }
        )
        2 -> FirstPersonStep(
            personName = personName, personType = personType,
            showTypeDropdown = showTypeDropdown,
            onNameChange = { personName = it },
            onTypeChange = { personType = it },
            onDropdownChange = { showTypeDropdown = it },
            isLoading = isLoading,
            onFinish = {
                viewModel.completeOnboarding(
                    homePlaceLabel = homePlaceLabel, homePlaceCity = homePlaceCity,
                    homePlaceState = homePlaceState,
                    homePlaceLat = homePlaceLat.toDoubleOrNull(),
                    homePlaceLng = homePlaceLng.toDoubleOrNull(),
                    firstPersonName = personName.takeIf { it.isNotBlank() },
                    firstPersonType = personType
                )
            },
            onBack = { step = 1 }
        )
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit, onSkip: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            Text("Mnemosyne", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
            Text(
                "A private space to track your patterns across people, places, emotions, and time.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Your data stays on your device. No accounts. No ads. No cloud sync by default.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Get Started")
            }
            TextButton(onClick = onSkip) { Text("Skip Setup") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomePlaceStep(
    label: String, city: String, state: String, lat: String, lng: String,
    onLabelChange: (String) -> Unit, onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit, onLatChange: (String) -> Unit, onLngChange: (String) -> Unit,
    onNext: () -> Unit, onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Default.Home, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text("Where are you based?", style = MaterialTheme.typography.headlineSmall)
        Text("This lets Mnemosyne capture weather for your location automatically.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(value = label, onValueChange = onLabelChange,
            label = { Text("Place name (e.g. Home)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = city, onValueChange = onCityChange,
                label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = state, onValueChange = onStateChange,
                label = { Text("State") }, modifier = Modifier.weight(0.6f), singleLine = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = lat, onValueChange = onLatChange, label = { Text("Latitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = lng, onValueChange = onLngChange, label = { Text("Longitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f), singleLine = true)
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(onClick = onNext, modifier = Modifier.weight(1f)) { Text("Next") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstPersonStep(
    personName: String, personType: RelationshipType, showTypeDropdown: Boolean,
    onNameChange: (String) -> Unit, onTypeChange: (RelationshipType) -> Unit,
    onDropdownChange: (Boolean) -> Unit, isLoading: Boolean,
    onFinish: () -> Unit, onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Default.People, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text("Add your first person", style = MaterialTheme.typography.headlineSmall)
        Text("You can add friends, family, partners, or anyone you want to track interactions with. You can skip this and add people later.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(value = personName, onValueChange = onNameChange,
            label = { Text("Their name (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        ExposedDropdownMenuBox(expanded = showTypeDropdown, onExpandedChange = onDropdownChange) {
            OutlinedTextField(
                value = personType.label, onValueChange = {}, readOnly = true,
                label = { Text("Relationship") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = showTypeDropdown, onDismissRequest = { onDropdownChange(false) }) {
                RelationshipType.entries.forEach { type ->
                    DropdownMenuItem(text = { Text(type.label) }, onClick = {
                        onTypeChange(type); onDropdownChange(false)
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), enabled = !isLoading) {
                Text("Back")
            }
            Button(onClick = onFinish, modifier = Modifier.weight(1f), enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Finish Setup")
            }
        }
    }
}
