package com.gtnoo.mnemosyne.domain.model

data class DailyEntryVector(
    val entryId: String,
    val emotionVector: DoubleArray,
    val thoughtVector: DoubleArray,
    val healthVector: DoubleArray,
    val intakeVector: DoubleArray,
    val environmentVector: DoubleArray,
    val weatherVector: DoubleArray,
    val aggregateCommunicationVector: DoubleArray,
    val aggregateInPersonVector: DoubleArray,
    val aggregateRelationshipVector: DoubleArray,
    val aggregateOutcomeVector: DoubleArray
) {
    fun toFullVector(): DoubleArray =
        emotionVector +
        thoughtVector +
        healthVector +
        intakeVector +
        environmentVector +
        weatherVector +
        aggregateCommunicationVector +
        aggregateInPersonVector +
        aggregateRelationshipVector +
        aggregateOutcomeVector

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DailyEntryVector) return false
        return entryId == other.entryId
    }

    override fun hashCode(): Int = entryId.hashCode()
}
