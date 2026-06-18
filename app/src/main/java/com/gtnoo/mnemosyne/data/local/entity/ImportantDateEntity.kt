package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.ImportantDateKind
import java.time.LocalDate

@Entity(
    tableName = "person_important_dates",
    indices = [Index(value = ["personId"])]
)
@TypeConverters(Converters::class)
data class ImportantDateEntity(
    @PrimaryKey val id: String,
    val personId: String,
    val label: String,
    val date: LocalDate?,
    val kind: String,
    val isRecurring: Boolean,
    val notifyEnabled: Boolean,
    val notifyDaysBefore: Int,
    val remindToLogInteraction: Boolean
) {
    fun toDomain() = ImportantDate(
        id = id,
        personId = personId,
        label = label,
        date = date,
        kind = ImportantDateKind.valueOf(kind),
        isRecurring = isRecurring,
        notifyEnabled = notifyEnabled,
        notifyDaysBefore = notifyDaysBefore,
        remindToLogInteraction = remindToLogInteraction
    )

    companion object {
        fun fromDomain(date: ImportantDate) = ImportantDateEntity(
            id = date.id,
            personId = date.personId,
            label = date.label,
            date = date.date,
            kind = date.kind.name,
            isRecurring = date.isRecurring,
            notifyEnabled = date.notifyEnabled,
            notifyDaysBefore = date.notifyDaysBefore,
            remindToLogInteraction = date.remindToLogInteraction
        )
    }
}
