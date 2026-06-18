package com.gtnoo.mnemosyne.domain.model

data class ThoughtPatternData(
    val ruminationLevel: Int = 0,
    val intrusiveThoughts: Int = 0,
    val worryAnticipation: Int = 0,
    val catastrophizing: Int = 0,
    val reassuranceUrge: Int = 0,
    val mentalClarity: Int = 0,
    val uncertaintyTolerance: Int = 0,
    val decisionFriction: Int = 0,
    val cognitiveFlexibility: Int = 0,
    val selfTalkTone: Int = 5
)
