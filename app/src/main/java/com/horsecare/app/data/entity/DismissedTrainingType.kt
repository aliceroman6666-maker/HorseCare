package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dismissed_training_types",
    indices = [Index(value = ["horseId", "type"], unique = true)]
)
data class DismissedTrainingType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horseId: Long,
    val type: String
)