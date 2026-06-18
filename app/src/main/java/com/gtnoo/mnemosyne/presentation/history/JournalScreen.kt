package com.gtnoo.mnemosyne.presentation.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.gtnoo.mnemosyne.presentation.medicine.MedicineListContent
import com.gtnoo.mnemosyne.presentation.people.PeopleDirectoryContent
import com.gtnoo.mnemosyne.presentation.places.PlacesDirectoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onEntryClick: (String) -> Unit,
    onNewEntry: () -> Unit,
    onPersonClick: (String) -> Unit,
    onAddPerson: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onAddPlace: () -> Unit,
    onMedicineClick: (String) -> Unit,
    onAddMedicine: () -> Unit,
    onEditMedicine: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
                            onClick = { selectedTab = index },
                            text = { Text(label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(onClick = onNewEntry) {
                    Icon(Icons.Default.Add, contentDescription = "New Entry")
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
