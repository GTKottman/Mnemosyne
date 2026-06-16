package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DailyEntry(
    val id: String = UUID.randomUUID().toString(),
    val entryDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastEditedAt: LocalDateTime = LocalDateTime.now(),
    val context: EntryContext = EntryContext(),
    val interactions: List<PersonInteraction> = emptyList(),
    val emotionData: EmotionData = EmotionData(),
    val thoughtPatternData: ThoughtPatternData = ThoughtPatternData(),
    val healthContextData: HealthContextData = HealthContextData(),
    val placesVisited: List<SavedPlace> = emptyList(),
    val weatherSnapshots: List<WeatherSnapshot> = emptyList(),
    val freeformNotes: String = "",
    val tags: List<String> = emptyList()
)
