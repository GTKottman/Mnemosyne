package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places ORDER BY label ASC")
    fun observeAll(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places ORDER BY label ASC")
    suspend fun getAll(): List<SavedPlaceEntity>

    @Query("SELECT * FROM saved_places WHERE id = :id")
    suspend fun getById(id: String): SavedPlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedPlaceEntity)

    @Update
    suspend fun update(entity: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteById(id: String)
}
