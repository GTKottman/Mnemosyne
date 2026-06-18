package com.gtnoo.mnemosyne.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gtnoo.mnemosyne.domain.model.*
import java.time.LocalDate

@Entity(
    tableName = "person_interactions",
    indices = [Index("date"), Index("personId")]
)
data class PersonInteractionEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val personId: String,
    val mode: String,
    val notes: String,

    // CommunicationData
    val commTheyInitiated: Boolean,
    val commIInitiated: Boolean,
    val commTheyReplied: Boolean,
    val commIReplied: Boolean,
    val commLeftOnRead: Boolean,
    val commIWasLeftOnRead: Boolean,
    val commReacted: Boolean,
    val commSentLong: Boolean,
    val commSentShort: Boolean,
    val commUsedEmoji: Boolean,
    val commUsedExclamation: Boolean,
    val commAskedQuestion: Boolean,
    val commKeptGoing: Boolean,
    val commEndedAbruptly: Boolean,
    val commTotalMessages: Int,
    val commTheirMessages: Int,
    val commMyMessages: Int,
    val commAvgReplyTime: Double,
    val commLongestGap: Double,

    // InPersonData
    val inpSawInPerson: Boolean,
    val inpTalkedInPerson: Boolean,
    val inpTheyApproached: Boolean,
    val inpIApproached: Boolean,
    val inpSatNear: Boolean,
    val inpWalkedTogether: Boolean,
    val inpTheySmiled: Boolean,
    val inpTheyLaughed: Boolean,
    val inpConversationMinutes: Int,
    val inpEyeContact: Int,
    val inpPhysicalProximity: Int,
    val inpFeltNatural: Int,
    val inpFeltAwkward: Int,

    // RelationshipSignalData
    val relFeltPrioritized: Int,
    val relFeltIgnored: Int,
    val relFeltChosen: Int,
    val relFeltOptional: Int,
    val relFeltSafe: Int,
    val relFeltConfused: Int,
    val relFeltWarm: Int,
    val relFeltDistant: Int,
    val relFeltMutual: Int,
    val relFeltStable: Int,

    // OutcomeData
    val outFeltBetter: Boolean,
    val outFeltWorse: Boolean,
    val outResolved: Boolean,
    val outTalkedAgain: Boolean,
    val outTheyFollowedUp: Boolean,
    val outPredictionCorrect: Boolean,
    val outAnxietyFalseAlarm: Boolean,
    val outConnectionImproved: Int,
    val outConnectionDeclined: Int
) {
    fun toDomain(person: Person) = PersonInteraction(
        id = id,
        date = date,
        person = person,
        mode = InteractionMode.valueOf(mode),
        notes = notes,
        communicationData = CommunicationData(
            theyInitiatedContact = commTheyInitiated, iInitiatedContact = commIInitiated,
            theyReplied = commTheyReplied, iReplied = commIReplied,
            leftOnRead = commLeftOnRead, iWasLeftOnRead = commIWasLeftOnRead,
            reactedToMessage = commReacted, sentLongMessage = commSentLong,
            sentShortMessage = commSentShort, usedEmoji = commUsedEmoji,
            usedExclamation = commUsedExclamation, askedQuestion = commAskedQuestion,
            keptConversationGoing = commKeptGoing, conversationEndedAbruptly = commEndedAbruptly,
            totalMessageCount = commTotalMessages, theirMessageCount = commTheirMessages,
            myMessageCount = commMyMessages, averageReplyTimeMinutes = commAvgReplyTime,
            longestGapHours = commLongestGap
        ),
        inPersonData = InPersonData(
            sawInPerson = inpSawInPerson, talkedInPerson = inpTalkedInPerson,
            theyApproachedMe = inpTheyApproached, iApproachedThem = inpIApproached,
            satNearEachOther = inpSatNear, walkedTogether = inpWalkedTogether,
            theySmiled = inpTheySmiled, theyLaughed = inpTheyLaughed,
            conversationMinutes = inpConversationMinutes, eyeContactLevel = inpEyeContact,
            physicalProximity = inpPhysicalProximity, feltNatural = inpFeltNatural,
            feltAwkward = inpFeltAwkward
        ),
        relationshipSignalData = RelationshipSignalData(
            feltPrioritized = relFeltPrioritized, feltIgnored = relFeltIgnored,
            feltChosen = relFeltChosen, feltOptional = relFeltOptional,
            feltSafeWithThem = relFeltSafe, feltConfusedByThem = relFeltConfused,
            feltTheyWereWarm = relFeltWarm, feltTheyWereDistant = relFeltDistant,
            feltMutualInterest = relFeltMutual, feltConnectionStable = relFeltStable
        ),
        outcomeData = OutcomeData(
            feltBetterAfter = outFeltBetter, feltWorseAfter = outFeltWorse,
            situationResolved = outResolved, talkedAgainLater = outTalkedAgain,
            theyFollowedUp = outTheyFollowedUp, myPredictionWasCorrect = outPredictionCorrect,
            anxietyWasFalseAlarm = outAnxietyFalseAlarm,
            connectionImproved = outConnectionImproved, connectionDeclined = outConnectionDeclined
        )
    )

    companion object {
        fun fromDomain(interaction: PersonInteraction) = PersonInteractionEntity(
            id = interaction.id, date = interaction.date,
            personId = interaction.person.id, mode = interaction.mode.name, notes = interaction.notes,
            commTheyInitiated = interaction.communicationData.theyInitiatedContact,
            commIInitiated = interaction.communicationData.iInitiatedContact,
            commTheyReplied = interaction.communicationData.theyReplied,
            commIReplied = interaction.communicationData.iReplied,
            commLeftOnRead = interaction.communicationData.leftOnRead,
            commIWasLeftOnRead = interaction.communicationData.iWasLeftOnRead,
            commReacted = interaction.communicationData.reactedToMessage,
            commSentLong = interaction.communicationData.sentLongMessage,
            commSentShort = interaction.communicationData.sentShortMessage,
            commUsedEmoji = interaction.communicationData.usedEmoji,
            commUsedExclamation = interaction.communicationData.usedExclamation,
            commAskedQuestion = interaction.communicationData.askedQuestion,
            commKeptGoing = interaction.communicationData.keptConversationGoing,
            commEndedAbruptly = interaction.communicationData.conversationEndedAbruptly,
            commTotalMessages = interaction.communicationData.totalMessageCount,
            commTheirMessages = interaction.communicationData.theirMessageCount,
            commMyMessages = interaction.communicationData.myMessageCount,
            commAvgReplyTime = interaction.communicationData.averageReplyTimeMinutes,
            commLongestGap = interaction.communicationData.longestGapHours,
            inpSawInPerson = interaction.inPersonData.sawInPerson,
            inpTalkedInPerson = interaction.inPersonData.talkedInPerson,
            inpTheyApproached = interaction.inPersonData.theyApproachedMe,
            inpIApproached = interaction.inPersonData.iApproachedThem,
            inpSatNear = interaction.inPersonData.satNearEachOther,
            inpWalkedTogether = interaction.inPersonData.walkedTogether,
            inpTheySmiled = interaction.inPersonData.theySmiled,
            inpTheyLaughed = interaction.inPersonData.theyLaughed,
            inpConversationMinutes = interaction.inPersonData.conversationMinutes,
            inpEyeContact = interaction.inPersonData.eyeContactLevel,
            inpPhysicalProximity = interaction.inPersonData.physicalProximity,
            inpFeltNatural = interaction.inPersonData.feltNatural,
            inpFeltAwkward = interaction.inPersonData.feltAwkward,
            relFeltPrioritized = interaction.relationshipSignalData.feltPrioritized,
            relFeltIgnored = interaction.relationshipSignalData.feltIgnored,
            relFeltChosen = interaction.relationshipSignalData.feltChosen,
            relFeltOptional = interaction.relationshipSignalData.feltOptional,
            relFeltSafe = interaction.relationshipSignalData.feltSafeWithThem,
            relFeltConfused = interaction.relationshipSignalData.feltConfusedByThem,
            relFeltWarm = interaction.relationshipSignalData.feltTheyWereWarm,
            relFeltDistant = interaction.relationshipSignalData.feltTheyWereDistant,
            relFeltMutual = interaction.relationshipSignalData.feltMutualInterest,
            relFeltStable = interaction.relationshipSignalData.feltConnectionStable,
            outFeltBetter = interaction.outcomeData.feltBetterAfter,
            outFeltWorse = interaction.outcomeData.feltWorseAfter,
            outResolved = interaction.outcomeData.situationResolved,
            outTalkedAgain = interaction.outcomeData.talkedAgainLater,
            outTheyFollowedUp = interaction.outcomeData.theyFollowedUp,
            outPredictionCorrect = interaction.outcomeData.myPredictionWasCorrect,
            outAnxietyFalseAlarm = interaction.outcomeData.anxietyWasFalseAlarm,
            outConnectionImproved = interaction.outcomeData.connectionImproved,
            outConnectionDeclined = interaction.outcomeData.connectionDeclined
        )
    }
}
