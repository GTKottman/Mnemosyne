package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import java.time.LocalDate
import javax.inject.Inject

data class ChartPoint(val date: LocalDate, val value: Double, val label: String = "")
data class ScatterPoint(val x: Double, val y: Double, val label: String = "")

class VisualizationService @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend fun buildEmotionTrend(emotionName: String, range: DateRange): List<ChartPoint> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        return entries.sortedBy { it.entryDate }.map { entry ->
            val value = when (emotionName.lowercase()) {
                "hopeful" -> entry.emotionData.hopeful
                "anxious" -> entry.emotionData.anxious
                "sad" -> entry.emotionData.sad
                "happy" -> entry.emotionData.happy
                "calm" -> entry.emotionData.calm
                "lonely" -> entry.emotionData.lonely
                "excited" -> entry.emotionData.excited
                "confused" -> entry.emotionData.confused
                "secure" -> entry.emotionData.secure
                "jealous" -> entry.emotionData.jealous
                "rejected" -> entry.emotionData.rejected
                "connected" -> entry.emotionData.connected
                "overwhelmed" -> entry.emotionData.overwhelmed
                "regulated" -> entry.emotionData.regulated
                else -> 0
            }
            ChartPoint(entry.entryDate, value.toDouble())
        }
    }

    suspend fun buildConnectionTrend(person: Person, range: DateRange): List<ChartPoint> {
        val entries = entryRepository.getByPersonId(person.id).filter {
            !it.entryDate.isBefore(range.start) && !it.entryDate.isAfter(range.end)
        }
        return entries.sortedBy { it.entryDate }.mapNotNull { entry ->
            val score = entry.interactions
                .firstOrNull { it.person.id == person.id }
                ?.relationshipSignalData?.feltConnectionStable ?: return@mapNotNull null
            ChartPoint(entry.entryDate, score.toDouble())
        }
    }

    suspend fun buildSleepVsAnxiety(range: DateRange): List<ScatterPoint> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        return entries.map { entry ->
            ScatterPoint(
                x = entry.healthContextData.hoursSlept,
                y = entry.emotionData.anxious.toDouble(),
                label = entry.entryDate.toString()
            )
        }
    }

    suspend fun buildWeatherVsMood(range: DateRange): List<ScatterPoint> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        return entries.mapNotNull { entry ->
            val temp = entry.weatherSnapshots.firstOrNull()?.temperatureAverage ?: return@mapNotNull null
            ScatterPoint(
                x = temp,
                y = entry.emotionData.happy.toDouble(),
                label = entry.entryDate.toString()
            )
        }
    }

    suspend fun buildSocialBatteryVsInteractionCount(range: DateRange): List<ChartPoint> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        return entries.sortedBy { it.entryDate }.map { entry ->
            ChartPoint(
                date = entry.entryDate,
                value = entry.healthContextData.socialBattery.toDouble(),
                label = "${entry.interactions.size} interactions"
            )
        }
    }

    suspend fun buildMoodByPlaceBreakdown(range: DateRange): List<Pair<String, Double>> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        return entries
            .flatMap { entry -> entry.placesVisited.map { it.label to entry.emotionData.happy.toDouble() } }
            .groupBy { it.first }
            .map { (label, pairs) -> label to pairs.map { it.second }.average() }
            .sortedByDescending { it.second }
    }

    suspend fun buildCommunicationFrequency(person: Person, range: DateRange): List<ChartPoint> {
        val entries = entryRepository.getByPersonId(person.id).filter {
            !it.entryDate.isBefore(range.start) && !it.entryDate.isAfter(range.end)
        }
        return entries.sortedBy { it.entryDate }.mapNotNull { entry ->
            val count = entry.interactions
                .firstOrNull { it.person.id == person.id }
                ?.communicationData?.totalMessageCount ?: return@mapNotNull null
            ChartPoint(entry.entryDate, count.toDouble())
        }
    }

    suspend fun buildOutcomeSummary(range: DateRange): Map<String, Int> {
        val entries = entryRepository.getBetweenDates(range.start, range.end)
        val allOutcomes = entries.flatMap { it.interactions }.map { it.outcomeData }
        return mapOf(
            "Felt Better" to allOutcomes.count { it.feltBetterAfter },
            "Felt Worse" to allOutcomes.count { it.feltWorseAfter },
            "Resolved" to allOutcomes.count { it.situationResolved },
            "Anxiety was False Alarm" to allOutcomes.count { it.anxietyWasFalseAlarm },
            "Prediction Correct" to allOutcomes.count { it.myPredictionWasCorrect }
        )
    }
}
