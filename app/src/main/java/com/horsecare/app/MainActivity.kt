package com.horsecare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.horsecare.app.ui.RepositoryViewModelFactory
import com.horsecare.app.ui.screens.home.HomeScreen
import com.horsecare.app.ui.screens.home.HomeViewModel
import com.horsecare.app.ui.screens.horse.AddHorseScreen
import com.horsecare.app.ui.screens.horse.AddHorseViewModel
import com.horsecare.app.ui.theme.HorseCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorseCareTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        // Поки що працюємо з першим конем (id = 1).
                        // Коли додамо перемикач - id буде динамічним.
                        val homeViewModel: HomeViewModel = viewModel(
                            factory = RepositoryViewModelFactory { repo ->
                                HomeViewModel(repo, horseId = 1L)
                            }
                        )
                        val uiState by homeViewModel.uiState.collectAsState()

                        HomeScreen(
                            uiState = uiState,
                            onAddHorseClick = { navController.navigate("addHorse") },
                            onMenuClick = { /* TODO: перемикання між кіньми */ },
                            onMarkDone = { /* TODO: відмітити виконаним */ },
                            onReschedule = { record ->
                                homeViewModel.rescheduleRecord(
                                    record.id,
                                    record.nextDueDate?.plusWeeks(1) ?: return@HomeScreen
                                )
                            },
                            onOpenHealth = { /* TODO: навігація до Здоров'я */ },
                            onOpenTraining = { /* TODO: навігація до Тренувань */ },
                            onOpenFeeding = { /* TODO: навігація до Годування */ },
                            onOpenProfile = { /* TODO: навігація до повного профілю */ }
                        )
                    }

                    composable("addHorse") {
                        val addHorseViewModel: AddHorseViewModel = viewModel(
                            factory = RepositoryViewModelFactory { repo ->
                                AddHorseViewModel(repo)
                            }
                        )

                        AddHorseScreen(
                            onBack = { navController.popBackStack() },
                            onSave = { name, breed, birthDate, sex, color, chipNumber,
                                       photoUri, heightCm, weightKg, markings,
                                       acquiredDate, sireName, damName ->
                                addHorseViewModel.saveHorse(
                                    name = name,
                                    breed = breed,
                                    birthDate = birthDate,
                                    sex = sex,
                                    color = color,
                                    chipNumber = chipNumber,
                                    photoUri = photoUri,
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    markings = markings,
                                    acquiredDate = acquiredDate,
                                    sireName = sireName,
                                    damName = damName,
                                    onSaved = { navController.popBackStack() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}