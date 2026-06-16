package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate

data class PersonSummary(
    val person: Person,
    val totalInteractions: Int,
    val averageConnectionScore: Double,
    val daysSinceLastContact: Int?,
    val lastContactDate: LocalDate?,
    val connectionTrend: List<Pair<LocalDate, Double>>
)
