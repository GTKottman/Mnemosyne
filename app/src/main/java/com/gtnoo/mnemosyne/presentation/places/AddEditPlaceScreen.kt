package com.gtnoo.mnemosyne.presentation.places

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.PlaceType
import com.gtnoo.mnemosyne.ui.components.LocationPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlaceScreen(
    placeId: String?,
    onBack: () -> Unit,
    viewModel: AddEditPlaceViewModel = hiltViewModel()
) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val label by viewModel.label.collectAsStateWithLifecycle()
    val placeType by viewModel.placeType.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val country by viewModel.country.collectAsStateWithLifecycle()
    val latStr by viewModel.latStr.collectAsStateWithLifecycle()
    val lngStr by viewModel.lngStr.collectAsStateWithLifecycle()
    val timezone by viewModel.timezone.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showTypeDropdown by rememberSaveable { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var mapInitialLat by remember { mutableStateOf<Double?>(null) }
    var mapInitialLng by remember { mutableStateOf<Double?>(null) }
    val scope = rememberCoroutineScope()

    val hasCoords = latStr.toDoubleOrNull() != null && lngStr.toDoubleOrNull() != null

    LaunchedEffect(placeId) {
        if (!placeId.isNullOrBlank()) viewModel.loadPlace(placeId)
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
                value = label, onValueChange = { viewModel.updateLabel(it) },
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
                            viewModel.updatePlaceType(type); showTypeDropdown = false
                        })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = city, onValueChange = { viewModel.updateCity(it) },
                    label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = state, onValueChange = { viewModel.updateState(it) },
                    label = { Text("State") }, modifier = Modifier.weight(0.6f), singleLine = true)
            }
            OutlinedTextField(value = country, onValueChange = { viewModel.updateCountry(it) },
                label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            Text("Location (for weather)", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            OutlinedButton(
                onClick = {
                    scope.launch {
                        mapInitialLat = latStr.toDoubleOrNull()
                        mapInitialLng = lngStr.toDoubleOrNull()
                        if (mapInitialLat == null && city.isNotBlank()) {
                            val coords = viewModel.geocodeCity(city)
                            mapInitialLat = coords?.first
                            mapInitialLng = coords?.second
                        }
                        showMapPicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PinDrop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (hasCoords) "Change pinned location" else "Pin on map")
            }
            if (hasCoords) {
                AssistChip(
                    onClick = {},
                    label = { Text("Location pinned") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize))
                    }
                )
            }
            OutlinedTextField(value = timezone, onValueChange = { viewModel.updateTimezone(it) },
                label = { Text("Timezone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = notes, onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 3)

            Button(
                onClick = {
                    viewModel.savePlace(id = if (placeId.isNullOrBlank()) null else placeId)
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

    if (showMapPicker) {
        LocationPickerDialog(
            initialLat = mapInitialLat,
            initialLng = mapInitialLng,
            cityHint = city,
            onConfirm = { lat, lng ->
                viewModel.updateLatStr(lat.toString())
                viewModel.updateLngStr(lng.toString())
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}
