package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate

data class FilterCriteria(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val personId: String? = null,
    val placeId: String? = null,
    val tags: List<String> = emptyList(),
    val maxHappiness: Int? = null,
    val minConnection: Int? = null,
    val onlyDaysWithInteractions: Boolean = false,
    val onlyHighRuminationDays: Boolean = false,
    val onlyWeatherAffectedDays: Boolean = false
)
