package com.gtnoo.mnemosyne.domain.model

/**
 * Each field is a single bipolar axis scored 0–10.
 * 0 = full distress pole, 5 = neutral, 10 = full supportive pole.
 */
data class EmotionData(
    val happiness: Int = 5,    // 0 = Sadness,      10 = Happiness
    val safety: Int = 5,       // 0 = Threat,        10 = Safety
    val calm: Int = 5,         // 0 = Agitation,     10 = Calm
    val connection: Int = 5,   // 0 = Isolation,     10 = Connection
    val clarity: Int = 5,      // 0 = Confusion,     10 = Clarity
    val capacity: Int = 5,     // 0 = Overwhelm,     10 = Capacity
    val hope: Int = 5,         // 0 = Hopelessness,  10 = Hope
    val worthiness: Int = 5,   // 0 = Shame,         10 = Worthiness
    val peace: Int = 5,        // 0 = Anger,         10 = Peace
    val energy: Int = 5,       // 0 = Exhaustion,    10 = Energy
    val agency: Int = 5,       // 0 = Helplessness,  10 = Agency
    val presence: Int = 5      // 0 = Numbness,      10 = Presence
)
