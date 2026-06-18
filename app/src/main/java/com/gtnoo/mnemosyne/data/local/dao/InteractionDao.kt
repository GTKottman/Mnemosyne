package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.PersonInteractionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InteractionDao {

    @Query("SELECT * FROM person_interactions WHERE date = :date ORDER BY rowid ASC")
    fun observeByDate(date: LocalDate): Flow<List<PersonInteractionEntity>>

    @Query("SELECT * FROM person_interactions WHERE date = :date ORDER BY rowid ASC")
    suspend fun getByDate(date: LocalDate): List<PersonInteractionEntity>

    @Query("SELECT * FROM person_interactions WHERE personId = :personId ORDER BY date DESC")
    suspend fun getByPersonId(personId: String): List<PersonInteractionEntity>

    @Query("""
        SELECT * FROM person_interactions
        WHERE personId = :personId AND date BETWEEN :start AND :end
        ORDER BY date DESC
    """)
    suspend fun getByPersonIdBetweenDates(
        personId: String,
        start: LocalDate,
        end: LocalDate
    ): List<PersonInteractionEntity>

    @Query("SELECT * FROM person_interactions WHERE id = :id")
    suspend fun getById(id: String): PersonInteractionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PersonInteractionEntity)

    @Query("DELETE FROM person_interactions WHERE id = :id")
    suspend fun deleteById(id: String)
}
