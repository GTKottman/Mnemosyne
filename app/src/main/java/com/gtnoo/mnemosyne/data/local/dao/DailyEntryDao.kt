package com.gtnoo.mnemosyne.data.local.dao

import androidx.room.*
import com.gtnoo.mnemosyne.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries ORDER BY entryDate DESC")
    fun observeAll(): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries WHERE entryDate = :date ORDER BY createdAt DESC")
    fun observeByDate(date: LocalDate): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries ORDER BY entryDate DESC")
    suspend fun getAll(): List<DailyEntryEntity>

    @Query("SELECT * FROM daily_entries WHERE id = :id")
    suspend fun getById(id: String): DailyEntryEntity?

    @Query("SELECT * FROM daily_entries WHERE entryDate = :date ORDER BY createdAt DESC")
    suspend fun getByDate(date: LocalDate): List<DailyEntryEntity>

    @Query("SELECT * FROM daily_entries WHERE entryDate BETWEEN :start AND :end ORDER BY entryDate DESC")
    suspend fun getBetweenDates(start: LocalDate, end: LocalDate): List<DailyEntryEntity>

    @Query("""
        SELECT DISTINCT de.* FROM daily_entries de
        INNER JOIN entry_places ep ON ep.entryId = de.id
        WHERE ep.placeId = :placeId
        ORDER BY de.entryDate DESC
    """)
    suspend fun getByPlaceId(placeId: String): List<DailyEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyEntryEntity)

    @Update
    suspend fun update(entity: DailyEntryEntity)

    @Query("DELETE FROM daily_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    // Entry-place cross refs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntryPlace(crossRef: EntryPlaceCrossRef)

    @Query("DELETE FROM entry_places WHERE entryId = :entryId")
    suspend fun deleteEntryPlaces(entryId: String)

    @Query("""
        SELECT sp.* FROM saved_places sp
        INNER JOIN entry_places ep ON ep.placeId = sp.id
        WHERE ep.entryId = :entryId
    """)
    suspend fun getPlacesForEntry(entryId: String): List<SavedPlaceEntity>

    // Entry-weather cross refs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntryWeather(crossRef: EntryWeatherCrossRef)

    @Query("DELETE FROM entry_weather WHERE entryId = :entryId")
    suspend fun deleteEntryWeather(entryId: String)

    @Query("""
        SELECT ws.* FROM weather_snapshots ws
        INNER JOIN entry_weather ew ON ew.weatherSnapshotId = ws.id
        WHERE ew.entryId = :entryId
    """)
    suspend fun getWeatherForEntry(entryId: String): List<WeatherSnapshotEntity>
}
