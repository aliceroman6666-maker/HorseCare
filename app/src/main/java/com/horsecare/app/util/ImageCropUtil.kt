package com.horsecare.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageCropUtil {

    /**
     * Обрізає зображення відповідно до масштабу (zoom) і зсуву (offsetFraction - у частках
     * від розміру вихідного квадрата, приблизно -0.5..0.5), точно як показано користувачу
     * в круглому перегляді, і зберігає результат як окремий JPEG-файл у сховищі застосунку.
     */
    fun cropAndSave(
        context: Context,
        sourceUri: Uri,
        zoom: Float,
        offsetFraction: Offset,
        outputSizePx: Int = 640
    ): Uri? {
        val bitmap = loadRotatedBitmap(context, sourceUri) ?: return null

        val baselineScale = maxOf(
            outputSizePx.toFloat() / bitmap.width,
            outputSizePx.toFloat() / bitmap.height
        )
        val effectiveScale = baselineScale * zoom
        val displayedW = bitmap.width * effectiveScale
        val displayedH = bitmap.height * effectiveScale

        val topLeftX = (outputSizePx / 2f - displayedW / 2f) + offsetFraction.x * outputSizePx
        val topLeftY = (outputSizePx / 2f - displayedH / 2f) + offsetFraction.y * outputSizePx

        val output = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val matrix = Matrix().apply {
            setScale(effectiveScale, effectiveScale)
            postTranslate(topLeftX, topLeftY)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)

        val dir = File(context.filesDir, "horse_photos").apply { mkdirs() }
        val file = File(dir, "horse_crop_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> output.compress(Bitmap.CompressFormat.JPEG, 92, out) }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Завантажує bitmap і одразу виправляє орієнтацію за EXIF (важливо для фото з камери). */
    private fun loadRotatedBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null

        val rotationDegrees = try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }

        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}