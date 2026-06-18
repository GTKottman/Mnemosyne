package com.gtnoo.mnemosyne.presentation.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.MedicineBottle
import com.gtnoo.mnemosyne.domain.model.MedicineDose
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    medicineId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsStateWithLifecycle()
    val currentBottle by viewModel.currentBottle.collectAsStateWithLifecycle()
    val doseHistory by viewModel.doseHistory.collectAsStateWithLifecycle()
    val todaysDoses by viewModel.todaysDoses.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRefillSheet by remember { mutableStateOf(false) }
    var showChangeDosageSheet by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) { viewModel.load(medicineId) }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(medicine?.name ?: "Medicine") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    medicine?.let { med ->
                        IconButton(onClick = { onEdit(med.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        TextButton(onClick = { viewModel.toggleActive() }) {
                            Text(if (med.isActive) "Mark Inactive" else "Mark Active")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            medicine?.let { med ->
                if (med.notes.isNotBlank()) {
                    item {
                        Text(
                            med.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    "Current Bottle",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                if (currentBottle != null) {
                    CurrentBottleCard(
                        bottle = currentBottle!!,
                        todaysDoseCount = todaysDoses.sumOf { it.pillsTaken },
                        dateFormatter = dateFormatter,
                        onRefill = { showRefillSheet = true },
                        onChangeDosage = { showChangeDosageSheet = true },
                        onTakePill = { viewModel.takePill() }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Column {
                                Text("No active bottle", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Add a refill to start tracking doses again",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showRefillSheet = true }) { Text("Add Refill") }
                        OutlinedButton(onClick = { showChangeDosageSheet = true }) { Text("New Dosage") }
                    }
                }
            }

            if (doseHistory.isNotEmpty()) {
                item {
                    Text(
                        "Dose History",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(doseHistory) { dose ->
                    DoseHistoryRow(dose = dose, dateFormatter = dateFormatter, dateTimeFormatter = dateTimeFormatter)
                }
            }
        }
    }

    if (showRefillSheet && currentBottle != null) {
        RefillBottomSheet(
            currentMg = currentBottle!!.mgPerPill,
            onDismiss = { showRefillSheet = false },
            onConfirm = { newPillCount ->
                viewModel.refill(newPillCount)
                showRefillSheet = false
            }
        )
    }

    if (showChangeDosageSheet) {
        ChangeDosageBottomSheet(
            onDismiss = { showChangeDosageSheet = false },
            onConfirm = { newMg, newPillCount ->
                viewModel.changeDosage(newMg, newPillCount)
                showChangeDosageSheet = false
            }
        )
    }
}

@Composable
private fun CurrentBottleCard(
    bottle: MedicineBottle,
    todaysDoseCount: Int,
    dateFormatter: DateTimeFormatter,
    onRefill: () -> Unit,
    onChangeDosage: () -> Unit,
    onTakePill: () -> Unit
) {
    val lowStock = bottle.pillsRemaining <= 7
    val outOfStock = bottle.pillsRemaining <= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (lowStock)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${bottle.mgPerPill}mg per pill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Opened ${bottle.openedDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${bottle.pillsRemaining}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "of ${bottle.pillsTotal} pills",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { if (bottle.pillsTotal > 0) bottle.pillsRemaining.toFloat() / bottle.pillsTotal else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onTakePill,
                modifier = Modifier.fillMaxWidth(),
                enabled = !outOfStock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Take Pill", style = MaterialTheme.typography.titleSmall)
            }

            if (todaysDoseCount > 0) {
                Text(
                    "Taken today: $todaysDoseCount pill${if (todaysDoseCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefill, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refill")
                }
                OutlinedButton(onClick = onChangeDosage, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Change Dosage")
                }
            }
        }
    }
}

@Composable
private fun DoseHistoryRow(
    dose: MedicineDose,
    dateFormatter: DateTimeFormatter,
    dateTimeFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Column {
                val label = if (dose.takenAt != null) {
                    dose.takenAt.format(dateTimeFormatter)
                } else {
                    dose.date.format(dateFormatter)
                }
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(
            "${dose.pillsTaken} pill${if (dose.pillsTaken > 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefillBottomSheet(
    currentMg: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var pillCountStr by remember { mutableStateOf("") }
    val pillCount = pillCountStr.toIntOrNull()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Refill Bottle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Same dosage: ${currentMg}mg per pill",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = pillCountStr,
                onValueChange = { pillCountStr = it },
                label = { Text("Number of pills in new bottle") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("pills") },
                singleLine = true
            )
            Button(
                onClick = { pillCount?.let { onConfirm(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = pillCount != null && pillCount > 0
            ) {
                Text("Confirm Refill")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeDosageBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var mgStr by remember { mutableStateOf("") }
    var pillCountStr by remember { mutableStateOf("") }
    val mg = mgStr.toIntOrNull()
    val pillCount = pillCountStr.toIntOrNull()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Change Dosage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "This will retire the current bottle and start a new one with the new dosage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = mgStr,
                onValueChange = { mgStr = it },
                label = { Text("New mg per pill") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("mg") },
                singleLine = true
            )
            OutlinedTextField(
                value = pillCountStr,
                onValueChange = { pillCountStr = it },
                label = { Text("Number of pills in new bottle") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("pills") },
                singleLine = true
            )
            Button(
                onClick = { if (mg != null && pillCount != null) onConfirm(mg, pillCount) },
                modifier = Modifier.fillMaxWidth(),
                enabled = mg != null && mg > 0 && pillCount != null && pillCount > 0
            ) {
                Text("Confirm Change")
            }
        }
    }
}
