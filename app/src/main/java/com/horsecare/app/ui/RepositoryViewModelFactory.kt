package com.horsecare.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.horsecare.app.HorseCareApp
import com.horsecare.app.data.repository.HorseCareRepository

/**
 * Спрощена generic-фабрика: приймає лямбду-конструктор ViewModel, якій передається repository.
 */
class RepositoryViewModelFactory<T : ViewModel>(
    private val create: (HorseCareRepository) -> T
) : ViewModelProvider.Factory {
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
        val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HorseCareApp
        @Suppress("UNCHECKED_CAST")
        return create(app.repository) as VM
    }
}