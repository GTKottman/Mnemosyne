package com.gtnoo.mnemosyne.presentation.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.data.remote.geocoding.GeocodingClient
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.PersonService
import com.gtnoo.mnemosyne.domain.service.PlaceService
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
    private val personService: PersonService,
    private val placeRepository: PlaceRepository,
    private val placeService: PlaceService,
    private val geocodingClient: GeocodingClient
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

    private val _latStr = MutableStateFlow("")
    val latStr: StateFlow<String> = _latStr.asStateFlow()

    private val _lngStr = MutableStateFlow("")
    val lngStr: StateFlow<String> = _lngStr.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private var linkedPlaceId: String? = null
    private var initialized = false

    fun updateName(value: String) { _name.value = value }
    fun updateRelationshipType(value: RelationshipType) { _relationshipType.value = value }
    fun updateIsFavorite(value: Boolean) { _isFavorite.value = value }
    fun updateNotes(value: String) { _notes.value = value }

    fun updateLatLng(lat: Double, lng: Double) {
        _latStr.value = lat.toString()
        _lngStr.value = lng.toString()
    }

    fun clearLocation() {
        _latStr.value = ""
        _lngStr.value = ""
        _city.value = ""
        _state.value = ""
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): Pair<String, String>? =
        geocodingClient.reverseGeocode(lat, lng)

    fun applyReverseGeocode(city: String, state: String) {
        _city.value = city
        _state.value = state
    }

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
                linkedPlaceId = p.usualPlaceId
                p.usualPlaceId?.let { placeId ->
                    placeRepository.getById(placeId)?.let { place ->
                        _latStr.value = place.latitude?.toString() ?: ""
                        _lngStr.value = place.longitude?.toString() ?: ""
                        _city.value = place.city
                        _state.value = place.state
                    }
                }
            }
        }
    }

    fun savePerson(id: String?) {
        viewModelScope.launch {
            val usualPlaceId = resolveUsualPlaceId()
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

    private suspend fun resolveUsualPlaceId(): String? {
        val lat = _latStr.value.toDoubleOrNull()
        val lng = _lngStr.value.toDoubleOrNull()
        if (lat == null || lng == null) return null

        val label = "${_name.value}'s location"
        val existingId = linkedPlaceId
        if (existingId != null) {
            val existing = placeRepository.getById(existingId)
            if (existing != null) {
                placeService.updatePlace(
                    existing.copy(
                        label = label,
                        city = _city.value,
                        state = _state.value,
                        latitude = lat,
                        longitude = lng
                    )
                )
                return existingId
            }
        }

        val newPlace = placeService.addPlace(
            label = label,
            type = PlaceType.OTHER,
            city = _city.value,
            state = _state.value,
            latitude = lat,
            longitude = lng
        )
        linkedPlaceId = newPlace.id
        return newPlace.id
    }
}
