package com.horsecare.app.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.HorseConditionAfterTraining
import com.horsecare.app.data.entity.TrainingSession
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrainingViewModel(
    private val repository: HorseCareRepository,
    private val horseId: Long
) : ViewModel() {

    val sessions: StateFlow<List<TrainingSession>> = repository.getSessionsForHorse(horseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val frequentTypes: StateFlow<List<String>> = repository.getFrequentTrainingTypes(horseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSession(
        date: LocalDate,
        type: String,
        durationMinutes: Int,
        notes: String?,
        condition: HorseConditionAfterTraining?
    ) {
        viewModelScope.launch {
            repository.saveTrainingSession(
                TrainingSession(
                    horseId = horseId,
                    date = date,
                    type = type,
                    durationMinutes = durationMinutes,
                    notes = notes?.takeIf { it.isNotBlank() },
                    horseCondition = condition
                )
            )
        }
    }
}