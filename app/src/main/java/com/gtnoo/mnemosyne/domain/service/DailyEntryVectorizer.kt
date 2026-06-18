package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.log10

class DailyEntryVectorizer @Inject constructor() {

    fun vectorize(entry: DailyEntry): DailyEntryVector {
        val interactions = entry.interactions
        return DailyEntryVector(
            entryId = entry.id,
            emotionVector = encodeEmotionData(entry.emotionData),
            thoughtVector = encodeThoughtData(entry.thoughtPatternData),
            healthVector = encodeHealthData(entry.healthContextData),
            intakeVector = encodeIntakeData(entry.intakeData),
            environmentVector = encodeEnvironmentData(entry.environmentData),
            weatherVector = encodeWeatherData(entry.weatherSnapshots.firstOrNull()),
            aggregateCommunicationVector = aggregateCommunication(interactions),
            aggregateInPersonVector = aggregateInPerson(interactions),
            aggregateRelationshipVector = aggregateRelationship(interactions),
            aggregateOutcomeVector = aggregateOutcome(interactions)
        )
    }

    private fun encodeEmotionData(e: EmotionData) = doubleArrayOf(
        encodeScale(e.happiness), encodeScale(e.safety), encodeScale(e.calm),
        encodeScale(e.connection), encodeScale(e.clarity), encodeScale(e.capacity),
        encodeScale(e.hope), encodeScale(e.worthiness), encodeScale(e.peace),
        encodeScale(e.energy), encodeScale(e.agency), encodeScale(e.presence)
    )

    private fun encodeThoughtData(t: ThoughtPatternData) = doubleArrayOf(
        encodeScale(t.ruminationLevel), encodeScale(t.intrusiveThoughts),
        encodeScale(t.worryAnticipation), encodeScale(t.catastrophizing),
        encodeScale(t.reassuranceUrge), encodeScale(t.mentalClarity),
        encodeScale(t.uncertaintyTolerance), encodeScale(t.decisionFriction),
        encodeScale(t.cognitiveFlexibility), encodeScale(t.selfTalkTone)
    )

    private fun encodeHealthData(h: HealthContextData) = doubleArrayOf(
        encodeScale(h.energyLevel), encodeScale(h.socialBattery),
        encodeScale(h.executiveFunction), encodeScale(h.mentalBandwidth),
        encodeScale(h.bodyDiscomfort), encodeScale(h.sensoryEnvironmentalStrain),
        encodeScale(h.sleepQuality), encodeScale(h.stressLoad),
        encodeScale(h.workSchoolPressure), encodeScale(h.moneyPressure),
        encodeScale(h.relationshipPressure), encodeScale(h.familyPressure),
        encodeScale(h.healthPressure), encodeScale(h.timePressure)
    )

    private fun encodeIntakeData(i: IntakeData) = doubleArrayOf(
        encodeSleep(i.hoursSlept), encodeCaffeine(i.caffeineIntakeMg),
        encodeScale(i.foodAdequacy), encodeScale(i.hydration),
        encodeScale(i.movementLevel)
    )

    private fun encodeEnvironmentData(e: EnvironmentData) = doubleArrayOf(
        encodeScale(e.timeOutsideSunlight), encodeScale(e.screenLoad),
        encodeScale(e.socialExposure), encodeScale(e.noveltyDisruption),
        encodeScale(e.physicalSpaceQuality), encodeScale(e.weatherImpact)
    )

    private fun encodeWeatherData(w: WeatherSnapshot?): DoubleArray {
        if (w == null) return DoubleArray(8) { 0.0 }
        return doubleArrayOf(
            encodeTemp(w.temperatureAverage),
            encodeHumidity(w.humidity),
            encodePrecip(w.precipitationMm),
            encodeWind(w.windSpeed),
            encodeUv(w.uvIndex),
            encodePressure(w.pressure),
            encodeWeatherCondition(w.condition),
            encodeSeason(w.season)
        )
    }

    private fun encodeCommunicationData(c: CommunicationData) = doubleArrayOf(
        encodeBoolean(c.theyInitiatedContact), encodeBoolean(c.iInitiatedContact),
        encodeBoolean(c.theyReplied), encodeBoolean(c.iReplied),
        encodeBoolean(c.leftOnRead), encodeBoolean(c.iWasLeftOnRead),
        encodeBoolean(c.keptConversationGoing), encodeBoolean(c.conversationEndedAbruptly),
        encodeCount(c.totalMessageCount), encodeReplyTime(c.averageReplyTimeMinutes)
    )

    private fun encodeInPersonData(ip: InPersonData) = doubleArrayOf(
        encodeBoolean(ip.sawInPerson), encodeBoolean(ip.talkedInPerson),
        encodeBoolean(ip.theyApproachedMe), encodeBoolean(ip.iApproachedThem),
        encodeBoolean(ip.walkedTogether), encodeScale(ip.eyeContactLevel),
        encodeScale(ip.physicalProximity), encodeScale(ip.feltNatural),
        encodeScale(ip.feltAwkward), encodeConvMinutes(ip.conversationMinutes)
    )

    private fun encodeRelationshipData(r: RelationshipSignalData) = doubleArrayOf(
        encodeScale(r.feltPrioritized), encodeScale(r.feltIgnored),
        encodeScale(r.feltChosen), encodeScale(r.feltOptional),
        encodeScale(r.feltSafeWithThem), encodeScale(r.feltConfusedByThem),
        encodeScale(r.feltTheyWereWarm), encodeScale(r.feltTheyWereDistant),
        encodeScale(r.feltMutualInterest), encodeScale(r.feltConnectionStable)
    )

    private fun encodeOutcomeData(o: OutcomeData) = doubleArrayOf(
        encodeBoolean(o.feltBetterAfter), encodeBoolean(o.feltWorseAfter),
        encodeBoolean(o.situationResolved), encodeBoolean(o.talkedAgainLater),
        encodeBoolean(o.theyFollowedUp), encodeBoolean(o.myPredictionWasCorrect),
        encodeBoolean(o.anxietyWasFalseAlarm),
        encodeScale(o.connectionImproved), encodeScale(o.connectionDeclined)
    )

    private fun aggregateCommunication(interactions: List<PersonInteraction>): DoubleArray {
        if (interactions.isEmpty()) return DoubleArray(10) { 0.0 }
        val encoded = interactions.map { encodeCommunicationData(it.communicationData) }
        return elementWiseMean(encoded)
    }

    private fun aggregateInPerson(interactions: List<PersonInteraction>): DoubleArray {
        if (interactions.isEmpty()) return DoubleArray(10) { 0.0 }
        val encoded = interactions.map { encodeInPersonData(it.inPersonData) }
        return elementWiseMean(encoded)
    }

    private fun aggregateRelationship(interactions: List<PersonInteraction>): DoubleArray {
        if (interactions.isEmpty()) return DoubleArray(10) { 0.0 }
        val encoded = interactions.map { encodeRelationshipData(it.relationshipSignalData) }
        return elementWiseMean(encoded)
    }

    private fun aggregateOutcome(interactions: List<PersonInteraction>): DoubleArray {
        if (interactions.isEmpty()) return DoubleArray(9) { 0.0 }
        val encoded = interactions.map { encodeOutcomeData(it.outcomeData) }
        return elementWiseMean(encoded)
    }

    private fun elementWiseMean(vectors: List<DoubleArray>): DoubleArray {
        if (vectors.isEmpty()) return DoubleArray(0)
        val size = vectors.first().size
        return DoubleArray(size) { i -> vectors.sumOf { it[i] } / vectors.size }
    }

    fun encodeBoolean(value: Boolean): Double = if (value) 1.0 else 0.0
    fun encodeScale(value: Int): Double = value.coerceIn(0, 10) / 10.0
    private fun encodeSleep(hours: Double) = hours.coerceIn(0.0, 12.0) / 12.0
    private fun encodeCaffeine(mg: Int) = (mg.coerceIn(0, 800) / 800.0)
    private fun encodeCount(count: Int) = ln((count + 1).toDouble()) / ln(101.0)
    private fun encodeReplyTime(minutes: Double) = 1.0 - (minutes.coerceIn(0.0, 1440.0) / 1440.0)
    private fun encodeConvMinutes(minutes: Int) = minutes.coerceIn(0, 480) / 480.0
    private fun encodeTemp(temp: Double?) = ((temp ?: 20.0).coerceIn(-30.0, 50.0) + 30.0) / 80.0
    private fun encodeHumidity(h: Double?) = (h ?: 50.0).coerceIn(0.0, 100.0) / 100.0
    private fun encodePrecip(mm: Double?) = (mm ?: 0.0).coerceIn(0.0, 50.0) / 50.0
    private fun encodeWind(speed: Double?) = (speed ?: 0.0).coerceIn(0.0, 100.0) / 100.0
    private fun encodeUv(uv: Double?) = (uv ?: 0.0).coerceIn(0.0, 12.0) / 12.0
    private fun encodePressure(p: Double?) = ((p ?: 1013.0).coerceIn(970.0, 1050.0) - 970.0) / 80.0
    private fun encodeWeatherCondition(c: WeatherCondition): Double = when (c) {
        WeatherCondition.SUNNY -> 1.0
        WeatherCondition.CLOUDY -> 0.75
        WeatherCondition.FOGGY -> 0.5
        WeatherCondition.WINDY -> 0.6
        WeatherCondition.RAINY -> 0.35
        WeatherCondition.STORMY -> 0.1
        WeatherCondition.SNOWY -> 0.3
        WeatherCondition.UNKNOWN -> 0.5
    }
    private fun encodeSeason(s: Season): Double = when (s) {
        Season.SPRING -> 0.75
        Season.SUMMER -> 1.0
        Season.FALL -> 0.5
        Season.WINTER -> 0.25
    }
}
