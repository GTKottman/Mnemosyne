package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.data.remote.weather.WeatherApiClient
import com.gtnoo.mnemosyne.domain.model.DailyEntry
import com.gtnoo.mnemosyne.domain.model.SavedPlace
import com.gtnoo.mnemosyne.domain.model.WeatherSnapshot
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import com.gtnoo.mnemosyne.domain.repository.WeatherRepository
import java.time.LocalDate
import javax.inject.Inject

class WeatherService @Inject constructor(
    private val apiClient: WeatherApiClient,
    private val weatherRepository: WeatherRepository,
    private val placeRepository: PlaceRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun getWeatherFor(place: SavedPlace, date: LocalDate): WeatherSnapshot? {
        val cached = weatherRepository.findByPlaceAndDate(place.id, date)
        if (cached != null) return cached
        if (place.latitude == null || place.longitude == null) return null
        val fetched = apiClient.fetchWeather(place, date) ?: return null
        weatherRepository.save(fetched)
        return fetched
    }

    suspend fun getWeatherForEntry(entry: DailyEntry): List<WeatherSnapshot> {
        val settings = settingsRepository.getSettings()
        val placesToFetch = mutableSetOf<SavedPlace>()

        settings.homePlaceId?.let { placeRepository.getById(it) }?.let { placesToFetch.add(it) }
        placesToFetch.addAll(entry.placesVisited)
        entry.interactions.forEach { interaction ->
            interaction.person.usualPlaceId?.let { placeRepository.getById(it) }
                ?.let { placesToFetch.add(it) }
        }

        return placesToFetch.mapNotNull { place -> getWeatherFor(place, entry.entryDate) }
    }
}
