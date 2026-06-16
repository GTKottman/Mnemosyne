package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.SavedPlace
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    fun observeAll(): Flow<List<SavedPlace>>
    suspend fun getById(id: String): SavedPlace?
    suspend fun getAll(): List<SavedPlace>
    suspend fun save(place: SavedPlace): SavedPlace
    suspend fun update(place: SavedPlace)
    suspend fun delete(id: String)
}
