package com.horsecare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.horsecare.app.ui.RepositoryViewModelFactory
import com.horsecare.app.ui.SelectedHorseViewModel
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorseCareTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val selectedHorseViewModel: SelectedHorseViewModel = viewModel(
                    factory = RepositoryViewModelFactory { repo -> SelectedHorseViewModel(repo) }
                )
                val allHorses by selectedHorseViewModel.allHorses.collectAsState()
                val selectedHorseId by selectedHorseViewModel.selectedHorseId.collectAsState()

                fun navigateFromDrawer(route: String) {
                    scope.launch { drawerState.close() }
                    navController.navigate(route) { launchSingleTop = true }
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

                            Text(
                                "Мої коні",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            allHorses.forEach { horse ->
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            horse.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                                    selected = horse.id == selectedHorseId,
                                    onClick = {
                                        selectedHorseViewModel.selectHorse(horse.id)
                                        navigateFromDrawer("home")
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                            NavigationDrawerItem(
                                label = { Text("Додати коня") },
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                selected = false,
                                onClick = { navigateFromDrawer("addHorse") },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                        }
                    }
                ) {
                    HorseCareNavHost(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        selectedHorseId = selectedHorseId,
                        onHorseSaved = { newId ->
                            selectedHorseViewModel.selectHorse(newId)
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun HorseCareNavHost(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    selectedHorseId: Long?,
    onHorseSaved: (Long) -> Unit
) {
    // Поки немає жодного коня - працюємо з "неіснуючим" id, HomeScreen сам покаже порожній стан.
    val activeHorseId = selectedHorseId ?: -1L

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                key = "home-$activeHorseId",
                factory = RepositoryViewModelFactory { repo ->
                    HomeViewModel(repo, horseId = activeHorseId)
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
                        onSaved = { newId ->
                            onHorseSaved(newId)
                            navController.popBackStack()
                        }
                    )
                }
            )
        }

        composable("editHorse") {
            val editHorseViewModel: EditHorseViewModel = viewModel(
                key = "edit-$activeHorseId",
                factory = RepositoryViewModelFactory { repo ->
                    EditHorseViewModel(repo, horseId = activeHorseId)
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
                key = "documents-$activeHorseId",
                factory = RepositoryViewModelFactory { repo ->
                    DocumentsViewModel(repo, horseId = activeHorseId)
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