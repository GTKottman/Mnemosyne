package com.gtnoo.mnemosyne.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.data.remote.geocoding.GeocodingClient
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import com.gtnoo.mnemosyne.domain.service.PersonService
import com.gtnoo.mnemosyne.domain.service.PlaceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val placeService: PlaceService,
    private val personService: PersonService,
    private val settingsRepository: SettingsRepository,
    private val geocodingClient: GeocodingClient
) : ViewModel() {

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun completeOnboarding(
        homePlaceLabel: String,
        homePlaceCity: String,
        homePlaceState: String,
        homePlaceLat: Double?,
        homePlaceLng: Double?,
        firstPersonName: String?,
        firstPersonType: RelationshipType?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val homePlace = placeService.addPlace(
                    label = homePlaceLabel.ifBlank { "Home" },
                    type = PlaceType.HOME,
                    city = homePlaceCity,
                    state = homePlaceState,
                    latitude = homePlaceLat,
                    longitude = homePlaceLng
                )
                val settings = settingsRepository.getSettings()
                settingsRepository.updateSettings(
                    settings.copy(homePlaceId = homePlace.id, onboardingComplete = true)
                )
                if (!firstPersonName.isNullOrBlank() && firstPersonType != null) {
                    personService.addPerson(firstPersonName, firstPersonType)
                }
            } finally {
                _isLoading.value = false
                _completed.value = true
            }
        }
    }

    suspend fun geocodeCity(city: String): Pair<Double, Double>? =
        geocodingClient.geocode(city)

    fun skipOnboarding() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            settingsRepository.updateSettings(settings.copy(onboardingComplete = true))
            _completed.value = true
        }
    }
}
