package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horseId: Long,
    val type: HealthRecordType,
    val date: LocalDate,
    val nextDueDate: LocalDate? = null,
    val name: String,
    val hoofCareTypes: Set<HoofCareType>? = null,
    val contactId: Long? = null,
    val notes: String? = null,
    val createdAt: Instant = Instant.now()
)