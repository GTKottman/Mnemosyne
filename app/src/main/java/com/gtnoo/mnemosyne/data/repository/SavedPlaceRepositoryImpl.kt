package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.SavedPlaceDao
import com.gtnoo.mnemosyne.data.local.entity.SavedPlaceEntity
import com.gtnoo.mnemosyne.domain.model.SavedPlace
import com.gtnoo.mnemosyne.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavedPlaceRepositoryImpl @Inject constructor(
    private val dao: SavedPlaceDao
) : PlaceRepository {

    override fun observeAll(): Flow<List<SavedPlace>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): SavedPlace? =
        dao.getById(id)?.toDomain()

    override suspend fun getAll(): List<SavedPlace> =
        dao.getAll().map { it.toDomain() }

    override suspend fun save(place: SavedPlace): SavedPlace {
        dao.insert(SavedPlaceEntity.fromDomain(place))
        return place
    }

    override suspend fun update(place: SavedPlace) {
        dao.update(SavedPlaceEntity.fromDomain(place))
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
