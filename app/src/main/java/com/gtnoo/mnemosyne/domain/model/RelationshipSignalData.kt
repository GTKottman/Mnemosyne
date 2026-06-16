package com.gtnoo.mnemosyne.domain.model

data class RelationshipSignalData(
    val feltPrioritized: Int = 0,
    val feltIgnored: Int = 0,
    val feltChosen: Int = 0,
    val feltOptional: Int = 0,
    val feltSafeWithThem: Int = 0,
    val feltConfusedByThem: Int = 0,
    val feltTheyWereWarm: Int = 0,
    val feltTheyWereDistant: Int = 0,
    val feltMutualInterest: Int = 0,
    val feltConnectionStable: Int = 0
)
