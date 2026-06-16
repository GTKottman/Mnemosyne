package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "entry_places",
    primaryKeys = ["entryId", "placeId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SavedPlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId"), Index("placeId")]
)
data class EntryPlaceCrossRef(
    val entryId: String,
    val placeId: String
)

@Entity(
    tableName = "entry_weather",
    primaryKeys = ["entryId", "weatherSnapshotId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WeatherSnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["weatherSnapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId"), Index("weatherSnapshotId")]
)
data class EntryWeatherCrossRef(
    val entryId: String,
    val weatherSnapshotId: String
)
