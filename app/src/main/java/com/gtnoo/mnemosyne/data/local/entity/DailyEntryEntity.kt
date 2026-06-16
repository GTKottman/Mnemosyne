package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "daily_entries")
@TypeConverters(Converters::class)
data class DailyEntryEntity(
    @PrimaryKey val id: String,
    val entryDate: LocalDate,
    val createdAt: LocalDateTime,
    val lastEditedAt: LocalDateTime,
    val freeformNotes: String,
    val tags: List<String>,

    // EntryContext
    val contextEntryType: String,
    val contextSchoolDay: Boolean,
    val contextWorkDay: Boolean,
    val contextWeekend: Boolean,
    val contextHoliday: Boolean,

    // EmotionData
    val emotionHopeful: Int,
    val emotionAnxious: Int,
    val emotionSad: Int,
    val emotionHappy: Int,
    val emotionCalm: Int,
    val emotionLonely: Int,
    val emotionExcited: Int,
    val emotionConfused: Int,
    val emotionSecure: Int,
    val emotionJealous: Int,
    val emotionRejected: Int,
    val emotionConnected: Int,
    val emotionOverwhelmed: Int,
    val emotionRegulated: Int,

    // ThoughtPatternData
    val thoughtRuminationLevel: Int,
    val thoughtCheckedPhone: Boolean,
    val thoughtRereadMessages: Boolean,
    val thoughtImaginedNegative: Boolean,
    val thoughtImaginedPositive: Boolean,
    val thoughtNeedReassurance: Int,
    val thoughtClarityLevel: Int,
    val thoughtUncertaintyLevel: Int,
    val thoughtWantedToPullAway: Int,
    val thoughtWantedToReachOut: Int,

    // HealthContextData
    val healthHoursSlept: Double,
    val healthAteEnough: Boolean,
    val healthCaffeineIntakeMg: Int,
    val healthSchoolStress: Int,
    val healthWorkStress: Int,
    val healthSensoryOverload: Int,
    val healthEnergyLevel: Int,
    val healthBodyDiscomfort: Int,
    val healthExecutiveFunction: Int,
    val healthSocialBattery: Int
) {
    fun toDomain(
        interactions: List<PersonInteraction>,
        placesVisited: List<SavedPlace>,
        weatherSnapshots: List<WeatherSnapshot>
    ) = DailyEntry(
        id = id,
        entryDate = entryDate,
        createdAt = createdAt,
        lastEditedAt = lastEditedAt,
        freeformNotes = freeformNotes,
        tags = tags,
        context = EntryContext(
            entryType = EntryType.valueOf(contextEntryType),
            schoolDay = contextSchoolDay,
            workDay = contextWorkDay,
            weekend = contextWeekend,
            holiday = contextHoliday
        ),
        emotionData = EmotionData(
            hopeful = emotionHopeful, anxious = emotionAnxious, sad = emotionSad,
            happy = emotionHappy, calm = emotionCalm, lonely = emotionLonely,
            excited = emotionExcited, confused = emotionConfused, secure = emotionSecure,
            jealous = emotionJealous, rejected = emotionRejected, connected = emotionConnected,
            overwhelmed = emotionOverwhelmed, regulated = emotionRegulated
        ),
        thoughtPatternData = ThoughtPatternData(
            ruminationLevel = thoughtRuminationLevel, checkedPhoneRepeatedly = thoughtCheckedPhone,
            rereadMessages = thoughtRereadMessages, imaginedNegativeOutcome = thoughtImaginedNegative,
            imaginedPositiveOutcome = thoughtImaginedPositive, needForReassurance = thoughtNeedReassurance,
            clarityLevel = thoughtClarityLevel, uncertaintyLevel = thoughtUncertaintyLevel,
            wantedToPullAway = thoughtWantedToPullAway, wantedToReachOut = thoughtWantedToReachOut
        ),
        healthContextData = HealthContextData(
            hoursSlept = healthHoursSlept, ateEnough = healthAteEnough,
            caffeineIntakeMg = healthCaffeineIntakeMg, schoolStress = healthSchoolStress,
            workStress = healthWorkStress, sensoryOverload = healthSensoryOverload,
            energyLevel = healthEnergyLevel, bodyDiscomfort = healthBodyDiscomfort,
            executiveFunction = healthExecutiveFunction, socialBattery = healthSocialBattery
        ),
        interactions = interactions,
        placesVisited = placesVisited,
        weatherSnapshots = weatherSnapshots
    )

    companion object {
        fun fromDomain(entry: DailyEntry) = DailyEntryEntity(
            id = entry.id,
            entryDate = entry.entryDate,
            createdAt = entry.createdAt,
            lastEditedAt = entry.lastEditedAt,
            freeformNotes = entry.freeformNotes,
            tags = entry.tags,
            contextEntryType = entry.context.entryType.name,
            contextSchoolDay = entry.context.schoolDay,
            contextWorkDay = entry.context.workDay,
            contextWeekend = entry.context.weekend,
            contextHoliday = entry.context.holiday,
            emotionHopeful = entry.emotionData.hopeful, emotionAnxious = entry.emotionData.anxious,
            emotionSad = entry.emotionData.sad, emotionHappy = entry.emotionData.happy,
            emotionCalm = entry.emotionData.calm, emotionLonely = entry.emotionData.lonely,
            emotionExcited = entry.emotionData.excited, emotionConfused = entry.emotionData.confused,
            emotionSecure = entry.emotionData.secure, emotionJealous = entry.emotionData.jealous,
            emotionRejected = entry.emotionData.rejected, emotionConnected = entry.emotionData.connected,
            emotionOverwhelmed = entry.emotionData.overwhelmed, emotionRegulated = entry.emotionData.regulated,
            thoughtRuminationLevel = entry.thoughtPatternData.ruminationLevel,
            thoughtCheckedPhone = entry.thoughtPatternData.checkedPhoneRepeatedly,
            thoughtRereadMessages = entry.thoughtPatternData.rereadMessages,
            thoughtImaginedNegative = entry.thoughtPatternData.imaginedNegativeOutcome,
            thoughtImaginedPositive = entry.thoughtPatternData.imaginedPositiveOutcome,
            thoughtNeedReassurance = entry.thoughtPatternData.needForReassurance,
            thoughtClarityLevel = entry.thoughtPatternData.clarityLevel,
            thoughtUncertaintyLevel = entry.thoughtPatternData.uncertaintyLevel,
            thoughtWantedToPullAway = entry.thoughtPatternData.wantedToPullAway,
            thoughtWantedToReachOut = entry.thoughtPatternData.wantedToReachOut,
            healthHoursSlept = entry.healthContextData.hoursSlept,
            healthAteEnough = entry.healthContextData.ateEnough,
            healthCaffeineIntakeMg = entry.healthContextData.caffeineIntakeMg,
            healthSchoolStress = entry.healthContextData.schoolStress,
            healthWorkStress = entry.healthContextData.workStress,
            healthSensoryOverload = entry.healthContextData.sensoryOverload,
            healthEnergyLevel = entry.healthContextData.energyLevel,
            healthBodyDiscomfort = entry.healthContextData.bodyDiscomfort,
            healthExecutiveFunction = entry.healthContextData.executiveFunction,
            healthSocialBattery = entry.healthContextData.socialBattery
        )
    }
}
