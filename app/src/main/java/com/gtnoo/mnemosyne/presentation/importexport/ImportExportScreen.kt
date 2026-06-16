package com.gtnoo.mnemosyne.presentation.importexport

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.ImportResult
import com.gtnoo.mnemosyne.domain.service.ImportExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val importExportService: ImportExportService
) : ViewModel() {
    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _lastImportResult = MutableStateFlow<ImportResult?>(null)
    val lastImportResult: StateFlow<ImportResult?> = _lastImportResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun exportJson(): File? {
        var result: File? = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                result = importExportService.exportFullBackup()
                _status.value = "Backup exported to: ${result?.name}"
            } catch (e: Exception) {
                _status.value = "Export failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        return result
    }

    fun exportCsv() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = importExportService.exportToCsv()
                _status.value = "CSV exported to: ${file.name}"
            } catch (e: Exception) {
                _status.value = "CSV export failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importFromJson(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = importExportService.importFromJson(content)
                _lastImportResult.value = result
                _status.value = if (result.success) {
                    "Imported ${result.entriesImported} entries (${result.entriesSkipped} skipped)"
                } else {
                    "Import failed: ${result.errors.firstOrNull()}"
                }
            } catch (e: Exception) {
                _status.value = "Import failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatus() { _status.value = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onBack: () -> Unit,
    viewModel: ImportExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsStateWithLifecycle()
    val importResult by viewModel.lastImportResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
            content?.let { text -> viewModel.importFromJson(text) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            status?.let { msg ->
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("Import failed") || msg.startsWith("Export failed"))
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(msg, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearStatus() }) {
                            Icon(Icons.Default.Close, "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text("Export", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = { viewModel.exportJson() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Backup, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export Full Backup (JSON)")
            }

            OutlinedButton(
                onClick = { viewModel.exportCsv() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.TableChart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export to CSV")
            }

            HorizontalDivider()

            Text("Import", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Import from JSON Backup")
            }

            importResult?.let { result ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Last Import Result", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Entries imported: ${result.entriesImported}", style = MaterialTheme.typography.bodySmall)
                        Text("Entries skipped: ${result.entriesSkipped}", style = MaterialTheme.typography.bodySmall)
                        if (result.warnings.isNotEmpty()) {
                            Text("Warnings: ${result.warnings.size}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            HorizontalDivider()

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Privacy Note", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text(
                        "All data stays on your device. Exported files contain your personal information. Store them securely and only share with trusted tools.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
