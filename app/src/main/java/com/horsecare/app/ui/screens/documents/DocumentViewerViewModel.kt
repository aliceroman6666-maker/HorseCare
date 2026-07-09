package com.horsecare.app.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.Document
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentViewerViewModel(
    private val repository: HorseCareRepository,
    documentId: Long
) : ViewModel() {

    private val _document = MutableStateFlow<Document?>(null)
    val document: StateFlow<Document?> = _document.asStateFlow()

    init {
        viewModelScope.launch {
            _document.value = repository.getDocumentById(documentId)
        }
    }
}