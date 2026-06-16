package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY isFavorite DESC, displayName ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isFavorite = 1 ORDER BY displayName ASC")
    fun observeFavorites(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people ORDER BY isFavorite DESC, displayName ASC")
    suspend fun getAll(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE isFavorite = 1 ORDER BY displayName ASC")
    suspend fun getFavorites(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: String): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PersonEntity)

    @Update
    suspend fun update(entity: PersonEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deleteById(id: String)
}
