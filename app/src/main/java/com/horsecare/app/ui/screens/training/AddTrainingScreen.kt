package com.horsecare.app.ui.screens.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.horsecare.app.data.entity.HorseConditionAfterTraining
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddTrainingScreen(
    frequentTypes: List<String>,
    onBack: () -> Unit,
    onSave: (date: LocalDate, type: String, durationMinutes: Int, notes: String?, condition: HorseConditionAfterTraining?) -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var customType by remember { mutableStateOf("") }
    var showCustomField by remember { mutableStateOf(frequentTypes.isEmpty()) }

    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf<HorseConditionAfterTraining?>(null) }

    val effectiveType = if (showCustomField) customType.trim() else selectedType ?: ""
    val isFormValid = effectiveType.isNotBlank() && duration.toIntOrNull() != null && duration.toIntOrNull()!! > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нове тренування") },
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
            Text("Тип тренування", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            if (frequentTypes.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequentTypes.forEach { type ->
                        FilterChip(
                            selected = !showCustomField && selectedType == type,
                            onClick = {
                                showCustomField = false
                                selectedType = type
                            },
                            label = { Text(type) }
                        )
                    }
                    FilterChip(
                        selected = showCustomField,
                        onClick = { showCustomField = true; selectedType = null },
                        label = { Text("+ Своє") }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (showCustomField) {
                OutlinedTextField(
                    value = customType,
                    onValueChange = { customType = it },
                    label = { Text("Тип тренування *") },
                    placeholder = { Text("Наприклад: Робота на корделі") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = date.format(dateFormatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text("Тривалість (хв) *") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Нотатки (опційно)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Стан коня після (опційно)", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HorseConditionAfterTraining.entries.forEach { option ->
                    FilterChip(
                        selected = condition == option,
                        onClick = { condition = if (condition == option) null else option },
                        label = { Text("${option.emoji} ${option.displayName}") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(date, effectiveType, duration.toInt(), notes, condition)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зберегти")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Обрати")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Скасувати") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}