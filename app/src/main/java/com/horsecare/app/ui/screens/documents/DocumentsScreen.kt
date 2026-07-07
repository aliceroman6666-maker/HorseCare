package com.horsecare.app.ui.screens.documents

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.horsecare.app.data.entity.Document
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<Document>,
    onBack: () -> Unit,
    onAddDocument: (title: String, uri: String, mimeType: String?) -> Unit,
    onDeleteDocument: (Document) -> Unit
) {
    val context = LocalContext.current

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingMimeType by remember { mutableStateOf<String?>(null) }
    var pendingTitle by remember { mutableStateOf("") }

    val pickDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Зберігаємо доступ до файлу назавжди, а не тільки на цю сесію
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // деякі провайдери не підтримують persistable permission - ігноруємо
            }
            pendingUri = it
            pendingMimeType = context.contentResolver.getType(it)
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
                onClick = {
                    pickDocumentLauncher.launch(arrayOf("image/*", "application/pdf"))
                },
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                        onOpen = { openDocument(context, doc) },
                        onDelete = { onDeleteDocument(doc) }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) } // місце під FAB
            }
        }
    }

    if (pendingUri != null) {
        AlertDialog(
            onDismissRequest = { pendingUri = null },
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
                        val uri = pendingUri
                        if (uri != null) {
                            val title = pendingTitle.trim().ifBlank { "Документ" }
                            onAddDocument(title, uri.toString(), pendingMimeType)
                        }
                        pendingUri = null
                    }
                ) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUri = null }) {
                    Text("Скасувати")
                }
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
                imageVector = if (document.mimeType?.startsWith("image/") == true) {
                    Icons.Default.Image
                } else {
                    Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(document.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    document.addedDate.format(dateFormatter),
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

private fun openDocument(context: android.content.Context, document: Document) {
    try {
        val uri = Uri.parse(document.uri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, document.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Немає застосунку, що вміє відкрити цей тип файлу - мовчки ігноруємо
    }
}