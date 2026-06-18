package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.util.UUID

data class MedicineBottle(
    val id: String = UUID.randomUUID().toString(),
    val medicineId: String,
    val mgPerPill: Int,
    val pillsTotal: Int,
    val pillsRemaining: Int,
    val openedDate: LocalDate = LocalDate.now(),
    val isCurrentBottle: Boolean = true
)
