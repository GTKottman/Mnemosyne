package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class SerializableBackup(
    val entries: List<SerializableEntry>,
    val people: List<SerializablePerson>,
    val places: List<SerializablePlace>,
    val exportedAt: String,
    val appVersion: String
)

@Serializable
data class SerializablePerson(
    val id: String,
    val displayName: String,
    val relationshipType: String,
    val usualPlaceId: String?,
    val isFavorite: Boolean,
    val notes: String,
    val addedOn: String
) {
    fun toDomain() = Person(
        id = id, displayName = displayName,
        relationshipType = RelationshipType.valueOf(relationshipType),
        usualPlaceId = usualPlaceId, isFavorite = isFavorite, notes = notes,
        addedOn = LocalDate.parse(addedOn)
    )
}

@Serializable
data class SerializablePlace(
    val id: String, val label: String, val placeType: String,
    val city: String, val state: String, val country: String,
    val latitude: Double?, val longitude: Double?,
    val timezone: String, val notes: String
) {
    fun toDomain() = SavedPlace(
        id = id, label = label, placeType = PlaceType.valueOf(placeType),
        city = city, state = state, country = country,
        latitude = latitude, longitude = longitude, timezone = timezone, notes = notes
    )
}

@Serializable
data class SerializableEntry(
    val id: String, val entryDate: String, val freeformNotes: String,
    val tags: List<String>, val interactions: List<SerializableInteraction>,
    val placesVisitedIds: List<String>,
    val emotionHopeful: Int, val emotionAnxious: Int, val emotionSad: Int,
    val emotionHappy: Int, val emotionCalm: Int, val emotionLonely: Int,
    val emotionExcited: Int, val emotionConnected: Int, val emotionRegulated: Int
) {
    fun toDomain(people: List<Person>, places: List<SavedPlace>): DailyEntry {
        val peopleMap = people.associateBy { it.id }
        val placesMap = places.associateBy { it.id }
        return DailyEntry(
            id = id,
            entryDate = LocalDate.parse(entryDate),
            freeformNotes = freeformNotes,
            tags = tags,
            emotionData = EmotionData(
                hopeful = emotionHopeful, anxious = emotionAnxious, sad = emotionSad,
                happy = emotionHappy, calm = emotionCalm, lonely = emotionLonely,
                excited = emotionExcited, connected = emotionConnected, regulated = emotionRegulated
            ),
            interactions = interactions.mapNotNull { intDto ->
                val person = peopleMap[intDto.personId] ?: return@mapNotNull null
                intDto.toDomain(person)
            },
            placesVisited = placesVisitedIds.mapNotNull { placesMap[it] }
        )
    }
}

@Serializable
data class SerializableInteraction(
    val id: String, val personId: String, val mode: String, val notes: String
) {
    fun toDomain(person: Person) = PersonInteraction(
        id = id, entryId = "", person = person,
        mode = InteractionMode.valueOf(mode), notes = notes
    )
}

fun AppBackup.toSerializable() = SerializableBackup(
    entries = entries.map { entry ->
        SerializableEntry(
            id = entry.id, entryDate = entry.entryDate.toString(),
            freeformNotes = entry.freeformNotes, tags = entry.tags,
            interactions = entry.interactions.map { i ->
                SerializableInteraction(i.id, i.person.id, i.mode.name, i.notes)
            },
            placesVisitedIds = entry.placesVisited.map { it.id },
            emotionHopeful = entry.emotionData.hopeful,
            emotionAnxious = entry.emotionData.anxious,
            emotionSad = entry.emotionData.sad,
            emotionHappy = entry.emotionData.happy,
            emotionCalm = entry.emotionData.calm,
            emotionLonely = entry.emotionData.lonely,
            emotionExcited = entry.emotionData.excited,
            emotionConnected = entry.emotionData.connected,
            emotionRegulated = entry.emotionData.regulated
        )
    },
    people = people.map { p ->
        SerializablePerson(p.id, p.displayName, p.relationshipType.name,
            p.usualPlaceId, p.isFavorite, p.notes, p.addedOn.toString())
    },
    places = places.map { pl ->
        SerializablePlace(pl.id, pl.label, pl.placeType.name, pl.city, pl.state,
            pl.country, pl.latitude, pl.longitude, pl.timezone, pl.notes)
    },
    exportedAt = exportedAt.toString(),
    appVersion = appVersion
)
