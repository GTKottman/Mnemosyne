package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Serializable
data class SerializableBackup(
    val entries: List<SerializableEntry>,
    val people: List<SerializablePerson>,
    val places: List<SerializablePlace>,
    val importantDates: List<SerializableImportantDate> = emptyList(),
    val medicines: List<SerializableMedicine> = emptyList(),
    val medicineBottles: List<SerializableMedicineBottle> = emptyList(),
    val medicineDoses: List<SerializableMedicineDose> = emptyList(),
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
    val emotionHappiness: Int, val emotionSafety: Int, val emotionCalm: Int,
    val emotionConnection: Int, val emotionClarity: Int, val emotionCapacity: Int,
    val emotionHope: Int, val emotionWorthiness: Int, val emotionPeace: Int,
    val emotionEnergy: Int, val emotionAgency: Int, val emotionPresence: Int
) {
    fun toDomain(people: List<Person>, places: List<SavedPlace>): DailyEntry {
        val placesMap = places.associateBy { it.id }
        return DailyEntry(
            id = id,
            entryDate = LocalDate.parse(entryDate),
            freeformNotes = freeformNotes,
            tags = tags,
            emotionData = EmotionData(
                happiness = emotionHappiness, safety = emotionSafety, calm = emotionCalm,
                connection = emotionConnection, clarity = emotionClarity, capacity = emotionCapacity,
                hope = emotionHope, worthiness = emotionWorthiness, peace = emotionPeace,
                energy = emotionEnergy, agency = emotionAgency, presence = emotionPresence
            ),
            interactions = emptyList(),
            placesVisited = placesVisitedIds.mapNotNull { placesMap[it] }
        )
    }
}

@Serializable
data class SerializableImportantDate(
    val id: String,
    val personId: String,
    val label: String,
    val date: String?,
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
        date = date?.let { LocalDate.parse(it) },
        kind = ImportantDateKind.valueOf(kind),
        isRecurring = isRecurring,
        notifyEnabled = notifyEnabled,
        notifyDaysBefore = notifyDaysBefore,
        remindToLogInteraction = remindToLogInteraction
    )
}

@Serializable
data class SerializableInteraction(
    val id: String, val personId: String, val mode: String, val notes: String
) {
    fun toDomain(person: Person, date: LocalDate) = PersonInteraction(
        id = id, date = date, person = person,
        mode = InteractionMode.valueOf(mode), notes = notes
    )
}

@Serializable
data class SerializableMedicine(
    val id: String,
    val name: String,
    val notes: String,
    val isActive: Boolean,
    val reminderTime: String? = null,
    val notifyEnabled: Boolean = false
) {
    fun toDomain() = Medicine(
        id = id,
        name = name,
        notes = notes,
        isActive = isActive,
        reminderTime = reminderTime?.let { LocalTime.parse(it) },
        notifyEnabled = notifyEnabled
    )
}

@Serializable
data class SerializableMedicineBottle(
    val id: String,
    val medicineId: String,
    val mgPerPill: Int,
    val pillsTotal: Int,
    val pillsRemaining: Int,
    val openedDate: String,
    val isCurrentBottle: Boolean
) {
    fun toDomain() = MedicineBottle(
        id = id,
        medicineId = medicineId,
        mgPerPill = mgPerPill,
        pillsTotal = pillsTotal,
        pillsRemaining = pillsRemaining,
        openedDate = LocalDate.parse(openedDate),
        isCurrentBottle = isCurrentBottle
    )
}

@Serializable
data class SerializableMedicineDose(
    val id: String,
    val medicineId: String,
    val bottleId: String,
    val date: String,
    val pillsTaken: Int,
    val takenAt: String? = null
) {
    fun toDomain() = MedicineDose(
        id = id,
        medicineId = medicineId,
        bottleId = bottleId,
        date = LocalDate.parse(date),
        pillsTaken = pillsTaken,
        takenAt = takenAt?.let { LocalDateTime.parse(it) }
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
            emotionHappiness = entry.emotionData.happiness,
            emotionSafety = entry.emotionData.safety,
            emotionCalm = entry.emotionData.calm,
            emotionConnection = entry.emotionData.connection,
            emotionClarity = entry.emotionData.clarity,
            emotionCapacity = entry.emotionData.capacity,
            emotionHope = entry.emotionData.hope,
            emotionWorthiness = entry.emotionData.worthiness,
            emotionPeace = entry.emotionData.peace,
            emotionEnergy = entry.emotionData.energy,
            emotionAgency = entry.emotionData.agency,
            emotionPresence = entry.emotionData.presence
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
    importantDates = importantDates.map { d ->
        SerializableImportantDate(
            id = d.id,
            personId = d.personId,
            label = d.label,
            date = d.date?.toString(),
            kind = d.kind.name,
            isRecurring = d.isRecurring,
            notifyEnabled = d.notifyEnabled,
            notifyDaysBefore = d.notifyDaysBefore,
            remindToLogInteraction = d.remindToLogInteraction
        )
    },
    medicines = medicines.map { m ->
        SerializableMedicine(
            id = m.id,
            name = m.name,
            notes = m.notes,
            isActive = m.isActive,
            reminderTime = m.reminderTime?.toString(),
            notifyEnabled = m.notifyEnabled
        )
    },
    medicineBottles = medicineBottles.map { b ->
        SerializableMedicineBottle(b.id, b.medicineId, b.mgPerPill, b.pillsTotal, b.pillsRemaining, b.openedDate.toString(), b.isCurrentBottle)
    },
    medicineDoses = medicineDoses.map { d ->
        SerializableMedicineDose(
            id = d.id,
            medicineId = d.medicineId,
            bottleId = d.bottleId,
            date = d.date.toString(),
            pillsTaken = d.pillsTaken,
            takenAt = d.takenAt?.toString()
        )
    },
    exportedAt = exportedAt.toString(),
    appVersion = appVersion
)
