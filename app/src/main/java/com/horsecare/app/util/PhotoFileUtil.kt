package com.horsecare.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.time.Instant

object PhotoFileUtil {

    /**
     * Створює порожній файл у внутрішньому сховищі застосунку і повертає
     * content:// URI (через FileProvider) - саме його треба передати камері.
     */
    fun createHorsePhotoUri(context: Context): Uri {
        val photosDir = File(context.filesDir, "horse_photos").apply { mkdirs() }
        val file = File(photosDir, "horse_${Instant.now().toEpochMilli()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}