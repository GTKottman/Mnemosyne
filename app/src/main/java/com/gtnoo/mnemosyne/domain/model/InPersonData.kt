package com.gtnoo.mnemosyne.domain.model

data class InPersonData(
    val sawInPerson: Boolean = false,
    val talkedInPerson: Boolean = false,
    val theyApproachedMe: Boolean = false,
    val iApproachedThem: Boolean = false,
    val satNearEachOther: Boolean = false,
    val walkedTogether: Boolean = false,
    val theySmiled: Boolean = false,
    val theyLaughed: Boolean = false,
    val conversationMinutes: Int = 0,
    val eyeContactLevel: Int = 0,
    val physicalProximity: Int = 0,
    val feltNatural: Int = 0,
    val feltAwkward: Int = 0
)
