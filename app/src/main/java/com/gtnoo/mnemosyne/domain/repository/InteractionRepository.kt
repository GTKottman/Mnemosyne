package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.PersonInteraction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InteractionRepository {
    fun observeByDate(date: LocalDate): Flow<List<PersonInteraction>>
    suspend fun getByDate(date: LocalDate): List<PersonInteraction>
    suspend fun getByPersonId(personId: String): List<PersonInteraction>
    suspend fun getByPersonIdBetweenDates(
        personId: String,
        start: LocalDate,
        end: LocalDate
    ): List<PersonInteraction>
    suspend fun getById(id: String): PersonInteraction?
    suspend fun save(interaction: PersonInteraction): PersonInteraction
    suspend fun delete(id: String)
}
