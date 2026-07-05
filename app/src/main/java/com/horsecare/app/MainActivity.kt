package com.horsecare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.horsecare.app.ui.RepositoryViewModelFactory
import com.horsecare.app.ui.screens.common.PlaceholderScreen
import com.horsecare.app.ui.screens.documents.DocumentsScreen
import com.horsecare.app.ui.screens.documents.DocumentsViewModel
import com.horsecare.app.ui.screens.home.HomeScreen
import com.horsecare.app.ui.screens.home.HomeViewModel
import com.horsecare.app.ui.screens.horse.AddHorseScreen
import com.horsecare.app.ui.screens.horse.AddHorseViewModel
import com.horsecare.app.ui.screens.horse.EditHorseViewModel
import com.horsecare.app.ui.theme.HorseCareTheme
import kotlinx.coroutines.launch

// Поки що працюємо з першим конем (id = 1). Коли додамо перемикач - id буде динамічним.
private const val CURRENT_HORSE_ID = 1L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorseCareTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                fun navigateFromDrawer(route: String) {
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text(
                                "HorseCare",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                            HorizontalDivider()
                            NavigationDrawerItem(
                                label = { Text("Головна") },
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("home") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text("Здоров'я") },
                                icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("health") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text("Тренування") },
                                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("training") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text("Годування") },
                                icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("feeding") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text("Документи") },
                                icon = { Icon(Icons.Default.Description, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("documents") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text("Профіль коня") },
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("editHorse") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                ) {
                    HorseCareNavHost(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun HorseCareNavHost(
    navController: NavHostController,
    onOpenDrawer: () -> Unit
) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                factory = RepositoryViewModelFactory { repo ->
                    HomeViewModel(repo, horseId = CURRENT_HORSE_ID)
                }
            )
            val uiState by homeViewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onAddHorseClick = { navController.navigate("addHorse") },
                onEditHorseClick = { navController.navigate("editHorse") },
                onMenuClick = onOpenDrawer,
                onMarkDone = { /* TODO: відмітити виконаним */ },
                onReschedule = { record ->
                    homeViewModel.rescheduleRecord(
                        record.id,
                        record.nextDueDate?.plusWeeks(1) ?: return@HomeScreen
                    )
                },
                onOpenHealth = { navController.navigate("health") },
                onOpenTraining = { navController.navigate("training") },
                onOpenFeeding = { navController.navigate("feeding") },
                onOpenProfile = { navController.navigate("editHorse") }
            )
        }

        composable("addHorse") {
            val addHorseViewModel: AddHorseViewModel = viewModel(
                factory = RepositoryViewModelFactory { repo -> AddHorseViewModel(repo) }
            )

            AddHorseScreen(
                onBack = { navController.popBackStack() },
                onSave = { name, breed, birthDate, sex, color, chipNumber,
                           photoUri, heightCm, weightKg, markings,
                           acquiredDate, sireName, damName ->
                    addHorseViewModel.saveHorse(
                        name = name, breed = breed, birthDate = birthDate, sex = sex, color = color,
                        chipNumber = chipNumber, photoUri = photoUri, heightCm = heightCm,
                        weightKg = weightKg, markings = markings, acquiredDate = acquiredDate,
                        sireName = sireName, damName = damName,
                        onSaved = { navController.popBackStack() }
                    )
                }
            )
        }

        composable("editHorse") {
            val editHorseViewModel: EditHorseViewModel = viewModel(
                factory = RepositoryViewModelFactory { repo ->
                    EditHorseViewModel(repo, horseId = CURRENT_HORSE_ID)
                }
            )
            val existingHorse by editHorseViewModel.horse.collectAsState()

            existingHorse?.let { horse ->
                AddHorseScreen(
                    initialHorse = horse,
                    onBack = { navController.popBackStack() },
                    onSave = { name, breed, birthDate, sex, color, chipNumber,
                               photoUri, heightCm, weightKg, markings,
                               acquiredDate, sireName, damName ->
                        editHorseViewModel.updateHorse(
                            horse.copy(
                                name = name, breed = breed, birthDate = birthDate, sex = sex,
                                color = color, chipNumber = chipNumber.takeIf { it.isNotBlank() },
                                photoUri = photoUri, heightCm = heightCm, weightKg = weightKg,
                                markings = markings?.takeIf { it.isNotBlank() },
                                acquiredDate = acquiredDate,
                                sireName = sireName?.takeIf { it.isNotBlank() },
                                damName = damName?.takeIf { it.isNotBlank() }
                            ),
                            onSaved = { navController.popBackStack() }
                        )
                    }
                )
            }
        }

        composable("documents") {
            val documentsViewModel: DocumentsViewModel = viewModel(
                factory = RepositoryViewModelFactory { repo ->
                    DocumentsViewModel(repo, horseId = CURRENT_HORSE_ID)
                }
            )
            val documents by documentsViewModel.documents.collectAsState()

            DocumentsScreen(
                documents = documents,
                onBack = { navController.popBackStack() },
                onAddDocument = { title, uri, mimeType ->
                    documentsViewModel.addDocument(title, uri, mimeType)
                },
                onDeleteDocument = { documentsViewModel.deleteDocument(it) }
            )
        }

        composable("health") {
            PlaceholderScreen(title = "Здоров'я", onBack = { navController.popBackStack() })
        }
        composable("training") {
            PlaceholderScreen(title = "Тренування", onBack = { navController.popBackStack() })
        }
        composable("feeding") {
            PlaceholderScreen(title = "Годування", onBack = { navController.popBackStack() })
        }
    }
}