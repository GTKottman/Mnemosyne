package com.gtnoo.mnemosyne.domain.model

data class EnvironmentData(
    val timeOutsideSunlight: Int = 0,
    val screenLoad: Int = 0,
    val socialExposure: Int = 0,
    val noveltyDisruption: Int = 0,
    val physicalSpaceQuality: Int = 5,
    val weatherImpact: Int = 0
)
