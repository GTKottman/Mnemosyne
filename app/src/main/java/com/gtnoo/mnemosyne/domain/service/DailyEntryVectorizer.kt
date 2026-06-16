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
            weatherVector = encodeWeatherData(entry.weatherSnapshots.firstOrNull()),
            aggregateCommunicationVector = aggregateCommunication(interactions),
            aggregateInPersonVector = aggregateInPerson(interactions),
            aggregateRelationshipVector = aggregateRelationship(interactions),
            aggregateOutcomeVector = aggregateOutcome(interactions)
        )
    }

    private fun encodeEmotionData(e: EmotionData) = doubleArrayOf(
        encodeScale(e.hopeful), encodeScale(e.anxious), encodeScale(e.sad),
        encodeScale(e.happy), encodeScale(e.calm), encodeScale(e.lonely),
        encodeScale(e.excited), encodeScale(e.confused), encodeScale(e.secure),
        encodeScale(e.jealous), encodeScale(e.rejected), encodeScale(e.connected),
        encodeScale(e.overwhelmed), encodeScale(e.regulated)
    )

    private fun encodeThoughtData(t: ThoughtPatternData) = doubleArrayOf(
        encodeScale(t.ruminationLevel), encodeBoolean(t.checkedPhoneRepeatedly),
        encodeBoolean(t.rereadMessages), encodeBoolean(t.imaginedNegativeOutcome),
        encodeBoolean(t.imaginedPositiveOutcome), encodeScale(t.needForReassurance),
        encodeScale(t.clarityLevel), encodeScale(t.uncertaintyLevel),
        encodeScale(t.wantedToPullAway), encodeScale(t.wantedToReachOut)
    )

    private fun encodeHealthData(h: HealthContextData) = doubleArrayOf(
        encodeSleep(h.hoursSlept), encodeBoolean(h.ateEnough),
        encodeCaffeine(h.caffeineIntakeMg), encodeScale(h.schoolStress),
        encodeScale(h.workStress), encodeScale(h.sensoryOverload),
        encodeScale(h.energyLevel), encodeScale(h.bodyDiscomfort),
        encodeScale(h.executiveFunction), encodeScale(h.socialBattery)
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
