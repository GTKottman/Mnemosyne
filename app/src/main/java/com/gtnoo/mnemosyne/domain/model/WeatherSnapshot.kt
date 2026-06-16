package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class WeatherSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val placeId: String,
    val placeLabel: String = "",
    val date: LocalDate,
    val fetchedAt: LocalDateTime = LocalDateTime.now(),
    val temperatureHigh: Double? = null,
    val temperatureLow: Double? = null,
    val temperatureAverage: Double? = null,
    val humidity: Double? = null,
    val precipitationMm: Double? = null,
    val windSpeed: Double? = null,
    val pressure: Double? = null,
    val uvIndex: Double? = null,
    val condition: WeatherCondition = WeatherCondition.UNKNOWN,
    val season: Season = Season.SUMMER
)

fun LocalDate.toSeason(): Season {
    return when (monthValue) {
        in 3..5 -> Season.SPRING
        in 6..8 -> Season.SUMMER
        in 9..11 -> Season.FALL
        else -> Season.WINTER
    }
}
