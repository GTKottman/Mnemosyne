package com.gtnoo.mnemosyne.domain.model

data class HealthContextData(
    val hoursSlept: Double = 0.0,
    val ateEnough: Boolean = false,
    val caffeineIntakeMg: Int = 0,
    val schoolStress: Int = 0,
    val workStress: Int = 0,
    val sensoryOverload: Int = 0,
    val energyLevel: Int = 5,
    val bodyDiscomfort: Int = 0,
    val executiveFunction: Int = 5,
    val socialBattery: Int = 5
)
