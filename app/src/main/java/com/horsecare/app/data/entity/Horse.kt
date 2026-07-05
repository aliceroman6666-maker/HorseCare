package com.horsecare.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.Period

@Entity(tableName = "horses")
data class Horse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breed: String,
    val birthDate: LocalDate,
    val sex: HorseSex,
    val color: String,
    val chipNumber: String? = null,
    val photoUri: String? = null,
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val markings: String? = null,
    val acquiredDate: LocalDate? = null,
    val sireName: String? = null,
    val damName: String? = null,
    val createdAt: Instant = Instant.now()
) {
    val ageYears: Int
        get() = Period.between(birthDate, LocalDate.now()).years

    /**
     * Скільки часу минуло з дати "Зі мною з" - роки та місяці.
     * Повертає null, якщо дата не вказана.
     */
    val ownershipPeriod: Period?
        get() = acquiredDate?.let { Period.between(it, LocalDate.now()) }
}