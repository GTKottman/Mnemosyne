package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import javax.inject.Inject

class PlaceService @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val entryRepository: EntryRepository
) {
    suspend fun addPlace(
        label: String,
        type: PlaceType,
        city: String = "",
        state: String = "",
        country: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        timezone: String = "America/Chicago",
        notes: String = ""
    ): SavedPlace {
        val place = SavedPlace(
            label = label, placeType = type, city = city, state = state,
            country = country, latitude = latitude, longitude = longitude,
            timezone = timezone, notes = notes
        )
        return placeRepository.save(place)
    }

    suspend fun updatePlace(place: SavedPlace): SavedPlace {
        placeRepository.update(place)
        return place
    }

    suspend fun deletePlace(id: String) = placeRepository.delete(id)

    suspend fun getAllPlaces(): List<SavedPlace> = placeRepository.getAll()

    suspend fun getSummaryForPlace(place: SavedPlace, range: DateRange): PlaceSummary {
        val entries = entryRepository.getByPlaceId(place.id).filter {
            !it.entryDate.isBefore(range.start) && !it.entryDate.isAfter(range.end)
        }
        val moodScores = entries.map { it.emotionData.happy.toDouble() }
        val energyScores = entries.map { it.healthContextData.energyLevel.toDouble() }
        val lastDate = entries.maxByOrNull { it.entryDate }?.entryDate
        val trend = entries.sortedBy { it.entryDate }.map { it.entryDate to it.emotionData.happy.toDouble() }
        return PlaceSummary(
            place = place,
            totalVisits = entries.size,
            averageMood = if (moodScores.isEmpty()) 0.0 else moodScores.average(),
            averageEnergy = if (energyScores.isEmpty()) 0.0 else energyScores.average(),
            lastVisitDate = lastDate,
            moodTrend = trend
        )
    }
}
