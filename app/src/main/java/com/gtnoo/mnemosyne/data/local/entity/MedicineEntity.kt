package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gtnoo.mnemosyne.domain.model.Medicine

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notes: String,
    val isActive: Boolean
) {
    fun toDomain() = Medicine(
        id = id,
        name = name,
        notes = notes,
        isActive = isActive
    )

    companion object {
        fun fromDomain(medicine: Medicine) = MedicineEntity(
            id = medicine.id,
            name = medicine.name,
            notes = medicine.notes,
            isActive = medicine.isActive
        )
    }
}
