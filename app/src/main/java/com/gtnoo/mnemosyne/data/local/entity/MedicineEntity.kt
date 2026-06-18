package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.Medicine
import java.time.LocalTime

@Entity(tableName = "medicines")
@TypeConverters(Converters::class)
data class MedicineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notes: String,
    val isActive: Boolean,
    @ColumnInfo(name = "reminder_time") val reminderTime: LocalTime? = null,
    @ColumnInfo(name = "notify_enabled") val notifyEnabled: Boolean = false
) {
    fun toDomain() = Medicine(
        id = id,
        name = name,
        notes = notes,
        isActive = isActive,
        reminderTime = reminderTime,
        notifyEnabled = notifyEnabled
    )

    companion object {
        fun fromDomain(medicine: Medicine) = MedicineEntity(
            id = medicine.id,
            name = medicine.name,
            notes = medicine.notes,
            isActive = medicine.isActive,
            reminderTime = medicine.reminderTime,
            notifyEnabled = medicine.notifyEnabled
        )
    }
}
