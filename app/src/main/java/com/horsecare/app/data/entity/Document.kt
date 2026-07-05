package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horseId: Long,
    val title: String,
    val uri: String,
    val mimeType: String? = null,
    val addedDate: LocalDate = LocalDate.now()
)