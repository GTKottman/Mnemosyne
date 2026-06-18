package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.MedicineDose
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "medicine_doses",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicineBottleEntity::class,
            parentColumns = ["id"],
            childColumns = ["bottleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId"), Index("date"), Index("bottleId")]
)
@TypeConverters(Converters::class)
data class MedicineDoseEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val bottleId: String,
    val date: LocalDate,
    val pillsTaken: Int,
    @ColumnInfo(name = "taken_at") val takenAt: LocalDateTime? = null
) {
    fun toDomain() = MedicineDose(
        id = id,
        medicineId = medicineId,
        bottleId = bottleId,
        date = date,
        pillsTaken = pillsTaken,
        takenAt = takenAt
    )

    companion object {
        fun fromDomain(dose: MedicineDose) = MedicineDoseEntity(
            id = dose.id,
            medicineId = dose.medicineId,
            bottleId = dose.bottleId,
            date = dose.date,
            pillsTaken = dose.pillsTaken,
            takenAt = dose.takenAt
        )
    }
}
