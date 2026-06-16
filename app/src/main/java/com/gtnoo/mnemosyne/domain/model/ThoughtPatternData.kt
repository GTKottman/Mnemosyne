package com.gtnoo.mnemosyne.domain.model

data class ThoughtPatternData(
    val ruminationLevel: Int = 0,
    val checkedPhoneRepeatedly: Boolean = false,
    val rereadMessages: Boolean = false,
    val imaginedNegativeOutcome: Boolean = false,
    val imaginedPositiveOutcome: Boolean = false,
    val needForReassurance: Int = 0,
    val clarityLevel: Int = 0,
    val uncertaintyLevel: Int = 0,
    val wantedToPullAway: Int = 0,
    val wantedToReachOut: Int = 0
)
