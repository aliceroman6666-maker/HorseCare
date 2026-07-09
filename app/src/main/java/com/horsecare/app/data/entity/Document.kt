package com.horsecare.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

private const val URI_DELIMITER = "|||"

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horseId: Long,
    val title: String,
    // Зберігається в тій самій колонці "uri", що й раніше (без міграції БД),
    // але тепер може містити декілька URI, розділених спецсимволом -
    // потрібно для документів з кількох фото (наприклад, сторінки паспорта).
    @ColumnInfo(name = "uri") val uris: String,
    val mimeType: String? = null,
    val addedDate: LocalDate = LocalDate.now()
) {
    val uriList: List<String>
        get() = uris.split(URI_DELIMITER).filter { it.isNotBlank() }

    companion object {
        fun joinUris(uris: List<String>): String = uris.joinToString(URI_DELIMITER)
    }
}