package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.UpcomingImportantDate
import kotlinx.coroutines.flow.Flow

interface ImportantDateRepository {
    fun observeByPersonId(personId: String): Flow<List<ImportantDate>>
    suspend fun getByPersonId(personId: String): List<ImportantDate>
    suspend fun getById(id: String): ImportantDate?
    suspend fun getBirthdayForPerson(personId: String): ImportantDate?
    suspend fun getAllWithNotificationsEnabled(): List<ImportantDate>
    suspend fun getAll(): List<ImportantDate>
    suspend fun save(date: ImportantDate): ImportantDate
    suspend fun delete(id: String)
    suspend fun deleteAllForPerson(personId: String)
    fun observeUpcoming(withinDays: Int): Flow<List<UpcomingImportantDate>>
}
