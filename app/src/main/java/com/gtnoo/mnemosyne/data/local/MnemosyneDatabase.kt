package com.gtnoo.mnemosyne.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MnemosyneDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun personDao(): PersonDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun weatherSnapshotDao(): WeatherSnapshotDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE daily_entries ADD COLUMN contextVacation INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
