package com.gtnoo.mnemosyne.presentation.interaction

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.ui.components.SliderField
import com.gtnoo.mnemosyne.ui.components.SwitchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InteractionBlock(
    interaction: PersonInteraction,
    onUpdate: (PersonInteraction) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Communication", "In Person", "Interpretation", "Outcome")

    PrimaryScrollableTabRow(selectedTabIndex = selectedTab) {
        tabs.forEachIndexed { idx, title ->
            Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(title) })
        }
    }

    Spacer(Modifier.height(8.dp))

    when (selectedTab) {
        0 -> CommunicationBlock(interaction.communicationData) { onUpdate(interaction.copy(communicationData = it)) }
        1 -> InPersonBlock(interaction.inPersonData) { onUpdate(interaction.copy(inPersonData = it)) }
        2 -> RelationshipBlock(interaction.relationshipSignalData) { onUpdate(interaction.copy(relationshipSignalData = it)) }
        3 -> OutcomeBlock(interaction.outcomeData) { onUpdate(interaction.copy(outcomeData = it)) }
    }

    OutlinedTextField(
        value = interaction.notes,
        onValueChange = { onUpdate(interaction.copy(notes = it)) },
        label = { Text("Notes about this interaction") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        maxLines = 3
    )
}

@Composable
internal fun CommunicationBlock(data: CommunicationData, onUpdate: (CommunicationData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("They initiated contact", data.theyInitiatedContact) { onUpdate(data.copy(theyInitiatedContact = it)) }
        SwitchField("I initiated contact", data.iInitiatedContact) { onUpdate(data.copy(iInitiatedContact = it)) }
        SwitchField("They replied", data.theyReplied) { onUpdate(data.copy(theyReplied = it)) }
        SwitchField("Left me on read", data.leftOnRead) { onUpdate(data.copy(leftOnRead = it)) }
        SwitchField("I was left on read", data.iWasLeftOnRead) { onUpdate(data.copy(iWasLeftOnRead = it)) }
        SwitchField("Kept conversation going", data.keptConversationGoing) { onUpdate(data.copy(keptConversationGoing = it)) }
        SwitchField("Conversation ended abruptly", data.conversationEndedAbruptly) { onUpdate(data.copy(conversationEndedAbruptly = it)) }
        SwitchField("Used emoji", data.usedEmoji) { onUpdate(data.copy(usedEmoji = it)) }
        SwitchField("Asked a question", data.askedQuestion) { onUpdate(data.copy(askedQuestion = it)) }
        OutlinedTextField(
            value = if (data.totalMessageCount == 0) "" else data.totalMessageCount.toString(),
            onValueChange = { onUpdate(data.copy(totalMessageCount = it.toIntOrNull() ?: 0)) },
            label = { Text("Total messages") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
    }
}

@Composable
internal fun InPersonBlock(data: InPersonData, onUpdate: (InPersonData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("Saw in person", data.sawInPerson) { onUpdate(data.copy(sawInPerson = it)) }
        SwitchField("Talked in person", data.talkedInPerson) { onUpdate(data.copy(talkedInPerson = it)) }
        SwitchField("They approached me", data.theyApproachedMe) { onUpdate(data.copy(theyApproachedMe = it)) }
        SwitchField("I approached them", data.iApproachedThem) { onUpdate(data.copy(iApproachedThem = it)) }
        SwitchField("Walked together", data.walkedTogether) { onUpdate(data.copy(walkedTogether = it)) }
        SwitchField("They smiled", data.theySmiled) { onUpdate(data.copy(theySmiled = it)) }
        SwitchField("They laughed", data.theyLaughed) { onUpdate(data.copy(theyLaughed = it)) }
        SliderField("Eye contact (0-10)", data.eyeContactLevel) { onUpdate(data.copy(eyeContactLevel = it)) }
        SliderField("Physical proximity (0-10)", data.physicalProximity) { onUpdate(data.copy(physicalProximity = it)) }
        SliderField("Felt natural (0-10)", data.feltNatural) { onUpdate(data.copy(feltNatural = it)) }
        SliderField("Felt awkward (0-10)", data.feltAwkward) { onUpdate(data.copy(feltAwkward = it)) }
    }
}

@Composable
internal fun RelationshipBlock(data: RelationshipSignalData, onUpdate: (RelationshipSignalData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("These are your interpretations, not objective facts.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        SliderField("Felt Prioritized", data.feltPrioritized) { onUpdate(data.copy(feltPrioritized = it)) }
        SliderField("Felt Ignored", data.feltIgnored) { onUpdate(data.copy(feltIgnored = it)) }
        SliderField("Felt Chosen", data.feltChosen) { onUpdate(data.copy(feltChosen = it)) }
        SliderField("Felt Optional", data.feltOptional) { onUpdate(data.copy(feltOptional = it)) }
        SliderField("Felt Safe with Them", data.feltSafeWithThem) { onUpdate(data.copy(feltSafeWithThem = it)) }
        SliderField("Felt Confused by Them", data.feltConfusedByThem) { onUpdate(data.copy(feltConfusedByThem = it)) }
        SliderField("Felt They Were Warm", data.feltTheyWereWarm) { onUpdate(data.copy(feltTheyWereWarm = it)) }
        SliderField("Felt They Were Distant", data.feltTheyWereDistant) { onUpdate(data.copy(feltTheyWereDistant = it)) }
        SliderField("Felt Mutual Interest", data.feltMutualInterest) { onUpdate(data.copy(feltMutualInterest = it)) }
        SliderField("Felt Connection Stable", data.feltConnectionStable) { onUpdate(data.copy(feltConnectionStable = it)) }
    }
}

@Composable
internal fun OutcomeBlock(data: OutcomeData, onUpdate: (OutcomeData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SwitchField("Felt better after", data.feltBetterAfter) { onUpdate(data.copy(feltBetterAfter = it)) }
        SwitchField("Felt worse after", data.feltWorseAfter) { onUpdate(data.copy(feltWorseAfter = it)) }
        SwitchField("Situation resolved", data.situationResolved) { onUpdate(data.copy(situationResolved = it)) }
        SwitchField("Talked again later", data.talkedAgainLater) { onUpdate(data.copy(talkedAgainLater = it)) }
        SwitchField("They followed up", data.theyFollowedUp) { onUpdate(data.copy(theyFollowedUp = it)) }
        SwitchField("My prediction was correct", data.myPredictionWasCorrect) { onUpdate(data.copy(myPredictionWasCorrect = it)) }
        SwitchField("Anxiety was a false alarm", data.anxietyWasFalseAlarm) { onUpdate(data.copy(anxietyWasFalseAlarm = it)) }
        SliderField("Connection improved", data.connectionImproved) { onUpdate(data.copy(connectionImproved = it)) }
        SliderField("Connection declined", data.connectionDeclined) { onUpdate(data.copy(connectionDeclined = it)) }
    }
}
