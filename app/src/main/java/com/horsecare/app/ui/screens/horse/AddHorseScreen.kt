package com.horsecare.app.ui.screens.horse

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
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
    onDelete: (() -> Unit)? = null,
    documentsCount: Int = 0,
    onOpenDocuments: (() -> Unit)? = null,
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