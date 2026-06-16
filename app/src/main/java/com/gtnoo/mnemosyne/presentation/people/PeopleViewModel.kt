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

    fun loadPerson(id: String) {
        viewModelScope.launch {
            _person.value = personRepository.getById(id)
        }
    }

    fun savePerson(
        id: String?,
        name: String,
        type: RelationshipType,
        usualPlaceId: String?,
        isFavorite: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = id?.let { personRepository.getById(it) }
            val person = (existing ?: Person(displayName = "")).copy(
                displayName = name,
                relationshipType = type,
                usualPlaceId = usualPlaceId,
                isFavorite = isFavorite,
                notes = notes
            )
            if (existing != null) personService.updatePerson(person)
            else personService.addPerson(name, type, usualPlaceId, notes)
            _saved.value = true
        }
    }
}
