package com.gtnoo.mnemosyne.presentation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtnoo.mnemosyne.presentation.medicine.MedicineListContent
import com.gtnoo.mnemosyne.presentation.people.PeopleDirectoryContent
import com.gtnoo.mnemosyne.presentation.places.PlacesDirectoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onEntryClick: (String) -> Unit,
    onNewEntry: () -> Unit,
    onNewInteraction: () -> Unit,
    onPersonClick: (String) -> Unit,
    onAddPerson: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onAddPlace: () -> Unit,
    onMedicineClick: (String) -> Unit,
    onAddMedicine: () -> Unit,
    onEditMedicine: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var fabExpanded by remember { mutableStateOf(false) }

    val tabLabels = listOf("Entries", "People", "Places", "Medicines")

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Journal") },
                    windowInsets = WindowInsets(0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                fabExpanded = false
                            },
                            text = { Text(label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedVisibility(
                        visible = fabExpanded,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        "Log Interaction",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = { onNewInteraction(); fabExpanded = false }
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Log Interaction")
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        "Log Daily Journal",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = { onNewEntry(); fabExpanded = false }
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = "Log Daily Journal")
                                }
                            }
                        }
                    }
                    FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                        Icon(
                            if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (fabExpanded) "Close" else "New"
                        )
                    }
                }
                1 -> FloatingActionButton(onClick = onAddPerson) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Person")
                }
                2 -> FloatingActionButton(onClick = onAddPlace) {
                    Icon(Icons.Default.AddLocation, contentDescription = "Add Place")
                }
                3 -> FloatingActionButton(onClick = onAddMedicine) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> EntryHistoryContent(
                modifier = Modifier.padding(padding),
                onEntryClick = onEntryClick
            )
            1 -> PeopleDirectoryContent(
                modifier = Modifier.padding(padding),
                onPersonClick = onPersonClick
            )
            2 -> PlacesDirectoryContent(
                modifier = Modifier.padding(padding),
                onPlaceClick = onPlaceClick
            )
            3 -> MedicineListContent(
                modifier = Modifier.padding(padding),
                onMedicineClick = onMedicineClick,
                onEditMedicine = onEditMedicine
            )
        }
    }
}
