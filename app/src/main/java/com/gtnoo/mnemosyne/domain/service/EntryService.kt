package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.DailyEntry
import com.gtnoo.mnemosyne.domain.model.FilterCriteria
import com.gtnoo.mnemosyne.domain.model.SavedPlace
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class EntryService @Inject constructor(
    private val entryRepository: EntryRepository,
    private val weatherService: WeatherService,
    private val settingsRepository: SettingsRepository
) {
    suspend fun createEntry(date: LocalDate): DailyEntry = DailyEntry(entryDate = date)

    suspend fun saveEntry(entry: DailyEntry, fetchWeather: Boolean = true): DailyEntry {
        val settings = settingsRepository.getSettings()
        val entryWithWeather = if (fetchWeather && settings.autoFetchWeather) {
            val snapshots = weatherService.getWeatherForEntry(entry)
            entry.copy(weatherSnapshots = snapshots, lastEditedAt = LocalDateTime.now())
        } else {
            entry.copy(lastEditedAt = LocalDateTime.now())
        }
        return entryRepository.save(entryWithWeather)
    }

    suspend fun updateEntry(entry: DailyEntry): DailyEntry {
        val updated = entry.copy(lastEditedAt = LocalDateTime.now())
        entryRepository.update(updated)
        return updated
    }

    suspend fun deleteEntry(id: String) = entryRepository.delete(id)

    suspend fun getEntry(id: String): DailyEntry? = entryRepository.getById(id)

    suspend fun getEntriesForDate(date: LocalDate): List<DailyEntry> =
        entryRepository.getByDate(date)

    suspend fun getEntriesInRange(start: LocalDate, end: LocalDate): List<DailyEntry> =
        entryRepository.getBetweenDates(start, end)

    suspend fun getEntriesForPerson(personId: String): List<DailyEntry> =
        entryRepository.getByPersonId(personId)

    suspend fun getEntriesForPlace(placeId: String): List<DailyEntry> =
        entryRepository.getByPlaceId(placeId)

    suspend fun getAllEntries(): List<DailyEntry> = entryRepository.getAll()

    suspend fun filterEntries(criteria: FilterCriteria): List<DailyEntry> {
        val start = criteria.startDate ?: LocalDate.now().minusYears(1)
        val end = criteria.endDate ?: LocalDate.now()
        var entries = entryRepository.getBetweenDates(start, end)

        criteria.personId?.let { pid ->
            entries = entries.filter { entry -> entry.interactions.any { it.person.id == pid } }
        }
        criteria.placeId?.let { placeId ->
            entries = entries.filter { entry -> entry.placesVisited.any { it.id == placeId } }
        }
        if (criteria.tags.isNotEmpty()) {
            entries = entries.filter { entry -> criteria.tags.any { it in entry.tags } }
        }
        criteria.maxHappiness?.let { max ->
            entries = entries.filter { it.emotionData.happiness <= max }
        }
        criteria.minConnection?.let { min ->
            entries = entries.filter { it.emotionData.connection >= min }
        }
        if (criteria.onlyDaysWithInteractions) {
            entries = entries.filter { it.interactions.isNotEmpty() }
        }
        if (criteria.onlyHighRuminationDays) {
            entries = entries.filter { it.thoughtPatternData.ruminationLevel >= 7 }
        }
        if (criteria.onlyWeatherAffectedDays) {
            entries = entries.filter { it.weatherSnapshots.isNotEmpty() }
        }
        return entries
    }
}
