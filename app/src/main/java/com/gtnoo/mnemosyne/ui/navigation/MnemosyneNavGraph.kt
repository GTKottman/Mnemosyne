package com.gtnoo.mnemosyne.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gtnoo.mnemosyne.presentation.dashboard.VisualizationDashboardScreen
import com.gtnoo.mnemosyne.presentation.entry.DailyEntryFormScreen
import com.gtnoo.mnemosyne.presentation.entry.EntryDetailScreen
import com.gtnoo.mnemosyne.presentation.history.JournalScreen
import com.gtnoo.mnemosyne.presentation.home.HomeScreen
import com.gtnoo.mnemosyne.presentation.importexport.ImportExportScreen
import com.gtnoo.mnemosyne.presentation.interaction.InteractionFormScreen
import com.gtnoo.mnemosyne.presentation.medicine.AddMedicineScreen
import com.gtnoo.mnemosyne.presentation.medicine.MedicineDetailScreen
import com.gtnoo.mnemosyne.presentation.onboarding.OnboardingScreen
import com.gtnoo.mnemosyne.presentation.people.AddEditPersonScreen
import com.gtnoo.mnemosyne.presentation.people.PersonAnalyticsScreen
import com.gtnoo.mnemosyne.presentation.people.PersonDetailScreen
import com.gtnoo.mnemosyne.presentation.places.AddEditPlaceScreen
import com.gtnoo.mnemosyne.presentation.places.PlaceDetailScreen
import com.gtnoo.mnemosyne.presentation.settings.SettingsScreen
import com.gtnoo.mnemosyne.presentation.similarity.SimilarityResultsScreen
import java.time.LocalDate

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val screen: Screen)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MnemosyneNavGraph(
    startDestination: String = Screen.Home.route,
    initialPersonId: String? = null,
    initialOpenInteraction: Boolean = false
) {
    val navController = rememberNavController()
    val bottomItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Home),
        BottomNavItem("Journal", Icons.Default.Book, Screen.EntryHistory),
        BottomNavItem("Dashboard", Icons.Default.BarChart, Screen.VisualizationDashboard)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomItems.any { it.screen.route == currentRoute }

    LaunchedEffect(initialPersonId, initialOpenInteraction) {
        if (initialPersonId != null) {
            if (initialOpenInteraction) {
                navController.navigate(
                    Screen.InteractionForm.create(
                        personId = initialPersonId,
                        date = LocalDate.now().toString()
                    )
                )
            } else {
                navController.navigate(Screen.PersonDetail.create(initialPersonId))
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNewEntry = { navController.navigate(Screen.DailyEntryForm.create()) },
                    onNewInteraction = { navController.navigate(Screen.InteractionForm.create()) },
                    onEntryClick = { navController.navigate(Screen.EntryDetail.create(it)) },
                    onSettings = { navController.navigate(Screen.Settings.route) },
                    onImportExport = { navController.navigate(Screen.ImportExport.route) },
                    onPersonClick = { navController.navigate(Screen.PersonDetail.create(it)) },
                    onLogInteractionForPerson = { personId ->
                        navController.navigate(
                            Screen.InteractionForm.create(
                                personId = personId,
                                date = LocalDate.now().toString()
                            )
                        )
                    },
                    onHistory = { navController.navigate(Screen.EntryHistory.route) }
                )
            }
            composable(Screen.EntryHistory.route) {
                JournalScreen(
                    onEntryClick = { navController.navigate(Screen.EntryDetail.create(it)) },
                    onNewEntry = { navController.navigate(Screen.DailyEntryForm.create()) },
                    onNewInteraction = { navController.navigate(Screen.InteractionForm.create()) },
                    onPersonClick = { navController.navigate(Screen.PersonDetail.create(it)) },
                    onAddPerson = { navController.navigate(Screen.AddEditPerson.create()) },
                    onPlaceClick = { navController.navigate(Screen.PlaceDetail.create(it)) },
                    onAddPlace = { navController.navigate(Screen.AddEditPlace.create()) },
                    onMedicineClick = { navController.navigate(Screen.MedicineDetail.create(it)) },
                    onAddMedicine = { navController.navigate(Screen.AddEditMedicine.create()) },
                    onEditMedicine = { navController.navigate(Screen.AddEditMedicine.create(it)) }
                )
            }
            composable(
                route = Screen.DailyEntryForm.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("date") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")?.takeIf { it.isNotBlank() }
                val date = backStackEntry.arguments?.getString("date")?.takeIf { it.isNotBlank() }
                DailyEntryFormScreen(
                    entryId = entryId,
                    date = date,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.InteractionForm.route,
                arguments = listOf(
                    navArgument("interactionId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("date") { type = NavType.StringType; defaultValue = "" },
                    navArgument("personId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val interactionId = backStackEntry.arguments?.getString("interactionId")?.takeIf { it.isNotBlank() }
                val date = backStackEntry.arguments?.getString("date")?.takeIf { it.isNotBlank() }
                val personId = backStackEntry.arguments?.getString("personId")?.takeIf { it.isNotBlank() }
                InteractionFormScreen(
                    interactionId = interactionId,
                    initialDate = date,
                    initialPersonId = personId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.EntryDetail.route) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                EntryDetailScreen(
                    entryId = entryId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.DailyEntryForm.create(entryId = it)) },
                    onSimilarity = { navController.navigate(Screen.SimilarityResults.create(it)) }
                )
            }
            composable(
                route = Screen.AddEditPerson.route,
                arguments = listOf(navArgument("personId") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId")?.takeIf { it.isNotBlank() }
                AddEditPersonScreen(personId = personId, onBack = { navController.popBackStack() })
            }
            composable(Screen.PersonDetail.route) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: return@composable
                PersonDetailScreen(
                    personId = personId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.AddEditPerson.create(it)) },
                    onAnalytics = { navController.navigate(Screen.PersonAnalytics.create(it)) },
                    onEntryClick = { navController.navigate(Screen.EntryDetail.create(it)) }
                )
            }
            composable(Screen.PersonAnalytics.route) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: return@composable
                PersonAnalyticsScreen(personId = personId, onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.AddEditPlace.route,
                arguments = listOf(navArgument("placeId") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId")?.takeIf { it.isNotBlank() }
                AddEditPlaceScreen(placeId = placeId, onBack = { navController.popBackStack() })
            }
            composable(Screen.PlaceDetail.route) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                PlaceDetailScreen(
                    placeId = placeId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.AddEditPlace.create(it)) },
                    onEntryClick = { navController.navigate(Screen.EntryDetail.create(it)) }
                )
            }
            composable(Screen.VisualizationDashboard.route) {
                VisualizationDashboardScreen()
            }
            composable(Screen.SimilarityResults.route) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                SimilarityResultsScreen(
                    entryId = entryId,
                    onBack = { navController.popBackStack() },
                    onEntryClick = { navController.navigate(Screen.EntryDetail.create(it)) }
                )
            }
            composable(Screen.ImportExport.route) {
                ImportExportScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.AddEditMedicine.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getString("medicineId")?.takeIf { it.isNotBlank() }
                AddMedicineScreen(medicineId = medicineId, onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.MedicineDetail.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getString("medicineId") ?: return@composable
                MedicineDetailScreen(
                    medicineId = medicineId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.AddEditMedicine.create(it)) }
                )
            }
        }
    }
}
