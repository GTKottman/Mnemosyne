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

@Serializable
private data class NominatimReverseResponse(
    @SerialName("address") val address: NominatimAddress? = null
)

@Serializable
private data class NominatimAddress(
    @SerialName("city") val city: String? = null,
    @SerialName("town") val town: String? = null,
    @SerialName("village") val village: String? = null,
    @SerialName("municipality") val municipality: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("county") val county: String? = null
)

@Singleton
class GeocodingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val TAG = "GeocodingClient"
        private const val BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        private const val NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse"
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

    /** Returns (city, state) for the given coordinates, or null on failure. */
    suspend fun reverseGeocode(lat: Double, lng: Double): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$NOMINATIM_REVERSE_URL?lat=$lat&lon=$lng&format=json&addressdetails=1"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mnemosyne/1.0")
                    .build()
                val body = okHttpClient.newCall(request).execute().use { it.body?.string() }
                    ?: return@withContext null
                val response = json.decodeFromString<NominatimReverseResponse>(body)
                val address = response.address ?: return@withContext null
                val city = address.city
                    ?: address.town
                    ?: address.village
                    ?: address.municipality
                    ?: address.county
                    ?: return@withContext null
                val state = address.state ?: ""
                Pair(city, state)
            } catch (e: Exception) {
                Log.w(TAG, "Reverse geocoding failed for lat=$lat, lng=$lng", e)
                null
            }
        }
    }
}
