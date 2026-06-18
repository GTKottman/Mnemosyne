package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.ImportantDateDao
import com.gtnoo.mnemosyne.data.local.dao.PersonDao
import com.gtnoo.mnemosyne.data.local.entity.ImportantDateEntity
import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.UpcomingImportantDate
import com.gtnoo.mnemosyne.domain.repository.ImportantDateRepository
import com.gtnoo.mnemosyne.domain.util.ImportantDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ImportantDateRepositoryImpl @Inject constructor(
    private val dao: ImportantDateDao,
    private val personDao: PersonDao
) : ImportantDateRepository {

    override fun observeByPersonId(personId: String): Flow<List<ImportantDate>> =
        dao.observeByPersonId(personId).map { list -> list.map { it.toDomain() } }

    override suspend fun getByPersonId(personId: String): List<ImportantDate> =
        dao.getByPersonId(personId).map { it.toDomain() }

    override suspend fun getById(id: String): ImportantDate? =
        dao.getById(id)?.toDomain()

    override suspend fun getBirthdayForPerson(personId: String): ImportantDate? =
        dao.getBirthdayForPerson(personId)?.toDomain()

    override suspend fun getAllWithNotificationsEnabled(): List<ImportantDate> =
        dao.getAllWithNotificationsEnabled().map { it.toDomain() }

    override suspend fun getAll(): List<ImportantDate> =
        dao.getAll().map { it.toDomain() }

    override suspend fun save(date: ImportantDate): ImportantDate {
        dao.insert(ImportantDateEntity.fromDomain(date))
        return date
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteAllForPerson(personId: String) {
        dao.deleteByPersonId(personId)
    }

    override fun observeUpcoming(withinDays: Int): Flow<List<UpcomingImportantDate>> =
        combine(
            personDao.observeAll(),
            dao.observeAllWithDates()
        ) { people, dateEntities ->
            val dates = dateEntities.map { it.toDomain() }
            val persons = people.map { it.toDomain() }
            ImportantDateUtils.buildUpcoming(dates, persons, withinDays)
        }
}
