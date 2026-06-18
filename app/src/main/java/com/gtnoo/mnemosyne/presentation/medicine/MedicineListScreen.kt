package com.gtnoo.mnemosyne.presentation.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.Medicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onAddMedicine: () -> Unit,
    onMedicineClick: (String) -> Unit,
    onEditMedicine: (String) -> Unit,
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val medicinesWithBottles by viewModel.medicinesWithBottles.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Medicine?>(null) }

    val active = medicinesWithBottles.filter { it.medicine.isActive }
    val inactive = medicinesWithBottles.filter { !it.medicine.isActive }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Medicines & Supplements") },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (active.isEmpty() && inactive.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No medicines yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap + to add a medicine or supplement",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (active.isNotEmpty()) {
                item {
                    Text(
                        "Active",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                items(active) { mwb ->
                    MedicineCard(
                        item = mwb,
                        onClick = { onMedicineClick(mwb.medicine.id) },
                        onEdit = { onEditMedicine(mwb.medicine.id) },
                        onToggleActive = { viewModel.toggleActive(mwb.medicine) },
                        onDelete = { showDeleteDialog = mwb.medicine },
                        onTakePill = { viewModel.takePill(mwb.medicine.id) }
                    )
                }
            }

            if (inactive.isNotEmpty()) {
                item {
                    Text(
                        "Inactive",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(inactive) { mwb ->
                    MedicineCard(
                        item = mwb,
                        onClick = { onMedicineClick(mwb.medicine.id) },
                        onEdit = { onEditMedicine(mwb.medicine.id) },
                        onToggleActive = { viewModel.toggleActive(mwb.medicine) },
                        onDelete = { showDeleteDialog = mwb.medicine },
                        onTakePill = { viewModel.takePill(mwb.medicine.id) }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { medicine ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete ${medicine.name}?") },
            text = { Text("This will permanently delete the medicine and all its bottle and dose history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedicine(medicine.id)
                    showDeleteDialog = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MedicineCard(
    item: MedicineWithBottle,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onTakePill: () -> Unit
) {
    val bottle = item.currentBottle
    val lowStock = bottle != null && bottle.pillsRemaining <= 7
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); showMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (item.medicine.isActive) "Mark Inactive" else "Mark Active") },
                            onClick = { onToggleActive(); showMenu = false },
                            leadingIcon = {
                                Icon(
                                    if (item.medicine.isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }

            if (item.medicine.notes.isNotBlank()) {
                Text(
                    item.medicine.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (bottle != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("${bottle.pillsRemaining} / ${bottle.pillsTotal} pills")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = if (lowStock) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        ) else AssistChipDefaults.assistChipColors()
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${bottle.mgPerPill}mg each") }
                    )
                    if (bottle.pillsRemaining > 0) {
                        FilledTonalButton(
                            onClick = onTakePill,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Take pill", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (lowStock) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Low stock") },
                            leadingIcon = {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            } else {
                Text(
                    "No bottle on record",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
