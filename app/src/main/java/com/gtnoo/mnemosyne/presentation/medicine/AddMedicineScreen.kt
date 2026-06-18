package com.gtnoo.mnemosyne.presentation.medicine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    medicineId: String? = null,
    onBack: () -> Unit,
    viewModel: AddMedicineViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val mgPerPill by viewModel.mgPerPill.collectAsStateWithLifecycle()
    val totalPills by viewModel.totalPills.collectAsStateWithLifecycle()
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()
    val notifyEnabled by viewModel.notifyEnabled.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = !medicineId.isNullOrBlank()

    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) {
        if (!medicineId.isNullOrBlank()) viewModel.loadMedicine(medicineId)
    }
    LaunchedEffect(saved) { if (saved) onBack() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialTime = reminderTime ?: LocalTime.of(8, 0),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                viewModel.updateReminderTime(time)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Medicine" else "Add Medicine") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }) {
                        Text("Save")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Medicine Info", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Name") },
                placeholder = { Text("e.g. Vitamin D, Sertraline") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Purpose, brand, any reminders") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            HorizontalDivider()

            Text("Reminders", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("Reminder time", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (reminderTime != null)
                                    reminderTime!!.format(DateTimeFormatter.ofPattern("h:mm a"))
                                else "Tap to set a time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (reminderTime != null) {
                        IconButton(onClick = { viewModel.updateReminderTime(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear reminder time", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable reminder notification", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (reminderTime != null)
                            "Notify me daily to take this medicine"
                        else
                            "Set a reminder time first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notifyEnabled,
                    onCheckedChange = { viewModel.updateNotifyEnabled(it) },
                    enabled = reminderTime != null
                )
            }

            if (!isEditing) {
                HorizontalDivider()

                Text("First Bottle", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = mgPerPill,
                    onValueChange = { viewModel.updateMgPerPill(it) },
                    label = { Text("mg per pill") },
                    placeholder = { Text("e.g. 500") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("mg") }
                )

                OutlinedTextField(
                    value = totalPills,
                    onValueChange = { viewModel.updateTotalPills(it) },
                    label = { Text("Number of pills in bottle") },
                    placeholder = { Text("e.g. 60") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("pills") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set reminder time") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
