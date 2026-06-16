package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gtnoo.mnemosyne.domain.model.PlaceType
import com.gtnoo.mnemosyne.domain.model.SavedPlace

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey val id: String,
    val label: String,
    val placeType: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String,
    val notes: String
) {
    fun toDomain() = SavedPlace(
        id = id,
        label = label,
        placeType = PlaceType.valueOf(placeType),
        city = city,
        state = state,
        country = country,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
        notes = notes
    )

    companion object {
        fun fromDomain(place: SavedPlace) = SavedPlaceEntity(
            id = place.id,
            label = place.label,
            placeType = place.placeType.name,
            city = place.city,
            state = place.state,
            country = place.country,
            latitude = place.latitude,
            longitude = place.longitude,
            timezone = place.timezone,
            notes = place.notes
        )
    }
}
