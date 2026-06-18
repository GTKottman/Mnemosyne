package com.gtnoo.mnemosyne.domain.model

import java.time.LocalTime
import java.util.UUID

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    val isActive: Boolean = true,
    val reminderTime: LocalTime? = null,
    val notifyEnabled: Boolean = false
)
