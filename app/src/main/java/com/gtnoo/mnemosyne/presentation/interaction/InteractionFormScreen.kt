package com.gtnoo.mnemosyne.presentation.interaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.gtnoo.mnemosyne.domain.model.InteractionMode
import com.gtnoo.mnemosyne.domain.model.Person
import com.gtnoo.mnemosyne.ui.components.CollapsibleSection
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionFormScreen(
    interactionId: String?,
    initialDate: String?,
    initialPersonId: String? = null,
    onBack: () -> Unit,
    viewModel: InteractionFormViewModel = hiltViewModel()
) {
    val interaction by viewModel.interaction.collectAsStateWithLifecycle()
    val allPeople by viewModel.allPeople.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPersonPicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    val currentDate = interaction?.date
        ?: initialDate?.let { LocalDate.parse(it) }
        ?: LocalDate.now()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toEpochDay() * 86_400_000L
    )

    LaunchedEffect(interactionId, initialDate, initialPersonId) {
        when {
            !interactionId.isNullOrBlank() -> viewModel.loadInteraction(interactionId)
            !initialPersonId.isNullOrBlank() -> {
                val date = initialDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
                viewModel.initForPersonAndDate(initialPersonId, date)
            }
            !initialDate.isNullOrBlank() -> viewModel.initForDate(LocalDate.parse(initialDate))
            else -> viewModel.initEmpty()
        }
    }

    LaunchedEffect(saved) { if (saved) onBack() }
    LaunchedEffect(deleted) { if (deleted) onBack() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    val isEditing = !interactionId.isNullOrBlank()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Interaction" else "Log Interaction") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.saveInteraction() },
                            enabled = interaction != null
                        ) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Date picker
            item {
                OutlinedTextField(
                    value = currentDate.toString(),
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
            }

            // Person selector
            item {
                val person = interaction?.person
                OutlinedButton(
                    onClick = { showPersonPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(person?.displayName ?: "Select Person")
                }
            }

            // Mode selector
            item {
                val currentMode = interaction?.mode ?: InteractionMode.DIGITAL_ONLY
                CollapsibleSection("Interaction Mode") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InteractionMode.entries.forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = currentMode == mode,
                                    onClick = { viewModel.updateMode(mode) }
                                )
                                Text(mode.label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Interaction detail tabs (only shown once a person is selected)
            if (interaction != null) {
                item {
                    CollapsibleSection("Interaction Details", initiallyExpanded = true) {
                    InteractionBlock(
                        interaction = interaction!!,
                        onUpdate = { viewModel.updateInteraction(it) }
                    )
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.saveInteraction() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Interaction")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = LocalDate.ofEpochDay(millis / 86_400_000L)
                        viewModel.updateDate(newDate)
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

    if (showPersonPicker) {
        PersonPickerDialog(
            people = allPeople,
            onSelect = { person ->
                if (interaction == null) {
                    viewModel.ensureInteractionForDate(currentDate, person)
                }
                viewModel.selectPerson(person)
                showPersonPicker = false
            },
            onDismiss = { showPersonPicker = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Interaction") },
            text = { Text("Remove this interaction? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteInteraction()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PersonPickerDialog(
    people: List<Person>,
    onSelect: (Person) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Person") },
        text = {
            if (people.isEmpty()) {
                Text("No people found. Go to People to add someone.")
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
