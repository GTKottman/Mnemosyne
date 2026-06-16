package com.gtnoo.mnemosyne.presentation.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val placeService: PlaceService
) : ViewModel() {

    private val _place = MutableStateFlow<SavedPlace?>(null)
    val place: StateFlow<SavedPlace?> = _place.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadPlace(id: String) {
        viewModelScope.launch { _place.value = placeRepository.getById(id) }
    }

    fun savePlace(
        id: String?,
        label: String,
        placeType: PlaceType,
        city: String,
        state: String,
        country: String,
        latitude: Double?,
        longitude: Double?,
        timezone: String,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = id?.let { placeRepository.getById(it) }
            val place = (existing ?: SavedPlace(label = label)).copy(
                label = label, placeType = placeType, city = city, state = state,
                country = country, latitude = latitude, longitude = longitude,
                timezone = timezone, notes = notes
            )
            if (existing != null) placeService.updatePlace(place)
            else placeService.addPlace(label, placeType, city, state, country, latitude, longitude, timezone, notes)
            _saved.value = true
        }
    }
}
