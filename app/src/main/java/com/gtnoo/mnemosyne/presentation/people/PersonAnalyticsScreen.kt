package com.gtnoo.mnemosyne.presentation.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.service.PersonService
import com.gtnoo.mnemosyne.domain.service.VisualizationService
import com.gtnoo.mnemosyne.ui.components.SectionLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PersonAnalyticsViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val vizService: VisualizationService
) : ViewModel() {
    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    private val _connectionTrend = MutableStateFlow<List<Pair<LocalDate, Double>>>(emptyList())
    val connectionTrend: StateFlow<List<Pair<LocalDate, Double>>> = _connectionTrend.asStateFlow()

    private val _commFrequency = MutableStateFlow<List<Pair<LocalDate, Double>>>(emptyList())
    val commFrequency: StateFlow<List<Pair<LocalDate, Double>>> = _commFrequency.asStateFlow()

    fun load(personId: String) {
        viewModelScope.launch {
            val p = personRepository.getById(personId) ?: return@launch
            _person.value = p
            val range = DateRange.lastThreeMonths()
            _connectionTrend.value = vizService.buildConnectionTrend(p, range).map { it.date to it.value }
            _commFrequency.value = vizService.buildCommunicationFrequency(p, range).map { it.date to it.value }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonAnalyticsScreen(
    personId: String,
    onBack: () -> Unit,
    viewModel: PersonAnalyticsViewModel = hiltViewModel()
) {
    val person by viewModel.person.collectAsStateWithLifecycle()
    val connectionTrend by viewModel.connectionTrend.collectAsStateWithLifecycle()
    val commFrequency by viewModel.commFrequency.collectAsStateWithLifecycle()

    LaunchedEffect(personId) { viewModel.load(personId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Analytics: ${person?.displayName ?: ""}") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item { SectionLabel("Connection Over Time (last 90 days)") }
            item { BarChart(data = connectionTrend, yLabel = "Connection (0-10)", color = MaterialTheme.colorScheme.primary) }
            item { SectionLabel("Message Count Over Time") }
            item { BarChart(data = commFrequency, yLabel = "Messages", color = MaterialTheme.colorScheme.secondary) }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<LocalDate, Double>>,
    yLabel: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Card(modifier = modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No data available", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    val max = data.maxOf { it.second }.coerceAtLeast(1.0)
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.takeLast(30).forEach { (_, value) ->
                    val frac = (value / max).toFloat().coerceAtLeast(0.02f)
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 1.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(frac),
                            color = color, shape = MaterialTheme.shapes.extraSmall
                        ) {}
                    }
                }
            }
            Text(yLabel, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
