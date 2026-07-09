package com.horsecare.app.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsecare.app.data.entity.Document
import com.horsecare.app.data.repository.HorseCareRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val repository: HorseCareRepository,
    private val horseId: Long
) : ViewModel() {

    val documents: StateFlow<List<Document>> = repository.getDocuments(horseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** uris - одна чи декілька сторінок/фото, що складають один документ. */
    fun addDocument(title: String, uris: List<String>, mimeType: String?) {
        viewModelScope.launch {
            repository.saveDocument(
                Document(
                    horseId = horseId,
                    title = title,
                    uris = Document.joinUris(uris),
                    mimeType = mimeType
                )
            )
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }
}