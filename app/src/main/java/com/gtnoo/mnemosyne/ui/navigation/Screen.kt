package com.gtnoo.mnemosyne.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Onboarding : Screen("onboarding")

    object DailyEntryForm : Screen("entry/form?entryId={entryId}&date={date}") {
        fun create(entryId: String? = null, date: String? = null): String {
            val idPart = entryId ?: ""
            val datePart = date ?: ""
            return "entry/form?entryId=$idPart&date=$datePart"
        }
    }

    object EntryDetail : Screen("entry/detail/{entryId}") {
        fun create(entryId: String) = "entry/detail/$entryId"
    }

    object EntryHistory : Screen("history")

    object PeopleDirectory : Screen("people")
    object AddEditPerson : Screen("people/edit?personId={personId}") {
        fun create(personId: String? = null) = "people/edit?personId=${personId ?: ""}"
    }
    object PersonDetail : Screen("people/detail/{personId}") {
        fun create(personId: String) = "people/detail/$personId"
    }
    object PersonAnalytics : Screen("people/analytics/{personId}") {
        fun create(personId: String) = "people/analytics/$personId"
    }

    object PlacesDirectory : Screen("places")
    object AddEditPlace : Screen("places/edit?placeId={placeId}") {
        fun create(placeId: String? = null) = "places/edit?placeId=${placeId ?: ""}"
    }
    object PlaceDetail : Screen("places/detail/{placeId}") {
        fun create(placeId: String) = "places/detail/$placeId"
    }

    object VisualizationDashboard : Screen("dashboard")
    object SimilarityResults : Screen("similarity/{entryId}") {
        fun create(entryId: String) = "similarity/$entryId"
    }

    object InteractionForm : Screen("interaction/form?interactionId={interactionId}&date={date}") {
        fun create(interactionId: String? = null, date: String? = null): String {
            val idPart = interactionId ?: ""
            val datePart = date ?: ""
            return "interaction/form?interactionId=$idPart&date=$datePart"
        }
    }

    object ImportExport : Screen("importexport")
    object Settings : Screen("settings")
}
