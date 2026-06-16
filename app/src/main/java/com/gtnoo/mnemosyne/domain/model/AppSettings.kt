package com.gtnoo.mnemosyne.domain.model

data class AppSettings(
    val homePlaceId: String? = null,
    val autoFetchWeather: Boolean = true,
    val onboardingComplete: Boolean = false,
    val appVersion: String = "0.1.0"
)
