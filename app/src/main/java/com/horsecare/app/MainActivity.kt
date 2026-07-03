package com.horsecare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horsecare.app.ui.RepositoryViewModelFactory
import com.horsecare.app.ui.screens.home.HomeScreen
import com.horsecare.app.ui.screens.home.HomeViewModel
import com.horsecare.app.ui.theme.HorseCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorseCareTheme {
                val app = application as HorseCareApp

                // Поки що працюємо з першим конем (id = 1).
                // Коли додамо екран вибору/додавання коня - id буде динамічним.
                val homeViewModel: HomeViewModel = viewModel(
                    factory = RepositoryViewModelFactory { repo ->
                        HomeViewModel(repo, horseId = 1L)
                    }
                )
                val uiState by homeViewModel.uiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    onAddHorseClick = { /* TODO: екран додавання коня */ },
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
        }
    }
}