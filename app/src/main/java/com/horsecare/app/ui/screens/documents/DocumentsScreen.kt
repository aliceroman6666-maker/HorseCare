package com.horsecare.app.ui.screens.documents

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horsecare.app.data.entity.Document
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<Document>,
    onBack: () -> Unit,
    onOpenDocument: (Document) -> Unit,
    onAddDocument: (title: String, uris: List<String>, mimeType: String?) -> Unit,
    onDeleteDocument: (Document) -> Unit
) {
    val context = LocalContext.current

    var showAddOptions by remember { mutableStateOf(false) }
    var pendingUris by remember { mutableStateOf<List<Uri>?>(null) }
    var pendingMimeType by remember { mutableStateOf<String?>(null) }
    var pendingTitle by remember { mutableStateOf("") }
    var documentPendingDelete by remember { mutableStateOf<Document?>(null) }

    // --- Декілька фото як один документ (наприклад, розворот паспорта) ---
    val multiPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            pendingUris = uris
            pendingMimeType = "image/*"
            pendingTitle = ""
        }
    }

    // --- Один PDF-файл ---
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // деякі провайдери не підтримують persistable permission - ігноруємо
            }
            pendingUris = listOf(it)
            pendingMimeType = "application/pdf"
            pendingTitle = queryDisplayName(context, it) ?: "Документ"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Документи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptions = true },
                icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                text = { Text("Додати документ") }
            )
        }
    ) { padding ->
        if (documents.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Тут з'являться скани паспорта, довідок та інших документів коня",
                    modifier = Modifier.padding(32.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documents, key = { it.id }) { doc ->
                    DocumentRow(
                        document = doc,
                        onOpen = { onOpenDocument(doc) },
                        onDelete = { documentPendingDelete = doc }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) } // місце під FAB
            }
        }
    }

    // --- Вибір типу файлу для додавання ---
    if (showAddOptions) {
        AlertDialog(
            onDismissRequest = { showAddOptions = false },
            title = { Text("Що додати?") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showAddOptions = false
                            multiPhotoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Фото (можна декілька)", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                    }
                    TextButton(
                        onClick = {
                            showAddOptions = false
                            pdfLauncher.launch(arrayOf("application/pdf"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("PDF-файл", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddOptions = false }) { Text("Скасувати") }
            }
        )
    }

    // --- Підпис назви документа перед збереженням ---
    if (pendingUris != null) {
        AlertDialog(
            onDismissRequest = { pendingUris = null },
            title = { Text("Назва документа") },
            text = {
                Column {
                    Text(
                        "Наприклад: \"Паспорт коня\", \"Довідка від ветеринара\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pendingTitle,
                        onValueChange = { pendingTitle = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uris = pendingUris
                        if (uris != null) {
                            val title = pendingTitle.trim().ifBlank { "Документ" }
                            onAddDocument(title, uris.map { it.toString() }, pendingMimeType)
                        }
                        pendingUris = null
                    }
                ) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUris = null }) { Text("Скасувати") }
            }
        )
    }

    // --- Підтвердження видалення ---
    documentPendingDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentPendingDelete = null },
            title = { Text("Видалити документ?") },
            text = { Text("Ви дійсно хочете видалити \"${doc.title}\"? Цю дію не можна скасувати.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDocument(doc)
                        documentPendingDelete = null
                    }
                ) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentPendingDelete = null }) { Text("Скасувати") }
            }
        )
    }
}

@Composable
private fun DocumentRow(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    document.mimeType?.startsWith("image/") == true -> Icons.Default.Image
                    document.mimeType == "application/pdf" -> Icons.Default.PictureAsPdf
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(document.title, style = MaterialTheme.typography.bodyLarge)
                val pagesInfo = if (document.uriList.size > 1) " · ${document.uriList.size} стор." else ""
                Text(
                    document.addedDate.format(dateFormatter) + pagesInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Видалити")
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    } catch (e: Exception) {
        null
    }
}