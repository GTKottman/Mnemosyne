package com.gtnoo.mnemosyne.data.repository

import com.gtnoo.mnemosyne.data.local.dao.WeatherSnapshotDao
import com.gtnoo.mnemosyne.data.local.entity.WeatherSnapshotEntity
import com.gtnoo.mnemosyne.domain.model.WeatherSnapshot
import com.gtnoo.mnemosyne.domain.repository.WeatherRepository
import java.time.LocalDate
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val dao: WeatherSnapshotDao
) : WeatherRepository {

    override suspend fun save(snapshot: WeatherSnapshot): WeatherSnapshot {
        dao.insert(WeatherSnapshotEntity.fromDomain(snapshot))
        return snapshot
    }

    override suspend fun findByPlaceAndDate(placeId: String, date: LocalDate): WeatherSnapshot? =
        dao.getByPlaceAndDate(placeId, date)?.toDomain()

    override suspend fun findBetweenDates(
        placeId: String, start: LocalDate, end: LocalDate
    ): List<WeatherSnapshot> =
        dao.getBetweenDates(placeId, start, end).map { it.toDomain() }

    override suspend fun getAll(): List<WeatherSnapshot> =
        dao.getAll().map { it.toDomain() }
}
