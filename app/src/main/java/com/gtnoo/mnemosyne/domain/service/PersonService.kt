package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class PersonService @Inject constructor(
    private val personRepository: PersonRepository,
    private val entryRepository: EntryRepository
) {
    suspend fun addPerson(
        name: String,
        type: RelationshipType,
        usualPlaceId: String? = null,
        notes: String = ""
    ): Person {
        val person = Person(displayName = name, relationshipType = type, usualPlaceId = usualPlaceId, notes = notes)
        return personRepository.save(person)
    }

    suspend fun updatePerson(person: Person): Person {
        personRepository.update(person)
        return person
    }

    suspend fun deletePerson(id: String) = personRepository.delete(id)

    suspend fun getAllPeople(): List<Person> = personRepository.getAll()

    suspend fun getFavorites(): List<Person> = personRepository.getFavorites()

    suspend fun getSummaryForPerson(person: Person, range: DateRange): PersonSummary {
        val entries = entryRepository.getByPersonId(person.id).filter {
            !it.entryDate.isBefore(range.start) && !it.entryDate.isAfter(range.end)
        }
        val connectionScores = entries.mapNotNull { entry ->
            entry.interactions
                .firstOrNull { it.person.id == person.id }
                ?.relationshipSignalData
                ?.feltConnectionStable
                ?.toDouble()
        }
        val avgConnection = if (connectionScores.isEmpty()) 0.0 else connectionScores.average()
        val lastDate = entries.maxByOrNull { it.entryDate }?.entryDate
        val daysSince = lastDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() }
        val trend = entries
            .sortedBy { it.entryDate }
            .mapNotNull { entry ->
                val score = entry.interactions
                    .firstOrNull { it.person.id == person.id }
                    ?.relationshipSignalData
                    ?.feltConnectionStable
                    ?.toDouble() ?: return@mapNotNull null
                entry.entryDate to score
            }
        return PersonSummary(
            person = person,
            totalInteractions = entries.size,
            averageConnectionScore = avgConnection,
            daysSinceLastContact = daysSince,
            lastContactDate = lastDate,
            connectionTrend = trend
        )
    }
}
