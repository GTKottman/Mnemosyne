package com.gtnoo.mnemosyne.domain.model

data class AppSettings(
    val homePlaceId: String? = null,
    val autoFetchWeather: Boolean = true,
    val useFahrenheit: Boolean = false,
    val onboardingComplete: Boolean = false,
    val appVersion: String = "0.1.0",
    val showWorkSchoolPressure: Boolean = true,
    val showMoneyPressure: Boolean = true,
    val showRelationshipPressure: Boolean = true,
    val showFamilyPressure: Boolean = true,
    val showHealthPressure: Boolean = true,
    val showTimePressure: Boolean = true
)
