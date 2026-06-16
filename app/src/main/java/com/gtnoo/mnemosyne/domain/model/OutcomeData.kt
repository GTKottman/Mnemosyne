package com.gtnoo.mnemosyne.domain.model

data class OutcomeData(
    val feltBetterAfter: Boolean = false,
    val feltWorseAfter: Boolean = false,
    val situationResolved: Boolean = false,
    val talkedAgainLater: Boolean = false,
    val theyFollowedUp: Boolean = false,
    val myPredictionWasCorrect: Boolean = false,
    val anxietyWasFalseAlarm: Boolean = false,
    val connectionImproved: Int = 0,
    val connectionDeclined: Int = 0
)
