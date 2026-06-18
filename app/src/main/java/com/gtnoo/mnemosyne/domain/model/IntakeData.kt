package com.gtnoo.mnemosyne.domain.model

data class IntakeData(
    val hoursSlept: Double = 0.0,
    val caffeineIntakeMg: Int = 0,
    val foodAdequacy: Int = 5,
    val hydration: Int = 0,
    val movementLevel: Int = 0
)
