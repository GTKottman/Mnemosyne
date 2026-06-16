package com.gtnoo.mnemosyne.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.data.local.dao.*
import com.gtnoo.mnemosyne.data.local.entity.*

@Database(
    entities = [
        SavedPlaceEntity::class,
        PersonEntity::class,
        DailyEntryEntity::class,
        PersonInteractionEntity::class,
        WeatherSnapshotEntity::class,
        EntryPlaceCrossRef::class,
        EntryWeatherCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MnemosyneDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun personDao(): PersonDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun weatherSnapshotDao(): WeatherSnapshotDao
}
