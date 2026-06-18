package com.gtnoo.mnemosyne.presentation.interaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.InteractionRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InteractionFormViewModel @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _interaction = MutableStateFlow<PersonInteraction?>(null)
    val interaction: StateFlow<PersonInteraction?> = _interaction.asStateFlow()

    val allPeople: StateFlow<List<Person>> = personRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var initialized = false

    fun loadInteraction(interactionId: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            val existing = interactionRepository.getById(interactionId)
            _interaction.value = existing
        }
    }

    fun initForDate(date: LocalDate) {
        if (initialized) return
        initialized = true
    }

    fun initForPersonAndDate(personId: String, date: LocalDate) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            val person = personRepository.getById(personId) ?: return@launch
            _interaction.value = PersonInteraction(date = date, person = person)
        }
    }

    fun initEmpty() {
        if (initialized) return
        initialized = true
    }

    fun selectPerson(person: Person) {
        val current = _interaction.value
        val date = current?.date ?: LocalDate.now()
        _interaction.value = (current ?: PersonInteraction(date = date, person = person))
            .copy(person = person)
    }

    fun updateDate(date: LocalDate) {
        _interaction.update { it?.copy(date = date) }
    }

    fun ensureInteractionForDate(date: LocalDate, person: Person) {
        if (_interaction.value == null) {
            _interaction.value = PersonInteraction(date = date, person = person)
        }
    }

    fun updateInteraction(updated: PersonInteraction) {
        _interaction.value = updated
    }

    fun updateMode(mode: InteractionMode) {
        _interaction.update { it?.copy(mode = mode) }
    }

    fun saveInteraction() {
        val current = _interaction.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                interactionRepository.save(current)
                _saved.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save interaction"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteInteraction() {
        val id = _interaction.value?.id ?: return
        viewModelScope.launch {
            try {
                interactionRepository.delete(id)
                _deleted.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete interaction"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
