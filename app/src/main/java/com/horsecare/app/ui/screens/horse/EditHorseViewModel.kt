package com.horsecare.app.ui.screens.horse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditHorseViewModel(
    private val repository: HorseCareRepository,
    private val horseId: Long
) : ViewModel() {

    val horse: StateFlow<Horse?> = repository.getHorseById(horseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val documentsCount: StateFlow<Int> = repository.getDocuments(horseId)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateHorse(updated: Horse, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateHorse(updated)
            onSaved()
        }
    }

    fun deleteHorse(horse: Horse, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHorseCompletely(horse)
            onDeleted()
        }
    }
}