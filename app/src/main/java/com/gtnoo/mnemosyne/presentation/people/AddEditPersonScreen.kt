package com.gtnoo.mnemosyne.presentation.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.domain.model.RelationshipType
import com.gtnoo.mnemosyne.ui.components.SwitchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    personId: String?,
    onBack: () -> Unit,
    viewModel: AddEditPersonViewModel = hiltViewModel()
) {
    val existingPerson by viewModel.person.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var relationshipType by remember { mutableStateOf(RelationshipType.FRIEND) }
    var isFavorite by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (!personId.isNullOrBlank()) viewModel.loadPerson(personId)
    }

    LaunchedEffect(existingPerson) {
        existingPerson?.let { p ->
            name = p.displayName
            relationshipType = p.relationshipType
            isFavorite = p.isFavorite
            notes = p.notes
        }
    }

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (personId.isNullOrBlank()) "Add Person" else "Edit Person") },
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
                onValueChange = { name = it },
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
                                relationshipType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            SwitchField(
                label = "Mark as Favorite",
                checked = isFavorite,
                onCheckedChange = { isFavorite = it }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    viewModel.savePerson(
                        id = if (personId.isNullOrBlank()) null else personId,
                        name = name,
                        type = relationshipType,
                        usualPlaceId = null,
                        isFavorite = isFavorite,
                        notes = notes
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
}
