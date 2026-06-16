package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.util.UUID

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val relationshipType: RelationshipType = RelationshipType.OTHER,
    val usualPlaceId: String? = null,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val addedOn: LocalDate = LocalDate.now()
)
