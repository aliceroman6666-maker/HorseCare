package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val healthRecordId: Long,
    val triggerDate: LocalDate,
    val type: ReminderType,
    val isSent: Boolean = false
)