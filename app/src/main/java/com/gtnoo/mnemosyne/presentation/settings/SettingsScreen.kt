package com.gtnoo.mnemosyne.presentation.settings

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
import com.gtnoo.mnemosyne.domain.model.AppSettings
import com.gtnoo.mnemosyne.domain.model.SavedPlace
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import com.gtnoo.mnemosyne.ui.components.SwitchField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val places: StateFlow<List<SavedPlace>> = placeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHomePlace(placeId: String?) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(homePlaceId = placeId))
        }
    }

    fun setAutoFetchWeather(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings.value.copy(autoFetchWeather = enabled))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val places by viewModel.places.collectAsStateWithLifecycle()
    var showPlaceDropdown by remember { mutableStateOf(false) }

    val homePlace = places.firstOrNull { it.id == settings.homePlaceId }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Location", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            item {
                ExposedDropdownMenuBox(expanded = showPlaceDropdown, onExpandedChange = { showPlaceDropdown = it }) {
                    OutlinedTextField(
                        value = homePlace?.label ?: "Not set",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Home Place (for weather)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showPlaceDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showPlaceDropdown, onDismissRequest = { showPlaceDropdown = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = {
                            viewModel.setHomePlace(null); showPlaceDropdown = false
                        })
                        places.forEach { place ->
                            DropdownMenuItem(text = { Text(place.label) }, onClick = {
                                viewModel.setHomePlace(place.id); showPlaceDropdown = false
                            })
                        }
                    }
                }
            }
            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Weather", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            item {
                SwitchField(
                    label = "Auto-fetch weather on save",
                    checked = settings.autoFetchWeather,
                    onCheckedChange = { viewModel.setAutoFetchWeather(it) }
                )
            }
            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("About", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Mnemosyne", style = MaterialTheme.typography.titleMedium)
                        Text("Version ${settings.appVersion}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Local-first personal pattern tracking.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("No accounts. No cloud sync. No analytics. Your data stays on your device.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
