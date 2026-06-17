package com.gtnoo.mnemosyne.data.remote.geocoding

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class GeocodingResponse(
    @SerialName("results") val results: List<GeocodingResult>? = null
)

@Serializable
private data class GeocodingResult(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double
)

@Singleton
class GeocodingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val TAG = "GeocodingClient"
        private const val BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
    }

    suspend fun geocode(cityName: String): Pair<Double, Double>? {
        if (cityName.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?name=${cityName.trim().replace(" ", "+")}&count=1&language=en&format=json"
                val request = Request.Builder().url(url).build()
                val body = okHttpClient.newCall(request).execute().use { it.body?.string() }
                    ?: return@withContext null
                val response = json.decodeFromString<GeocodingResponse>(body)
                val result = response.results?.firstOrNull() ?: return@withContext null
                Pair(result.latitude, result.longitude)
            } catch (e: Exception) {
                Log.w(TAG, "Geocoding failed for city=$cityName", e)
                null
            }
        }
    }
}
