package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.MedicineBottle
import java.time.LocalDate

@Entity(
    tableName = "medicine_bottles",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
@TypeConverters(Converters::class)
data class MedicineBottleEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val mgPerPill: Int,
    val pillsTotal: Int,
    val pillsRemaining: Int,
    val openedDate: LocalDate,
    val isCurrentBottle: Boolean
) {
    fun toDomain() = MedicineBottle(
        id = id,
        medicineId = medicineId,
        mgPerPill = mgPerPill,
        pillsTotal = pillsTotal,
        pillsRemaining = pillsRemaining,
        openedDate = openedDate,
        isCurrentBottle = isCurrentBottle
    )

    companion object {
        fun fromDomain(bottle: MedicineBottle) = MedicineBottleEntity(
            id = bottle.id,
            medicineId = bottle.medicineId,
            mgPerPill = bottle.mgPerPill,
            pillsTotal = bottle.pillsTotal,
            pillsRemaining = bottle.pillsRemaining,
            openedDate = bottle.openedDate,
            isCurrentBottle = bottle.isCurrentBottle
        )
    }
}
