package com.horsecare.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.HealthRecord
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.entity.TrainingSession
import com.horsecare.app.data.repository.HorseCareRepository
import com.horsecare.app.data.repository.OverdueHealthItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val horse: Horse? = null,
    val lastTraining: TrainingSession? = null,
    val overdueItems: List<OverdueHealthItem> = emptyList(),
    val nearestUpcoming: HealthRecord? = null,
    val documentsCount: Int = 0
)

class HomeViewModel(
    private val repository: HorseCareRepository,
    private val horseId: Long
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.getHorseById(horseId),
            repository.getLastSession(horseId),
            repository.getLatestRecordPerType(horseId),
            repository.getDocuments(horseId)
        ) { horse, lastTraining, latestPerType, documents ->
            val today = LocalDate.now()

            val overdue = latestPerType
                .filter { it.nextDueDate != null && it.nextDueDate.isBefore(today) }
                .map { OverdueHealthItem(it, ChronoUnit.DAYS.between(it.nextDueDate, today)) }
                .sortedByDescending { it.daysOverdue }

            val nearestUpcoming = latestPerType
                .filter { it.nextDueDate != null && !it.nextDueDate.isBefore(today) }
                .minByOrNull { it.nextDueDate!! }

            HomeUiState(
                horse = horse,
                lastTraining = lastTraining,
                overdueItems = overdue,
                nearestUpcoming = nearestUpcoming,
                documentsCount = documents.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun rescheduleRecord(recordId: Long, newDate: LocalDate) {
        viewModelScope.launch {
            repository.rescheduleHealthRecord(recordId, newDate)
        }
    }
}