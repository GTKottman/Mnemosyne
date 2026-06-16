package com.gtnoo.mnemosyne.presentation.places

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.SavedPlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesDirectoryScreen(
    onAddPlace: () -> Unit,
    onPlaceClick: (String) -> Unit,
    viewModel: PlacesViewModel = hiltViewModel()
) {
    val places by viewModel.places.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<SavedPlace?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Places") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlace) {
                Icon(Icons.Default.AddLocation, contentDescription = "Add Place")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            if (places.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No places yet", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add a place", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(places, key = { it.id }) { place ->
                        PlaceCard(place = place, onClick = { onPlaceClick(place.id) },
                            onDelete = { showDeleteDialog = place })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    showDeleteDialog?.let { place ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete ${place.label}?") },
            text = { Text("This will remove the place. Past entries that reference it will still be linked.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlace(place.id)
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
private fun PlaceCard(place: SavedPlace, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(place.label, style = MaterialTheme.typography.titleMedium)
                Text(place.placeType.label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val locationStr = listOfNotNull(
                    place.city.takeIf { it.isNotBlank() },
                    place.state.takeIf { it.isNotBlank() }
                ).joinToString(", ")
                if (locationStr.isNotBlank()) {
                    Text(locationStr, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "View")
                }
            }
        }
    }
}
