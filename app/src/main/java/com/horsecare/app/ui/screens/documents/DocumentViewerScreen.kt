package com.horsecare.app.ui.screens.documents

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.horsecare.app.data.entity.Document
import com.horsecare.app.util.PdfRenderUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    document: Document?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "Документ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                document == null -> CircularProgressIndicator()

                document.mimeType?.startsWith("image/") == true -> {
                    ImagePager(uris = document.uriList)
                }

                document.mimeType == "application/pdf" -> {
                    PdfPager(uri = document.uriList.first())
                }

                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Перегляд цього типу файлу не підтримується в застосунку")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(document.uriList.first()), document.mimeType ?: "*/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // немає застосунку, що вміє відкрити цей файл
                            }
                        }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Відкрити в іншому застосунку")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImagePager(uris: List<String>) {
    if (uris.isEmpty()) {
        Text("Немає фото")
        return
    }
    val pagerState = rememberPagerState(pageCount = { uris.size })
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            AsyncImage(
                model = uris[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (uris.size > 1) {
            Text(
                "${pagerState.currentPage + 1} / ${uris.size}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PdfPager(uri: String) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Bitmap>?>(null) }

    LaunchedEffect(uri) {
        pages = PdfRenderUtil.renderPages(context, Uri.parse(uri))
    }

    val currentPages = pages
    if (currentPages == null) {
        CircularProgressIndicator()
        return
    }
    if (currentPages.isEmpty()) {
        Text("Не вдалося відкрити PDF")
        return
    }

    val pagerState = rememberPagerState(pageCount = { currentPages.size })
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            Image(
                bitmap = currentPages[page].asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (currentPages.size > 1) {
            Text(
                "Сторінка ${pagerState.currentPage + 1} / ${currentPages.size}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp)
            )
        }
    }
}