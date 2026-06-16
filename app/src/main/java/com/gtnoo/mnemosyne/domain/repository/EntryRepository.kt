package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.DailyEntry
import com.gtnoo.mnemosyne.domain.model.SavedPlace
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EntryRepository {
    fun observeAll(): Flow<List<DailyEntry>>
    fun observeByDate(date: LocalDate): Flow<List<DailyEntry>>
    suspend fun getById(id: String): DailyEntry?
    suspend fun getByDate(date: LocalDate): List<DailyEntry>
    suspend fun getBetweenDates(start: LocalDate, end: LocalDate): List<DailyEntry>
    suspend fun getByPersonId(personId: String): List<DailyEntry>
    suspend fun getByPlaceId(placeId: String): List<DailyEntry>
    suspend fun getAll(): List<DailyEntry>
    suspend fun save(entry: DailyEntry): DailyEntry
    suspend fun update(entry: DailyEntry)
    suspend fun delete(id: String)
}
