package com.gtnoo.mnemosyne.presentation.similarity

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.service.EntryService
import com.gtnoo.mnemosyne.domain.service.SimilarityService
import com.gtnoo.mnemosyne.ui.components.EntryCard
import com.gtnoo.mnemosyne.ui.components.SectionLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimilarityViewModel @Inject constructor(
    private val entryService: EntryService,
    private val entryRepository: EntryRepository,
    private val similarityService: SimilarityService
) : ViewModel() {
    private val _targetEntry = MutableStateFlow<DailyEntry?>(null)
    val targetEntry: StateFlow<DailyEntry?> = _targetEntry.asStateFlow()

    private val _results = MutableStateFlow<List<SimilarityResult>>(emptyList())
    val results: StateFlow<List<SimilarityResult>> = _results.asStateFlow()

    private val _selectedType = MutableStateFlow(SimilarityType.FULL_VECTOR)
    val selectedType: StateFlow<SimilarityType> = _selectedType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(entryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val target = entryService.getEntry(entryId) ?: return@launch
                _targetEntry.value = target
                val allEntries = entryRepository.getAll()
                _results.value = similarityService.findSimilarEntries(target, allEntries, _selectedType.value)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changeType(type: SimilarityType) {
        _selectedType.value = type
        val target = _targetEntry.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allEntries = entryRepository.getAll()
                _results.value = similarityService.findSimilarEntries(target, allEntries, type)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarityResultsScreen(
    entryId: String,
    onBack: () -> Unit,
    onEntryClick: (String) -> Unit,
    viewModel: SimilarityViewModel = hiltViewModel()
) {
    val targetEntry by viewModel.targetEntry.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) { viewModel.load(entryId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Similar Entries") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))

            targetEntry?.let { entry ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Comparing to: ${entry.entryDate}", modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = showTypeDropdown, onExpandedChange = { showTypeDropdown = it }) {
                OutlinedTextField(
                    value = selectedType.label, onValueChange = {}, readOnly = true,
                    label = { Text("Similarity Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }) {
                    SimilarityType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.label) }, onClick = {
                            viewModel.changeType(type); showTypeDropdown = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (results.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No similar entries found", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Add more entries to enable pattern detection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text("${results.size} similar entries found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(results, key = { it.comparedEntry.id }) { result ->
                        SimilarityResultCard(result = result, onClick = { onEntryClick(result.comparedEntry.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SimilarityResultCard(result: SimilarityResult, onClick: () -> Unit) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(result.explanation, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                OutcomeIndicator(result.comparedEntry)
            }
        }
        EntryCard(entry = result.comparedEntry, onClick = onClick)
    }
}

@Composable
private fun OutcomeIndicator(entry: DailyEntry) {
    val outcomes = entry.interactions.map { it.outcomeData }
    val feltBetter = outcomes.count { it.feltBetterAfter }
    val feltWorse = outcomes.count { it.feltWorseAfter }
    if (outcomes.isEmpty()) return
    val color = when {
        feltBetter > feltWorse -> MaterialTheme.colorScheme.primary
        feltWorse > feltBetter -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        feltBetter > feltWorse -> "Better after"
        feltWorse > feltBetter -> "Worse after"
        else -> "Neutral"
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = color)
}
