package com.horsecare.app.ui.screens.horse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.entity.HorseSex
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddHorseViewModel(
    private val repository: HorseCareRepository
) : ViewModel() {

    fun saveHorse(
        name: String,
        breed: String,
        birthDate: LocalDate,
        sex: HorseSex,
        color: String,
        chipNumber: String?,
        photoUri: String?,
        heightCm: Int?,
        weightKg: Double?,
        markings: String?,
        acquiredDate: LocalDate?,
        sireName: String?,
        damName: String?,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.saveHorse(
                Horse(
                    name = name,
                    breed = breed,
                    birthDate = birthDate,
                    sex = sex,
                    color = color,
                    chipNumber = chipNumber?.takeIf { it.isNotBlank() },
                    photoUri = photoUri,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    markings = markings?.takeIf { it.isNotBlank() },
                    acquiredDate = acquiredDate,
                    sireName = sireName?.takeIf { it.isNotBlank() },
                    damName = damName?.takeIf { it.isNotBlank() }
                )
            )
            onSaved(newId)
        }
    }
}