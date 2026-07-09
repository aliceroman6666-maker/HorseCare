package com.horsecare.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfRenderUtil {

    /** Рендерить усі сторінки PDF у список Bitmap (по одному зображенню на сторінку). */
    suspend fun renderPages(context: Context, uri: Uri, targetWidthPx: Int = 1080): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
            pfd.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    (0 until renderer.pageCount).map { index ->
                        renderer.openPage(index).use { page ->
                            val scale = targetWidthPx.toFloat() / page.width
                            val bitmap = Bitmap.createBitmap(
                                targetWidthPx,
                                (page.height * scale).toInt().coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap
                        }
                    }
                }
            }
        }
}