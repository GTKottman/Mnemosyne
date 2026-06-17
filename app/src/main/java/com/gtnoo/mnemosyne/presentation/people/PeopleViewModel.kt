package com.gtnoo.mnemosyne.presentation.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.service.PersonService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personService: PersonService
) : ViewModel() {

    val people: StateFlow<List<Person>> = personRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePerson(id: String) {
        viewModelScope.launch { personService.deletePerson(id) }
    }

    fun toggleFavorite(person: Person) {
        viewModelScope.launch {
            personService.updatePerson(person.copy(isFavorite = !person.isFavorite))
        }
    }
}

@HiltViewModel
class AddEditPersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personService: PersonService
) : ViewModel() {

    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _relationshipType = MutableStateFlow(RelationshipType.FRIEND)
    val relationshipType: StateFlow<RelationshipType> = _relationshipType.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private var initialized = false

    fun updateName(value: String) { _name.value = value }
    fun updateRelationshipType(value: RelationshipType) { _relationshipType.value = value }
    fun updateIsFavorite(value: Boolean) { _isFavorite.value = value }
    fun updateNotes(value: String) { _notes.value = value }

    fun loadPerson(id: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            personRepository.getById(id)?.let { p ->
                _person.value = p
                _name.value = p.displayName
                _relationshipType.value = p.relationshipType
                _isFavorite.value = p.isFavorite
                _notes.value = p.notes
            }
        }
    }

    fun savePerson(id: String?, usualPlaceId: String?) {
        viewModelScope.launch {
            val existing = id?.let { personRepository.getById(it) }
            val person = (existing ?: Person(displayName = "")).copy(
                displayName = _name.value,
                relationshipType = _relationshipType.value,
                usualPlaceId = usualPlaceId,
                isFavorite = _isFavorite.value,
                notes = _notes.value
            )
            if (existing != null) personService.updatePerson(person)
            else personService.addPerson(_name.value, _relationshipType.value, usualPlaceId, _notes.value)
            _saved.value = true
        }
    }
}
