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
    val contextVacation: Boolean,

    // EmotionData (bipolar axes, 0 = distress pole, 5 = neutral, 10 = supportive pole)
    val emotionHappiness: Int,
    val emotionSafety: Int,
    val emotionCalm: Int,
    val emotionConnection: Int,
    val emotionClarity: Int,
    val emotionCapacity: Int,
    val emotionHope: Int,
    val emotionWorthiness: Int,
    val emotionPeace: Int,
    val emotionEnergy: Int,
    val emotionAgency: Int,
    val emotionPresence: Int,

    // ThoughtPatternData
    val thoughtRuminationLevel: Int,
    val thoughtIntrusiveThoughts: Int,
    val thoughtWorryAnticipation: Int,
    val thoughtCatastrophizing: Int,
    val thoughtReassuranceUrge: Int,
    val thoughtMentalClarity: Int,
    val thoughtUncertaintyTolerance: Int,
    val thoughtDecisionFriction: Int,
    val thoughtCognitiveFlexibility: Int,
    val thoughtSelfTalkTone: Int,

    // HealthContextData -- Core State
    val healthEnergyLevel: Int,
    val healthSocialBattery: Int,
    val healthExecutiveFunction: Int,
    val healthMentalBandwidth: Int,
    val healthBodyDiscomfort: Int,
    val healthSensoryEnvironmentalStrain: Int,
    val healthSleepQuality: Int,
    val healthStressLoad: Int,

    // HealthContextData -- Stress Categories
    val healthWorkSchoolPressure: Int,
    val healthMoneyPressure: Int,
    val healthRelationshipPressure: Int,
    val healthFamilyPressure: Int,
    val healthHealthPressure: Int,
    val healthTimePressure: Int,

    // HealthContextData -- Inputs / Intake
    val healthHoursSlept: Double,
    val healthCaffeineIntakeMg: Int,
    val healthFoodAdequacy: Int,
    val healthHydration: Int,
    val healthMovementLevel: Int,

    // HealthContextData -- Environment / Day Shape
    val healthTimeOutsideSunlight: Int,
    val healthScreenLoad: Int,
    val healthSocialExposure: Int,
    val healthNoveltyDisruption: Int,
    val healthPhysicalSpaceQuality: Int,
    val healthWeatherImpact: Int
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
            holiday = contextHoliday,
            vacation = contextVacation
        ),
        emotionData = EmotionData(
            happiness = emotionHappiness, safety = emotionSafety, calm = emotionCalm,
            connection = emotionConnection, clarity = emotionClarity, capacity = emotionCapacity,
            hope = emotionHope, worthiness = emotionWorthiness, peace = emotionPeace,
            energy = emotionEnergy, agency = emotionAgency, presence = emotionPresence
        ),
        thoughtPatternData = ThoughtPatternData(
            ruminationLevel = thoughtRuminationLevel,
            intrusiveThoughts = thoughtIntrusiveThoughts,
            worryAnticipation = thoughtWorryAnticipation,
            catastrophizing = thoughtCatastrophizing,
            reassuranceUrge = thoughtReassuranceUrge,
            mentalClarity = thoughtMentalClarity,
            uncertaintyTolerance = thoughtUncertaintyTolerance,
            decisionFriction = thoughtDecisionFriction,
            cognitiveFlexibility = thoughtCognitiveFlexibility,
            selfTalkTone = thoughtSelfTalkTone
        ),
        healthContextData = HealthContextData(
            energyLevel = healthEnergyLevel,
            socialBattery = healthSocialBattery,
            executiveFunction = healthExecutiveFunction,
            mentalBandwidth = healthMentalBandwidth,
            bodyDiscomfort = healthBodyDiscomfort,
            sensoryEnvironmentalStrain = healthSensoryEnvironmentalStrain,
            sleepQuality = healthSleepQuality,
            stressLoad = healthStressLoad,
            workSchoolPressure = healthWorkSchoolPressure,
            moneyPressure = healthMoneyPressure,
            relationshipPressure = healthRelationshipPressure,
            familyPressure = healthFamilyPressure,
            healthPressure = healthHealthPressure,
            timePressure = healthTimePressure
        ),
        intakeData = IntakeData(
            hoursSlept = healthHoursSlept,
            caffeineIntakeMg = healthCaffeineIntakeMg,
            foodAdequacy = healthFoodAdequacy,
            hydration = healthHydration,
            movementLevel = healthMovementLevel
        ),
        environmentData = EnvironmentData(
            timeOutsideSunlight = healthTimeOutsideSunlight,
            screenLoad = healthScreenLoad,
            socialExposure = healthSocialExposure,
            noveltyDisruption = healthNoveltyDisruption,
            physicalSpaceQuality = healthPhysicalSpaceQuality,
            weatherImpact = healthWeatherImpact
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
            contextVacation = entry.context.vacation,
            emotionHappiness = entry.emotionData.happiness, emotionSafety = entry.emotionData.safety,
            emotionCalm = entry.emotionData.calm, emotionConnection = entry.emotionData.connection,
            emotionClarity = entry.emotionData.clarity, emotionCapacity = entry.emotionData.capacity,
            emotionHope = entry.emotionData.hope, emotionWorthiness = entry.emotionData.worthiness,
            emotionPeace = entry.emotionData.peace, emotionEnergy = entry.emotionData.energy,
            emotionAgency = entry.emotionData.agency, emotionPresence = entry.emotionData.presence,
            thoughtRuminationLevel = entry.thoughtPatternData.ruminationLevel,
            thoughtIntrusiveThoughts = entry.thoughtPatternData.intrusiveThoughts,
            thoughtWorryAnticipation = entry.thoughtPatternData.worryAnticipation,
            thoughtCatastrophizing = entry.thoughtPatternData.catastrophizing,
            thoughtReassuranceUrge = entry.thoughtPatternData.reassuranceUrge,
            thoughtMentalClarity = entry.thoughtPatternData.mentalClarity,
            thoughtUncertaintyTolerance = entry.thoughtPatternData.uncertaintyTolerance,
            thoughtDecisionFriction = entry.thoughtPatternData.decisionFriction,
            thoughtCognitiveFlexibility = entry.thoughtPatternData.cognitiveFlexibility,
            thoughtSelfTalkTone = entry.thoughtPatternData.selfTalkTone,
            healthEnergyLevel = entry.healthContextData.energyLevel,
            healthSocialBattery = entry.healthContextData.socialBattery,
            healthExecutiveFunction = entry.healthContextData.executiveFunction,
            healthMentalBandwidth = entry.healthContextData.mentalBandwidth,
            healthBodyDiscomfort = entry.healthContextData.bodyDiscomfort,
            healthSensoryEnvironmentalStrain = entry.healthContextData.sensoryEnvironmentalStrain,
            healthSleepQuality = entry.healthContextData.sleepQuality,
            healthStressLoad = entry.healthContextData.stressLoad,
            healthWorkSchoolPressure = entry.healthContextData.workSchoolPressure,
            healthMoneyPressure = entry.healthContextData.moneyPressure,
            healthRelationshipPressure = entry.healthContextData.relationshipPressure,
            healthFamilyPressure = entry.healthContextData.familyPressure,
            healthHealthPressure = entry.healthContextData.healthPressure,
            healthTimePressure = entry.healthContextData.timePressure,
            healthHoursSlept = entry.intakeData.hoursSlept,
            healthCaffeineIntakeMg = entry.intakeData.caffeineIntakeMg,
            healthFoodAdequacy = entry.intakeData.foodAdequacy,
            healthHydration = entry.intakeData.hydration,
            healthMovementLevel = entry.intakeData.movementLevel,
            healthTimeOutsideSunlight = entry.environmentData.timeOutsideSunlight,
            healthScreenLoad = entry.environmentData.screenLoad,
            healthSocialExposure = entry.environmentData.socialExposure,
            healthNoveltyDisruption = entry.environmentData.noveltyDisruption,
            healthPhysicalSpaceQuality = entry.environmentData.physicalSpaceQuality,
            healthWeatherImpact = entry.environmentData.weatherImpact
        )
    }
}
