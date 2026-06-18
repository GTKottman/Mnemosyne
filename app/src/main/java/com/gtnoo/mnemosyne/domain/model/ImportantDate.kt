package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.util.UUID

enum class ImportantDateKind { BIRTHDAY, CUSTOM }

data class ImportantDate(
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val label: String,
    val date: LocalDate? = null,
    val kind: ImportantDateKind = ImportantDateKind.CUSTOM,
    val isRecurring: Boolean = false,
    val notifyEnabled: Boolean = false,
    val notifyDaysBefore: Int = 0,
    val remindToLogInteraction: Boolean = false
)

data class UpcomingImportantDate(
    val importantDate: ImportantDate,
    val person: Person,
    val nextOccurrence: LocalDate,
    val daysUntil: Int
)
