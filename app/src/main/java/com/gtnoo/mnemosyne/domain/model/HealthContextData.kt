package com.gtnoo.mnemosyne.domain.model

data class HealthContextData(
    // Core State
    val energyLevel: Int = 5,
    val socialBattery: Int = 5,
    val executiveFunction: Int = 5,
    val mentalBandwidth: Int = 5,
    val bodyDiscomfort: Int = 0,
    val sensoryEnvironmentalStrain: Int = 0,
    val sleepQuality: Int = 5,
    val stressLoad: Int = 0,

    // Stress Categories
    val workSchoolPressure: Int = 0,
    val moneyPressure: Int = 0,
    val relationshipPressure: Int = 0,
    val familyPressure: Int = 0,
    val healthPressure: Int = 0,
    val timePressure: Int = 0
)
