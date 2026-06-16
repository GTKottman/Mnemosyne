package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.DailyEntryDao
import com.gtnoo.mnemosyne.data.local.dao.PersonDao
import com.gtnoo.mnemosyne.data.local.entity.*
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: DailyEntryDao,
    private val personDao: PersonDao
) : EntryRepository {

    override fun observeAll(): Flow<List<DailyEntry>> =
        entryDao.observeAll().map { entities -> entities.map { hydrate(it) } }

    override fun observeByDate(date: LocalDate): Flow<List<DailyEntry>> =
        entryDao.observeByDate(date).map { entities -> entities.map { hydrate(it) } }

    override suspend fun getById(id: String): DailyEntry? =
        entryDao.getById(id)?.let { hydrate(it) }

    override suspend fun getByDate(date: LocalDate): List<DailyEntry> =
        entryDao.getByDate(date).map { hydrate(it) }

    override suspend fun getBetweenDates(start: LocalDate, end: LocalDate): List<DailyEntry> =
        entryDao.getBetweenDates(start, end).map { hydrate(it) }

    override suspend fun getByPersonId(personId: String): List<DailyEntry> =
        entryDao.getByPersonId(personId).map { hydrate(it) }

    override suspend fun getByPlaceId(placeId: String): List<DailyEntry> =
        entryDao.getByPlaceId(placeId).map { hydrate(it) }

    override suspend fun getAll(): List<DailyEntry> =
        entryDao.getAll().map { hydrate(it) }

    override suspend fun save(entry: DailyEntry): DailyEntry {
        persist(entry)
        return entry
    }

    override suspend fun update(entry: DailyEntry) {
        persist(entry)
    }

    override suspend fun delete(id: String) {
        entryDao.deleteById(id)
    }

    private suspend fun persist(entry: DailyEntry) {
        entryDao.insert(DailyEntryEntity.fromDomain(entry))

        entryDao.deleteInteractionsForEntry(entry.id)
        entry.interactions.forEach { interaction ->
            entryDao.insertInteraction(PersonInteractionEntity.fromDomain(interaction))
        }

        entryDao.deleteEntryPlaces(entry.id)
        entry.placesVisited.forEach { place ->
            entryDao.insertEntryPlace(EntryPlaceCrossRef(entry.id, place.id))
        }

        entryDao.deleteEntryWeather(entry.id)
        entry.weatherSnapshots.forEach { snapshot ->
            entryDao.insertEntryWeather(EntryWeatherCrossRef(entry.id, snapshot.id))
        }
    }

    private suspend fun hydrate(entity: DailyEntryEntity): DailyEntry {
        val interactionEntities = entryDao.getInteractionsForEntry(entity.id)
        val interactions = interactionEntities.mapNotNull { intEntity ->
            val person = personDao.getById(intEntity.personId)?.toDomain() ?: return@mapNotNull null
            intEntity.toDomain(person)
        }
        val places = entryDao.getPlacesForEntry(entity.id).map { it.toDomain() }
        val weather = entryDao.getWeatherForEntry(entity.id).map { it.toDomain() }
        return entity.toDomain(interactions, places, weather)
    }
}
