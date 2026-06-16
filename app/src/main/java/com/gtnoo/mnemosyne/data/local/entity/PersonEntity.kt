package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.Person
import com.gtnoo.mnemosyne.domain.model.RelationshipType
import java.time.LocalDate

@Entity(tableName = "people")
@TypeConverters(Converters::class)
data class PersonEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val relationshipType: String,
    val usualPlaceId: String?,
    val isFavorite: Boolean,
    val notes: String,
    val addedOn: LocalDate
) {
    fun toDomain() = Person(
        id = id,
        displayName = displayName,
        relationshipType = RelationshipType.valueOf(relationshipType),
        usualPlaceId = usualPlaceId,
        isFavorite = isFavorite,
        notes = notes,
        addedOn = addedOn
    )

    companion object {
        fun fromDomain(person: Person) = PersonEntity(
            id = person.id,
            displayName = person.displayName,
            relationshipType = person.relationshipType.name,
            usualPlaceId = person.usualPlaceId,
            isFavorite = person.isFavorite,
            notes = person.notes,
            addedOn = person.addedOn
        )
    }
}
