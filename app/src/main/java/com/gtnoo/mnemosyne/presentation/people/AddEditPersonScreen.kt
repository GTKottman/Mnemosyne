package com.gtnoo.mnemosyne.presentation.people

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
import com.gtnoo.mnemosyne.domain.model.RelationshipType
import com.gtnoo.mnemosyne.ui.components.LocationPickerDialog
import com.gtnoo.mnemosyne.ui.components.SwitchField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    personId: String?,
    onBack: () -> Unit,
    viewModel: AddEditPersonViewModel = hiltViewModel()
) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val relationshipType by viewModel.relationshipType.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val latStr by viewModel.latStr.collectAsStateWithLifecycle()
    val lngStr by viewModel.lngStr.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val birthday by viewModel.birthday.collectAsStateWithLifecycle()
    var showTypeDropdown by rememberSaveable { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var mapInitialLat by remember { mutableStateOf<Double?>(null) }
    var mapInitialLng by remember { mutableStateOf<Double?>(null) }
    val scope = rememberCoroutineScope()

    val hasCoords = latStr.toDoubleOrNull() != null && lngStr.toDoubleOrNull() != null

    LaunchedEffect(personId) {
        if (!personId.isNullOrBlank()) viewModel.loadPerson(personId)
    }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(if (personId.isNullOrBlank()) "Add Person" else "Edit Person") },
                windowInsets = WindowInsets(0),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = relationshipType.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Relationship Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    RelationshipType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                viewModel.updateRelationshipType(type)
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            SwitchField(
                label = "Mark as Favorite",
                checked = isFavorite,
                onCheckedChange = { viewModel.updateIsFavorite(it) }
            )

            Text(
                "Important Dates",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            BirthdayDateField(
                birthday = birthday,
                onDateChange = { viewModel.updateBirthday(it) }
            )

            Text(
                "Location (for weather)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedButton(
                onClick = {
                    mapInitialLat = latStr.toDoubleOrNull()
                    mapInitialLng = lngStr.toDoubleOrNull()
                    showMapPicker = true
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
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
                val locationLabel = when {
                    city.isNotBlank() && state.isNotBlank() -> "$city, $state"
                    city.isNotBlank() -> city
                    else -> null
                }
                if (locationLabel != null) {
                    Text(
                        locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { viewModel.clearLocation() }) {
                    Text("Remove location")
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    viewModel.savePerson(
                        id = if (personId.isNullOrBlank()) null else personId
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Person")
            }
        }
    }

    if (showMapPicker) {
        LocationPickerDialog(
            initialLat = mapInitialLat,
            initialLng = mapInitialLng,
            cityHint = city,
            onConfirm = { lat, lng ->
                viewModel.updateLatLng(lat, lng)
                scope.launch {
                    viewModel.reverseGeocode(lat, lng)?.let { (c, s) ->
                        viewModel.applyReverseGeocode(c, s)
                    }
                }
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}
