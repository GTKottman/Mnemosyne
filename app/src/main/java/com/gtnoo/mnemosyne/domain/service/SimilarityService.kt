package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.*
import javax.inject.Inject
import kotlin.math.sqrt

class SimilarityService @Inject constructor(
    private val vectorizer: DailyEntryVectorizer
) {
    fun cosineSimilarity(a: DoubleArray, b: DoubleArray): Double {
        if (a.size != b.size) return 0.0
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        normA = sqrt(normA)
        normB = sqrt(normB)
        return if (normA == 0.0 || normB == 0.0) 0.0 else dot / (normA * normB)
    }

    fun euclideanDistance(a: DoubleArray, b: DoubleArray): Double {
        if (a.size != b.size) return Double.MAX_VALUE
        var sum = 0.0
        for (i in a.indices) {
            val diff = a[i] - b[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    fun findSimilarEntries(
        target: DailyEntry,
        entries: List<DailyEntry>,
        type: SimilarityType = SimilarityType.FULL_VECTOR,
        limit: Int = 10
    ): List<SimilarityResult> {
        val targetVec = vectorizer.vectorize(target)
        return entries
            .filter { it.id != target.id }
            .map { entry ->
                val entryVec = vectorizer.vectorize(entry)
                val score = computeScore(targetVec, entryVec, type)
                SimilarityResult(
                    targetEntry = target,
                    comparedEntry = entry,
                    similarityScore = score,
                    similarityType = type,
                    explanation = buildExplanation(score, type)
                )
            }
            .sortedByDescending { it.similarityScore }
            .take(limit)
    }

    fun findSimilarDaysForPerson(
        person: Person,
        target: DailyEntry,
        allEntries: List<DailyEntry>
    ): List<SimilarityResult> {
        val personEntries = allEntries.filter { entry ->
            entry.interactions.any { it.person.id == person.id }
        }
        return findSimilarEntries(target, personEntries, SimilarityType.PER_PERSON)
    }

    private fun computeScore(
        a: DailyEntryVector,
        b: DailyEntryVector,
        type: SimilarityType
    ): Double = when (type) {
        SimilarityType.FULL_VECTOR -> cosineSimilarity(a.toFullVector(), b.toFullVector())
        SimilarityType.EMOTIONAL -> cosineSimilarity(a.emotionVector, b.emotionVector)
        SimilarityType.COMMUNICATION -> cosineSimilarity(a.aggregateCommunicationVector, b.aggregateCommunicationVector)
        SimilarityType.IN_PERSON -> cosineSimilarity(a.aggregateInPersonVector, b.aggregateInPersonVector)
        SimilarityType.WEATHER -> cosineSimilarity(a.weatherVector, b.weatherVector)
        SimilarityType.HEALTH_CONTEXT -> cosineSimilarity(a.healthVector, b.healthVector)
        SimilarityType.OUTCOME -> cosineSimilarity(a.aggregateOutcomeVector, b.aggregateOutcomeVector)
        SimilarityType.PER_PERSON -> cosineSimilarity(a.toFullVector(), b.toFullVector())
    }

    private fun buildExplanation(score: Double, type: SimilarityType): String {
        val pct = (score * 100).toInt()
        return "${pct}% similar (${type.label})"
    }
}
