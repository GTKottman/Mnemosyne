package com.gtnoo.mnemosyne.presentation.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.data.remote.geocoding.GeocodingClient
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.PlaceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val placeService: PlaceService
) : ViewModel() {

    val places: StateFlow<List<SavedPlace>> = placeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePlace(id: String) {
        viewModelScope.launch { placeService.deletePlace(id) }
    }
}

@HiltViewModel
class AddEditPlaceViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val placeService: PlaceService,
    private val geocodingClient: GeocodingClient
) : ViewModel() {

    private val _place = MutableStateFlow<SavedPlace?>(null)
    val place: StateFlow<SavedPlace?> = _place.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _placeType = MutableStateFlow(PlaceType.OTHER)
    val placeType: StateFlow<PlaceType> = _placeType.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _country = MutableStateFlow("")
    val country: StateFlow<String> = _country.asStateFlow()

    private val _latStr = MutableStateFlow("")
    val latStr: StateFlow<String> = _latStr.asStateFlow()

    private val _lngStr = MutableStateFlow("")
    val lngStr: StateFlow<String> = _lngStr.asStateFlow()

    private val _timezone = MutableStateFlow("America/Chicago")
    val timezone: StateFlow<String> = _timezone.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private var initialized = false

    fun updateLabel(value: String) { _label.value = value }
    fun updatePlaceType(value: PlaceType) { _placeType.value = value }
    fun updateCity(value: String) { _city.value = value }
    fun updateState(value: String) { _state.value = value }
    fun updateCountry(value: String) { _country.value = value }
    fun updateLatStr(value: String) { _latStr.value = value }
    fun updateLngStr(value: String) { _lngStr.value = value }
    fun updateTimezone(value: String) { _timezone.value = value }
    fun updateNotes(value: String) { _notes.value = value }

    suspend fun geocodeCity(city: String): Pair<Double, Double>? =
        geocodingClient.geocode(city)

    fun loadPlace(id: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            placeRepository.getById(id)?.let { p ->
                _place.value = p
                _label.value = p.label
                _placeType.value = p.placeType
                _city.value = p.city
                _state.value = p.state
                _country.value = p.country
                _latStr.value = p.latitude?.toString() ?: ""
                _lngStr.value = p.longitude?.toString() ?: ""
                _timezone.value = p.timezone
                _notes.value = p.notes
            }
        }
    }

    fun savePlace(id: String?) {
        viewModelScope.launch {
            val existing = id?.let { placeRepository.getById(it) }
            val place = (existing ?: SavedPlace(label = _label.value)).copy(
                label = _label.value, placeType = _placeType.value, city = _city.value,
                state = _state.value, country = _country.value,
                latitude = _latStr.value.toDoubleOrNull(), longitude = _lngStr.value.toDoubleOrNull(),
                timezone = _timezone.value, notes = _notes.value
            )
            if (existing != null) placeService.updatePlace(place)
            else placeService.addPlace(
                _label.value, _placeType.value, _city.value, _state.value, _country.value,
                _latStr.value.toDoubleOrNull(), _lngStr.value.toDoubleOrNull(), _timezone.value, _notes.value
            )
            _saved.value = true
        }
    }
}
