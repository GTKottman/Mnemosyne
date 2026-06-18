package com.gtnoo.mnemosyne.presentation.people

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.ImportantDateKind
import com.gtnoo.mnemosyne.ui.components.SwitchField
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val PRESET_LABELS = listOf("Graduation", "Interview", "Anniversary")
private val DAYS_BEFORE_OPTIONS = listOf(0, 1, 2, 3, 7, 14, 30)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ImportantDateDialog(
    existing: ImportantDate?,
    personId: String,
    onDismiss: () -> Unit,
    onSave: (ImportantDate) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val isBirthday = existing?.kind == ImportantDateKind.BIRTHDAY
    var label by rememberSaveable(existing?.id) { mutableStateOf(existing?.label ?: "") }
    var selectedPreset by rememberSaveable(existing?.id) { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf(existing?.date) }
    var isRecurring by rememberSaveable(existing?.id) {
        mutableStateOf(existing?.isRecurring ?: false)
    }
    var notifyEnabled by rememberSaveable(existing?.id) {
        mutableStateOf(existing?.notifyEnabled ?: false)
    }
    var notifyDaysBefore by rememberSaveable(existing?.id) {
        mutableIntStateOf(existing?.notifyDaysBefore ?: 0)
    }
    var remindToLogInteraction by rememberSaveable(existing?.id) {
        mutableStateOf(existing?.remindToLogInteraction ?: false)
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDaysDropdown by rememberSaveable { mutableStateOf(false) }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(existing) {
        if (existing != null) {
            label = existing.label
            date = existing.date
            isRecurring = existing.isRecurring
            notifyEnabled = existing.notifyEnabled
            notifyDaysBefore = existing.notifyDaysBefore
            remindToLogInteraction = existing.remindToLogInteraction
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (date ?: LocalDate.now()).toEpochDay() * 86_400_000L
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Important Date" else "Edit ${existing.label}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isBirthday) {
                    Text("Label", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRESET_LABELS.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset || label == preset,
                                onClick = {
                                    selectedPreset = preset
                                    label = preset
                                },
                                label = { Text(preset) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = label,
                        onValueChange = {
                            label = it
                            selectedPreset = null
                        },
                        label = { Text("Custom label") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Not set",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isBirthday) "Birthday" else "Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isBirthday) {
                    SwitchField("Repeats yearly", isRecurring) { isRecurring = it }
                }

                SwitchField("Enable reminder", notifyEnabled) { enabled ->
                    if (enabled && date == null) return@SwitchField
                    if (enabled && notificationPermission != null && !notificationPermission.status.isGranted) {
                        notificationPermission.launchPermissionRequest()
                    }
                    notifyEnabled = enabled
                }

                if (notifyEnabled) {
                    ExposedDropdownMenuBox(
                        expanded = showDaysDropdown,
                        onExpandedChange = { showDaysDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = formatDaysBefore(notifyDaysBefore),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Remind me") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showDaysDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showDaysDropdown,
                            onDismissRequest = { showDaysDropdown = false }
                        ) {
                            DAYS_BEFORE_OPTIONS.forEach { days ->
                                DropdownMenuItem(
                                    text = { Text(formatDaysBefore(days)) },
                                    onClick = {
                                        notifyDaysBefore = days
                                        showDaysDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                SwitchField(
                    label = "Remind to log interaction",
                    checked = remindToLogInteraction,
                    onCheckedChange = { remindToLogInteraction = it }
                )
                Text(
                    "On this date, also prompt me to record an interaction with this person.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val saved = (existing ?: ImportantDate(personId = personId, label = "")).copy(
                        label = if (isBirthday) "Birthday" else label.trim(),
                        date = date,
                        kind = existing?.kind ?: ImportantDateKind.CUSTOM,
                        isRecurring = if (isBirthday) true else isRecurring,
                        notifyEnabled = notifyEnabled && date != null,
                        notifyDaysBefore = notifyDaysBefore,
                        remindToLogInteraction = remindToLogInteraction
                    )
                    onSave(saved)
                },
                enabled = (isBirthday || label.isNotBlank())
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (existing != null && !isBirthday && onDelete != null) {
                    TextButton(onClick = { onDelete(existing.id) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = LocalDate.ofEpochDay(millis / 86_400_000L)
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
}

private fun formatDaysBefore(days: Int): String = when (days) {
    0 -> "Day of"
    1 -> "1 day before"
    else -> "$days days before"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayDateField(
    birthday: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (birthday ?: LocalDate.now()).toEpochDay() * 86_400_000L
    )

    OutlinedTextField(
        value = birthday?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Not set",
        onValueChange = {},
        readOnly = true,
        label = { Text("Birthday") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick birthday")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateChange(LocalDate.ofEpochDay(millis / 86_400_000L))
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
}
