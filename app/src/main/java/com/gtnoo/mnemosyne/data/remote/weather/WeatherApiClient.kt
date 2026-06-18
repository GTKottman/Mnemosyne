package com.gtnoo.mnemosyne.data.remote.weather

import android.util.Log
import com.gtnoo.mnemosyne.domain.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeatherApiClient @Inject constructor(
    private val api: OpenMeteoApi
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    companion object {
        private const val TAG = "WeatherApiClient"
    }

    suspend fun fetchWeather(place: SavedPlace, date: LocalDate): WeatherSnapshot? {
        val lat = place.latitude ?: return null
        val lng = place.longitude ?: return null
        return try {
            val response = api.getWeather(
                latitude = lat,
                longitude = lng,
                startDate = date.format(dateFormatter),
                endDate = date.format(dateFormatter),
                timezone = place.timezone
            )
            val daily = response.daily ?: return null
            val idx = 0
            val tempHigh = daily.tempMax.getOrNull(idx)
            val tempLow = daily.tempMin.getOrNull(idx)
            val tempAvg = if (tempHigh != null && tempLow != null) (tempHigh + tempLow) / 2.0 else null
            WeatherSnapshot(
                placeId = place.id,
                placeLabel = place.label,
                date = date,
                temperatureHigh = tempHigh,
                temperatureLow = tempLow,
                temperatureAverage = tempAvg,
                precipitationMm = daily.precipSum.getOrNull(idx),
                windSpeed = daily.windMax.getOrNull(idx),
                uvIndex = daily.uvIndexMax.getOrNull(idx),
                humidity = daily.humidityMax.getOrNull(idx),
                pressure = daily.pressureMax.getOrNull(idx),
                condition = mapWeatherCode(daily.weatherCode.getOrNull(idx)),
                season = date.toSeason()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch weather for place=${place.id} date=$date", e)
            null
        }
    }

    suspend fun fetchCurrentWeather(place: SavedPlace): Pair<Double, WeatherCondition>? {
        val lat = place.latitude ?: return null
        val lng = place.longitude ?: return null
        return try {
            val response = api.getCurrentWeather(
                latitude = lat,
                longitude = lng,
                timezone = place.timezone.ifBlank { "auto" }
            )
            val cw = response.currentWeather ?: return null
            val temp = cw.temperature ?: return null
            Pair(temp, mapWeatherCode(cw.weathercode))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch current weather for place=${place.id}", e)
            null
        }
    }

    private fun mapWeatherCode(code: Int?): WeatherCondition = when (code) {
        0 -> WeatherCondition.SUNNY
        in 1..3 -> WeatherCondition.CLOUDY
        in 45..48 -> WeatherCondition.FOGGY
        in 51..67 -> WeatherCondition.RAINY
        in 71..77 -> WeatherCondition.SNOWY
        in 80..82 -> WeatherCondition.RAINY
        in 85..86 -> WeatherCondition.SNOWY
        in 95..99 -> WeatherCondition.STORMY
        else -> WeatherCondition.UNKNOWN
    }
}
