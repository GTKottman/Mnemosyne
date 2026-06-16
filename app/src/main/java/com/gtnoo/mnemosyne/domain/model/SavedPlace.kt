package com.gtnoo.mnemosyne.domain.model

import java.util.UUID

data class SavedPlace(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val placeType: PlaceType = PlaceType.OTHER,
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String = "America/Chicago",
    val notes: String = ""
)
