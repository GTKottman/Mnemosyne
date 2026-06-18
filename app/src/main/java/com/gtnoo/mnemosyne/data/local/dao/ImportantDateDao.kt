package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gtnoo.mnemosyne.data.local.entity.ImportantDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportantDateDao {
    @Query("SELECT * FROM person_important_dates WHERE personId = :personId ORDER BY kind ASC, label ASC")
    fun observeByPersonId(personId: String): Flow<List<ImportantDateEntity>>

    @Query("SELECT * FROM person_important_dates WHERE personId = :personId ORDER BY kind ASC, label ASC")
    suspend fun getByPersonId(personId: String): List<ImportantDateEntity>

    @Query("SELECT * FROM person_important_dates WHERE id = :id")
    suspend fun getById(id: String): ImportantDateEntity?

    @Query("SELECT * FROM person_important_dates WHERE personId = :personId AND kind = 'BIRTHDAY' LIMIT 1")
    suspend fun getBirthdayForPerson(personId: String): ImportantDateEntity?

    @Query("SELECT * FROM person_important_dates WHERE notifyEnabled = 1 AND date IS NOT NULL")
    suspend fun getAllWithNotificationsEnabled(): List<ImportantDateEntity>

    @Query("SELECT * FROM person_important_dates WHERE date IS NOT NULL")
    suspend fun getAllWithDates(): List<ImportantDateEntity>

    @Query("SELECT * FROM person_important_dates")
    suspend fun getAll(): List<ImportantDateEntity>

    @Query("SELECT * FROM person_important_dates WHERE date IS NOT NULL")
    fun observeAllWithDates(): Flow<List<ImportantDateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImportantDateEntity)

    @Update
    suspend fun update(entity: ImportantDateEntity)

    @Query("DELETE FROM person_important_dates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM person_important_dates WHERE personId = :personId")
    suspend fun deleteByPersonId(personId: String)
}
