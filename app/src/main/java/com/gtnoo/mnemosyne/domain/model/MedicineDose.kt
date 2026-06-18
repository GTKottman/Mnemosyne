package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.util.UUID

data class MedicineDose(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String,
    val bottleId: String,
    val date: LocalDate = LocalDate.now(),
    val pillsTaken: Int = 1
)
