package com.gtnoo.mnemosyne.presentation.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.data.remote.weather.WeatherApiClient
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.service.ImportantDateService
import com.gtnoo.mnemosyne.domain.service.PersonService
import com.gtnoo.mnemosyne.notification.ImportantDateScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CurrentWeatherUiState {
    data object Loading : CurrentWeatherUiState
    data class Success(val temperatureCelsius: Double, val condition: WeatherCondition) : CurrentWeatherUiState
    data object Unavailable : CurrentWeatherUiState
}

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val placeRepository: PlaceRepository,
    private val importantDateService: ImportantDateService,
    private val weatherApiClient: WeatherApiClient,
    @ApplicationContext private val appContext: android.content.Context
) : ViewModel() {
    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    private val _linkedPlace = MutableStateFlow<SavedPlace?>(null)
    val linkedPlace: StateFlow<SavedPlace?> = _linkedPlace.asStateFlow()

    private val _summary = MutableStateFlow<PersonSummary?>(null)
    val summary: StateFlow<PersonSummary?> = _summary.asStateFlow()

    private val _importantDates = MutableStateFlow<List<ImportantDate>>(emptyList())
    val importantDates: StateFlow<List<ImportantDate>> = _importantDates.asStateFlow()

    private val _currentWeather = MutableStateFlow<CurrentWeatherUiState>(CurrentWeatherUiState.Loading)
    val currentWeather: StateFlow<CurrentWeatherUiState> = _currentWeather.asStateFlow()

    private var observeJob: kotlinx.coroutines.Job? = null

    fun load(personId: String) {
        viewModelScope.launch {
            val p = personRepository.getById(personId) ?: return@launch
            _person.value = p
            val place = p.usualPlaceId?.let { placeRepository.getById(it) }
            _linkedPlace.value = place
            val range = DateRange.lastThreeMonths()
            _summary.value = personService.getSummaryForPerson(p, range)

            if (place?.latitude != null && place.longitude != null) {
                _currentWeather.value = CurrentWeatherUiState.Loading
                val result = weatherApiClient.fetchCurrentWeather(place)
                _currentWeather.value = if (result != null) {
                    CurrentWeatherUiState.Success(result.first, result.second)
                } else {
                    CurrentWeatherUiState.Unavailable
                }
            } else {
                _currentWeather.value = CurrentWeatherUiState.Unavailable
            }
        }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            importantDateService.observeByPersonId(personId).collect { dates ->
                _importantDates.value = dates
            }
        }
    }

    fun toggleFavorite() {
        val p = _person.value ?: return
        viewModelScope.launch {
            val updated = p.copy(isFavorite = !p.isFavorite)
            personService.updatePerson(updated)
            _person.value = updated
        }
    }

    fun saveImportantDate(date: ImportantDate) {
        viewModelScope.launch {
            importantDateService.saveImportantDate(date)
            ImportantDateScheduler.schedule(appContext)
        }
    }

    fun deleteImportantDate(id: String) {
        viewModelScope.launch {
            importantDateService.deleteImportantDate(id)
            ImportantDateScheduler.schedule(appContext)
        }
    }
}
