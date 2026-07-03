package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horseId: Long,
    val date: LocalDate,
    val type: String,
    val durationMinutes: Int,
    val notes: String? = null,
    val horseCondition: HorseConditionAfterTraining? = null,
    val createdAt: Instant = Instant.now()
)