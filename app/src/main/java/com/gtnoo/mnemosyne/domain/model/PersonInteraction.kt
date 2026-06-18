package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate
import java.util.UUID

data class PersonInteraction(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val person: Person,
    val mode: InteractionMode = InteractionMode.DIGITAL_ONLY,
    val communicationData: CommunicationData = CommunicationData(),
    val inPersonData: InPersonData = InPersonData(),
    val relationshipSignalData: RelationshipSignalData = RelationshipSignalData(),
    val outcomeData: OutcomeData = OutcomeData(),
    val notes: String = ""
)
