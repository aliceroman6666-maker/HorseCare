package com.horsecare.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Тримає список усіх коней і id поточно обраного.
 * Якщо користувач ще нічого не обирав вручну - автоматично береться перший кінь зі списку.
 */
class SelectedHorseViewModel(
    repository: HorseCareRepository
) : ViewModel() {

    private val manuallySelectedId = MutableStateFlow<Long?>(null)

    val allHorses: StateFlow<List<Horse>> = repository.getAllHorses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedHorseId: StateFlow<Long?> = combine(
        manuallySelectedId, allHorses
    ) { manual, horses ->
        manual ?: horses.firstOrNull()?.id
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectHorse(id: Long) {
        manuallySelectedId.value = id
    }
}