package com.gtnoo.mnemosyne.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.InteractionRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.EntryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyEntryFormViewModel @Inject constructor(
    private val entryService: EntryService,
    private val personRepository: PersonRepository,
    private val placeRepository: PlaceRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _entry = MutableStateFlow(DailyEntry())
    val entry: StateFlow<DailyEntry> = _entry.asStateFlow()

    val allPlaces: StateFlow<List<SavedPlace>> = placeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interactionsForDate: StateFlow<List<PersonInteraction>> = _entry
        .map { it.entryDate }
        .distinctUntilChanged()
        .flatMapLatest { date -> interactionRepository.observeByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var initialized = false

    fun loadEntry(entryId: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            entryService.getEntry(entryId)?.let { _entry.value = it }
        }
    }

    fun initForDate(date: LocalDate) {
        if (initialized) return
        initialized = true
        _entry.value = DailyEntry(entryDate = date)
    }

    fun updateDate(date: LocalDate) {
        _entry.update { it.copy(entryDate = date) }
    }

    fun updateContext(context: EntryContext) {
        _entry.update { it.copy(context = context) }
    }

    fun updateEmotionData(emotion: EmotionData) {
        _entry.update { it.copy(emotionData = emotion) }
    }

    fun updateThoughtData(thought: ThoughtPatternData) {
        _entry.update { it.copy(thoughtPatternData = thought) }
    }

    fun updateHealthData(health: HealthContextData) {
        _entry.update { it.copy(healthContextData = health) }
    }

    fun updateIntakeData(intake: IntakeData) {
        _entry.update { it.copy(intakeData = intake) }
    }

    fun updateEnvironmentData(environment: EnvironmentData) {
        _entry.update { it.copy(environmentData = environment) }
    }

    fun updateNotes(notes: String) {
        _entry.update { it.copy(freeformNotes = notes) }
    }

    fun updateTags(tags: List<String>) {
        _entry.update { it.copy(tags = tags) }
    }

    fun addPlace(place: SavedPlace) {
        if (_entry.value.placesVisited.any { it.id == place.id }) return
        _entry.update { it.copy(placesVisited = it.placesVisited + place) }
    }

    fun removePlace(placeId: String) {
        _entry.update { it.copy(placesVisited = it.placesVisited.filter { p -> p.id != placeId }) }
    }

    fun saveEntry() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                entryService.saveEntry(_entry.value)
                _saved.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save entry"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
