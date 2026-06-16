package com.gtnoo.mnemosyne.domain.model

data class EntryContext(
    val entryType: EntryType = EntryType.DAILY_SUMMARY,
    val schoolDay: Boolean = false,
    val workDay: Boolean = false,
    val weekend: Boolean = false,
    val holiday: Boolean = false
)
