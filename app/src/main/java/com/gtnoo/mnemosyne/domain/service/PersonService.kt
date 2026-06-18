package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.InteractionRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class PersonService @Inject constructor(
    private val personRepository: PersonRepository,
    private val interactionRepository: InteractionRepository,
    private val importantDateService: ImportantDateService
) {
    suspend fun addPerson(
        name: String,
        type: RelationshipType,
        usualPlaceId: String? = null,
        notes: String = "",
        birthday: java.time.LocalDate? = null
    ): Person {
        val person = Person(displayName = name, relationshipType = type, usualPlaceId = usualPlaceId, notes = notes)
        val saved = personRepository.save(person)
        val birthdayDate = importantDateService.createDefaultBirthday(saved.id)
        if (birthday != null) {
            importantDateService.saveImportantDate(birthdayDate.copy(date = birthday))
        }
        return saved
    }

    suspend fun updatePerson(person: Person): Person {
        personRepository.update(person)
        return person
    }

    suspend fun deletePerson(id: String) {
        importantDateService.deleteAllForPerson(id)
        personRepository.delete(id)
    }

    suspend fun getAllPeople(): List<Person> = personRepository.getAll()

    suspend fun getFavorites(): List<Person> = personRepository.getFavorites()

    suspend fun getSummaryForPerson(person: Person, range: DateRange): PersonSummary {
        val interactions = interactionRepository.getByPersonIdBetweenDates(
            person.id, range.start, range.end
        )
        val connectionScores = interactions.map {
            it.relationshipSignalData.feltConnectionStable.toDouble()
        }
        val avgConnection = if (connectionScores.isEmpty()) 0.0 else connectionScores.average()
        val lastDate = interactions.maxByOrNull { it.date }?.date
        val daysSince = lastDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() }
        val trend = interactions
            .sortedBy { it.date }
            .map { it.date to it.relationshipSignalData.feltConnectionStable.toDouble() }
        return PersonSummary(
            person = person,
            totalInteractions = interactions.size,
            averageConnectionScore = avgConnection,
            daysSinceLastContact = daysSince,
            lastContactDate = lastDate,
            connectionTrend = trend
        )
    }
}
