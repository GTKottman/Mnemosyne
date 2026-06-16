package com.gtnoo.mnemosyne.presentation.places

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.PlaceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlaceScreen(
    placeId: String?,
    onBack: () -> Unit,
    viewModel: AddEditPlaceViewModel = hiltViewModel()
) {
    val existingPlace by viewModel.place.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    var label by remember { mutableStateOf("") }
    var placeType by remember { mutableStateOf(PlaceType.OTHER) }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var latStr by remember { mutableStateOf("") }
    var lngStr by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("America/Chicago") }
    var notes by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) {
        if (!placeId.isNullOrBlank()) viewModel.loadPlace(placeId)
    }

    LaunchedEffect(existingPlace) {
        existingPlace?.let { p ->
            label = p.label; placeType = p.placeType; city = p.city; state = p.state
            country = p.country; latStr = p.latitude?.toString() ?: ""; lngStr = p.longitude?.toString() ?: ""
            timezone = p.timezone; notes = p.notes
        }
    }

    LaunchedEffect(saved) { if (saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (placeId.isNullOrBlank()) "Add Place" else "Edit Place") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = label, onValueChange = { label = it },
                label = { Text("Place Name *") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            ExposedDropdownMenuBox(expanded = showTypeDropdown, onExpandedChange = { showTypeDropdown = it }) {
                OutlinedTextField(
                    value = placeType.label, onValueChange = {}, readOnly = true,
                    label = { Text("Place Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }) {
                    PlaceType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.label) }, onClick = {
                            placeType = type; showTypeDropdown = false
                        })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = city, onValueChange = { city = it },
                    label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = state, onValueChange = { state = it },
                    label = { Text("State") }, modifier = Modifier.weight(0.6f), singleLine = true)
            }
            OutlinedTextField(value = country, onValueChange = { country = it },
                label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Text("Coordinates (for weather)", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latStr, onValueChange = { latStr = it },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = lngStr, onValueChange = { lngStr = it },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }
            OutlinedTextField(value = timezone, onValueChange = { timezone = it },
                label = { Text("Timezone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 3)

            Button(
                onClick = {
                    viewModel.savePlace(
                        id = if (placeId.isNullOrBlank()) null else placeId,
                        label = label, placeType = placeType, city = city, state = state,
                        country = country, latitude = latStr.toDoubleOrNull(),
                        longitude = lngStr.toDoubleOrNull(), timezone = timezone, notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = label.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Place")
            }
        }
    }
}
