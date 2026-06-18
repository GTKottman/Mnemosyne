package com.gtnoo.mnemosyne.data.remote.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class OpenMeteoResponse(
    @SerialName("daily") val daily: OpenMeteoDailyData? = null
)

@Serializable
data class OpenMeteoCurrentWeatherResponse(
    @SerialName("current_weather") val currentWeather: OpenMeteoCurrentWeatherData? = null
)

@Serializable
data class OpenMeteoCurrentWeatherData(
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("windspeed") val windspeed: Double? = null,
    @SerialName("weathercode") val weathercode: Int? = null,
    @SerialName("time") val time: String? = null
)

@Serializable
data class OpenMeteoDailyData(
    @SerialName("time") val time: List<String> = emptyList(),
    @SerialName("temperature_2m_max") val tempMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val tempMin: List<Double?> = emptyList(),
    @SerialName("precipitation_sum") val precipSum: List<Double?> = emptyList(),
    @SerialName("windspeed_10m_max") val windMax: List<Double?> = emptyList(),
    @SerialName("weathercode") val weatherCode: List<Int?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    @SerialName("relative_humidity_2m_max") val humidityMax: List<Double?> = emptyList(),
    @SerialName("surface_pressure_max") val pressureMax: List<Double?> = emptyList()
)

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,weathercode,uv_index_max,relative_humidity_2m_max,surface_pressure_max",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoCurrentWeatherResponse
}
