package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.InteractionDao
import com.gtnoo.mnemosyne.data.local.dao.PersonDao
import com.gtnoo.mnemosyne.data.local.entity.PersonInteractionEntity
import com.gtnoo.mnemosyne.domain.model.PersonInteraction
import com.gtnoo.mnemosyne.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class InteractionRepositoryImpl @Inject constructor(
    private val interactionDao: InteractionDao,
    private val personDao: PersonDao
) : InteractionRepository {

    override fun observeByDate(date: LocalDate): Flow<List<PersonInteraction>> =
        interactionDao.observeByDate(date).map { entities -> entities.mapNotNull { resolve(it) } }

    override suspend fun getByDate(date: LocalDate): List<PersonInteraction> =
        interactionDao.getByDate(date).mapNotNull { resolve(it) }

    override suspend fun getByPersonId(personId: String): List<PersonInteraction> =
        interactionDao.getByPersonId(personId).mapNotNull { resolve(it) }

    override suspend fun getByPersonIdBetweenDates(
        personId: String,
        start: LocalDate,
        end: LocalDate
    ): List<PersonInteraction> =
        interactionDao.getByPersonIdBetweenDates(personId, start, end).mapNotNull { resolve(it) }

    override suspend fun getById(id: String): PersonInteraction? =
        interactionDao.getById(id)?.let { resolve(it) }

    override suspend fun save(interaction: PersonInteraction): PersonInteraction {
        interactionDao.insert(PersonInteractionEntity.fromDomain(interaction))
        return interaction
    }

    override suspend fun delete(id: String) {
        interactionDao.deleteById(id)
    }

    private suspend fun resolve(entity: PersonInteractionEntity): PersonInteraction? {
        val person = personDao.getById(entity.personId)?.toDomain() ?: return null
        return entity.toDomain(person)
    }
}
