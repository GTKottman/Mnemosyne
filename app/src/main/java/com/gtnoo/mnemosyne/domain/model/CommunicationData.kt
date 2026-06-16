package com.gtnoo.mnemosyne.domain.model

data class CommunicationData(
    val theyInitiatedContact: Boolean = false,
    val iInitiatedContact: Boolean = false,
    val theyReplied: Boolean = false,
    val iReplied: Boolean = false,
    val leftOnRead: Boolean = false,
    val iWasLeftOnRead: Boolean = false,
    val reactedToMessage: Boolean = false,
    val sentLongMessage: Boolean = false,
    val sentShortMessage: Boolean = false,
    val usedEmoji: Boolean = false,
    val usedExclamation: Boolean = false,
    val askedQuestion: Boolean = false,
    val keptConversationGoing: Boolean = false,
    val conversationEndedAbruptly: Boolean = false,
    val totalMessageCount: Int = 0,
    val theirMessageCount: Int = 0,
    val myMessageCount: Int = 0,
    val averageReplyTimeMinutes: Double = 0.0,
    val longestGapHours: Double = 0.0
)
