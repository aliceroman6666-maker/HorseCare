package com.horsecare.app.ui.screens.horse

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.entity.HorseSex
import com.horsecare.app.util.ImageCropUtil
import com.horsecare.app.util.PhotoFileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHorseScreen(
    initialHorse: Horse? = null,
    onBack: () -> Unit,
    onSave: (
        name: String,
        breed: String,
        birthDate: LocalDate,
        sex: HorseSex,
        color: String,
        chipNumber: String,
        photoUri: String?,
        heightCm: Int?,
        weightKg: Double?,
        markings: String?,
        acquiredDate: LocalDate?,
        sireName: String?,
        damName: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<Uri?>(initialHorse?.photoUri?.let { Uri.parse(it) }) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var zoom by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(photoUri) {
        zoom = 1f
        panOffset = Offset.Zero
    }

    var name by remember { mutableStateOf(initialHorse?.name ?: "") }
    var breed by remember { mutableStateOf(initialHorse?.breed ?: "") }
    var birthDate by remember { mutableStateOf(initialHorse?.birthDate) }
    var sex by remember { mutableStateOf(initialHorse?.sex) }
    var color by remember { mutableStateOf(initialHorse?.color ?: "") }
    var chipNumber by remember { mutableStateOf(initialHorse?.chipNumber ?: "") }

    var expanded by remember { mutableStateOf(initialHorse != null) }
    var heightCm by remember { mutableStateOf(initialHorse?.heightCm?.toString() ?: "") }
    var weightKg by remember { mutableStateOf(initialHorse?.weightKg?.toString() ?: "") }
    var markings by remember { mutableStateOf(initialHorse?.markings ?: "") }
    var acquiredDate by remember { mutableStateOf(initialHorse?.acquiredDate) }
    var sireName by remember { mutableStateOf(initialHorse?.sireName ?: "") }
    var damName by remember { mutableStateOf(initialHorse?.damName ?: "") }

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showAcquiredDatePicker by remember { mutableStateOf(false) }
    var sexMenuExpanded by remember { mutableStateOf(false) }

    // --- Вибір фото з галереї (системний Photo Picker, дозволи не потрібні) ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { photoUri = it } }

    // --- Зйомка фото камерою ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = pendingCameraUri
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = PhotoFileUtil.createHorsePhotoUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = PhotoFileUtil.createHorsePhotoUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val isFormValid = name.isNotBlank() && breed.isNotBlank() && birthDate != null &&
            sex != null && color.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialHorse != null) "Редагувати коня" else "Новий кінь") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- Фото ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .then(
                            if (photoUri != null) {
                                Modifier.pointerInput(photoUri) {
                                    detectTransformGestures { _, pan, gestureZoom, _ ->
                                        zoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                        panOffset = Offset(
                                            panOffset.x + pan.x / size.width,
                                            panOffset.y + pan.y / size.height
                                        )
                                    }
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Фото коня",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoom
                                    scaleY = zoom
                                    translationX = panOffset.x * size.width
                                    translationY = panOffset.y * size.height
                                }
                        )
                    } else {
                        Text(
                            "Немає фото",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            if (photoUri != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ущипніть, щоб наблизити, тягніть - щоб перемістити",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (zoom != 1f || panOffset != Offset.Zero) {
                    TextButton(
                        onClick = { zoom = 1f; panOffset = Offset.Zero },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Скинути масштаб")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Галерея")
                }
                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Камера")
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Обов'язкові поля ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Кличка *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Порода *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = birthDate?.format(dateFormatter) ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата народження *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBirthDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = sexMenuExpanded,
                onExpandedChange = { sexMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = sex?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Стать *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = sexMenuExpanded,
                    onDismissRequest = { sexMenuExpanded = false }
                ) {
                    HorseSex.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                sex = option
                                sexMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Масть *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = chipNumber,
                onValueChange = { chipNumber = it },
                label = { Text("Номер чипу/паспорта") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // --- Довідкові поля ---
            AnimatedVisibility(visible = expanded) {
                Column {
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it.filter { c -> c.isDigit() } },
                        label = { Text("Зріст у холці (см)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Вага (кг)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = markings,
                        onValueChange = { markings = it },
                        label = { Text("Особливі прикмети") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = acquiredDate?.format(dateFormatter) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Зі мною з") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAcquiredDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sireName,
                        onValueChange = { sireName = it },
                        label = { Text("Кличка батька") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = damName,
                        onValueChange = { damName = it },
                        label = { Text("Кличка матері") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Згорнути ▴" else "Показати більше ▾")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val finalPhotoUri = photoUri?.let { uri ->
                            withContext(Dispatchers.IO) {
                                ImageCropUtil.cropAndSave(context, uri, zoom, panOffset)
                            }
                        }
                        onSave(
                            name.trim(),
                            breed.trim(),
                            birthDate!!,
                            sex!!,
                            color.trim(),
                            chipNumber.trim(),
                            (finalPhotoUri ?: photoUri)?.toString(),
                            heightCm.toIntOrNull(),
                            weightKg.toDoubleOrNull(),
                            markings.trim(),
                            acquiredDate,
                            sireName.trim(),
                            damName.trim()
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initialHorse != null) "Зберегти зміни" else "Зберегти")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBirthDatePicker) {
        DatePickerModal(
            onDateSelected = { birthDate = it; showBirthDatePicker = false },
            onDismiss = { showBirthDatePicker = false }
        )
    }
    if (showAcquiredDatePicker) {
        DatePickerModal(
            onDateSelected = { acquiredDate = it; showAcquiredDatePicker = false },
            onDismiss = { showAcquiredDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    onDateSelected(date)
                } ?: onDismiss()
            }) {
                Text("Обрати")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}