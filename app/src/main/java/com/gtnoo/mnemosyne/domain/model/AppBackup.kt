package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDateTime

data class AppBackup(
    val entries: List<DailyEntry>,
    val people: List<Person>,
    val places: List<SavedPlace>,
    val weatherSnapshots: List<WeatherSnapshot>,
    val settings: AppSettings,
    val importantDates: List<ImportantDate> = emptyList(),
    val medicines: List<Medicine> = emptyList(),
    val medicineBottles: List<MedicineBottle> = emptyList(),
    val medicineDoses: List<MedicineDose> = emptyList(),
    val exportedAt: LocalDateTime = LocalDateTime.now(),
    val appVersion: String = "0.1.0"
)

data class ImportResult(
    val success: Boolean,
    val entriesImported: Int = 0,
    val entriesSkipped: Int = 0,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)
