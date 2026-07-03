package com.horsecare.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.horsecare.app.data.entity.HealthRecord
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.entity.TrainingSession
import com.horsecare.app.data.repository.OverdueHealthItem
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddHorseClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMarkDone: (HealthRecord) -> Unit,
    onReschedule: (HealthRecord) -> Unit,
    onOpenHealth: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenFeeding: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val horse = uiState.horse

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HorseCare") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Меню")
                    }
                },
                actions = {
                    IconButton(onClick = onAddHorseClick) {
                        Icon(Icons.Default.Add, contentDescription = "Додати коня")
                    }
                }
            )
        }
    ) { padding ->
        if (horse == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Додайте свого першого коня")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScrollWorkaround()
        ) {
            // Банер прострочень - завжди зверху, перекриває звичайну "найближчу подію"
            if (uiState.overdueItems.isNotEmpty()) {
                OverdueBanner(
                    items = uiState.overdueItems,
                    onMarkDone = onMarkDone,
                    onReschedule = onReschedule
                )
            }

            AsyncImage(
                model = horse.photoUri,
                contentDescription = horse.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            HorseInfoCard(horse = horse)

            if (uiState.overdueItems.isEmpty() && uiState.nearestUpcoming != null) {
                UpcomingEventCard(uiState.nearestUpcoming)
            }

            uiState.lastTraining?.let { LastTrainingCard(it) }

            SectionGrid(
                onOpenHealth = onOpenHealth,
                onOpenTraining = onOpenTraining,
                onOpenFeeding = onOpenFeeding,
                onOpenProfile = onOpenProfile
            )
        }
    }
}

// Проста обгортка, щоб не тягнути окремий імпорт verticalScroll в кожен файл
@Composable
private fun Modifier.verticalScrollWorkaround(): Modifier {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    return this.then(androidx.compose.foundation.verticalScroll(scrollState))
}

@Composable
private fun OverdueBanner(
    items: List<OverdueHealthItem>,
    onMarkDone: (HealthRecord) -> Unit,
    onReschedule: (HealthRecord) -> Unit
) {
    val top = items.first()
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (items.size > 1) "ПРОСТРОЧЕНО (${items.size})" else "ПРОСТРОЧЕНО",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("${top.record.type.displayName} — мало бути ${top.record.nextDueDate?.format(dateFormatter)}")
            Spacer(Modifier.height(8.dp))
            Row {
                TextButton(onClick = { onMarkDone(top.record) }) {
                    Text("Відмітити виконано")
                }
                TextButton(onClick = { onReschedule(top.record) }) {
                    Text("Перенести")
                }
            }
        }
    }
}

@Composable
private fun HorseInfoCard(horse: Horse) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text(horse.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${horse.breed}, ${horse.ageYears} років", style = MaterialTheme.typography.bodyMedium)
        Text("${horse.sex.displayName} · ${horse.color}", style = MaterialTheme.typography.bodyMedium)

        AnimatedVisibilityColumn(visible = expanded) {
            Spacer(Modifier.height(8.dp))
            horse.chipNumber?.let { Text("Номер чипу: $it") }
            horse.heightCm?.let { Text("Зріст: $it см") }
            horse.weightKg?.let { Text("Вага: $it кг") }
            horse.markings?.let { Text("Прикмети: $it") }
            horse.acquiredDate?.let { Text("Зі мною з: ${it.format(dateFormatter)}") }
            if (horse.sireName != null || horse.damName != null) {
                Text("Батько: ${horse.sireName ?: "—"} / Мати: ${horse.damName ?: "—"}")
            }
        }

        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Згорнути ▴" else "Показати більше ▾")
        }
    }
}

@Composable
private fun AnimatedVisibilityColumn(visible: Boolean, content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.animation.AnimatedVisibility(visible = visible) {
        Column(content = content)
    }
}

@Composable
private fun UpcomingEventCard(record: HealthRecord) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Найближча подія", style = MaterialTheme.typography.labelMedium)
            Text("${record.type.displayName} — ${record.nextDueDate?.format(dateFormatter)}")
        }
    }
}

@Composable
private fun LastTrainingCard(session: TrainingSession) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("Останнє тренування", style = MaterialTheme.typography.labelMedium)
            Text("${session.type}, ${session.durationMinutes} хв — ${session.date.format(dateFormatter)}")
        }
    }
}

@Composable
private fun SectionGrid(
    onOpenHealth: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenFeeding: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionButton("Здоров'я", Modifier.weight(1f), onOpenHealth)
            SectionButton("Тренування", Modifier.weight(1f), onOpenTraining)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionButton("Годування", Modifier.weight(1f), onOpenFeeding)
            SectionButton("Профіль", Modifier.weight(1f), onOpenProfile)
        }
    }
}

@Composable
private fun SectionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(56.dp)) {
        Text(label)
    }
}