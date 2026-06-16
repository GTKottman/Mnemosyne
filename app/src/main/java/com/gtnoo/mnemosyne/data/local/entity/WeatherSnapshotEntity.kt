package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.Season
import com.gtnoo.mnemosyne.domain.model.WeatherCondition
import com.gtnoo.mnemosyne.domain.model.WeatherSnapshot
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "weather_snapshots")
@TypeConverters(Converters::class)
data class WeatherSnapshotEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val placeLabel: String,
    val date: LocalDate,
    val fetchedAt: LocalDateTime,
    val temperatureHigh: Double?,
    val temperatureLow: Double?,
    val temperatureAverage: Double?,
    val humidity: Double?,
    val precipitationMm: Double?,
    val windSpeed: Double?,
    val pressure: Double?,
    val uvIndex: Double?,
    val condition: String,
    val season: String
) {
    fun toDomain() = WeatherSnapshot(
        id = id, placeId = placeId, placeLabel = placeLabel, date = date, fetchedAt = fetchedAt,
        temperatureHigh = temperatureHigh, temperatureLow = temperatureLow,
        temperatureAverage = temperatureAverage, humidity = humidity,
        precipitationMm = precipitationMm, windSpeed = windSpeed,
        pressure = pressure, uvIndex = uvIndex,
        condition = WeatherCondition.valueOf(condition),
        season = Season.valueOf(season)
    )

    companion object {
        fun fromDomain(snapshot: WeatherSnapshot) = WeatherSnapshotEntity(
            id = snapshot.id, placeId = snapshot.placeId, placeLabel = snapshot.placeLabel,
            date = snapshot.date, fetchedAt = snapshot.fetchedAt,
            temperatureHigh = snapshot.temperatureHigh, temperatureLow = snapshot.temperatureLow,
            temperatureAverage = snapshot.temperatureAverage, humidity = snapshot.humidity,
            precipitationMm = snapshot.precipitationMm, windSpeed = snapshot.windSpeed,
            pressure = snapshot.pressure, uvIndex = snapshot.uvIndex,
            condition = snapshot.condition.name, season = snapshot.season.name
        )
    }
}
