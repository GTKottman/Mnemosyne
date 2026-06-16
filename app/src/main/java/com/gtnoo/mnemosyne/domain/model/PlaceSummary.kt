package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate

data class PlaceSummary(
    val place: SavedPlace,
    val totalVisits: Int,
    val averageMood: Double,
    val averageEnergy: Double,
    val lastVisitDate: LocalDate?,
    val moodTrend: List<Pair<LocalDate, Double>>
)
