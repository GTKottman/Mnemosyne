package com.gtnoo.mnemosyne.domain.model

data class SimilarityResult(
    val targetEntry: DailyEntry,
    val comparedEntry: DailyEntry,
    val similarityScore: Double,
    val similarityType: SimilarityType,
    val explanation: String = ""
)
