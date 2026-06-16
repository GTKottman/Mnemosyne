package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.WeatherSnapshot
import java.time.LocalDate

interface WeatherRepository {
    suspend fun save(snapshot: WeatherSnapshot): WeatherSnapshot
    suspend fun findByPlaceAndDate(placeId: String, date: LocalDate): WeatherSnapshot?
    suspend fun findBetweenDates(placeId: String, start: LocalDate, end: LocalDate): List<WeatherSnapshot>
}
