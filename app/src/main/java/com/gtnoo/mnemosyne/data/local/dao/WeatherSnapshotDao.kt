package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.WeatherSnapshotEntity
import java.time.LocalDate

@Dao
interface WeatherSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeatherSnapshotEntity)

    @Query("SELECT * FROM weather_snapshots WHERE placeId = :placeId AND date = :date LIMIT 1")
    suspend fun getByPlaceAndDate(placeId: String, date: LocalDate): WeatherSnapshotEntity?

    @Query("SELECT * FROM weather_snapshots WHERE placeId = :placeId AND date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getBetweenDates(placeId: String, start: LocalDate, end: LocalDate): List<WeatherSnapshotEntity>
}
