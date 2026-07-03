package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: ContactRole,
    val fullName: String,
    val phone: String? = null,
    val notes: String? = null
)