package com.horsecare.app.data

import androidx.room.TypeConverter
import com.horsecare.app.data.entity.ContactRole
import com.horsecare.app.data.entity.HealthRecordType
import com.horsecare.app.data.entity.HoofCareType
import com.horsecare.app.data.entity.HorseConditionAfterTraining
import com.horsecare.app.data.entity.HorseSex
import com.horsecare.app.data.entity.ReminderType
import java.time.Instant
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromHorseSex(value: HorseSex?): String? = value?.name

    @TypeConverter
    fun toHorseSex(value: String?): HorseSex? = value?.let { HorseSex.valueOf(it) }

    @TypeConverter
    fun fromContactRole(value: ContactRole?): String? = value?.name

    @TypeConverter
    fun toContactRole(value: String?): ContactRole? = value?.let { ContactRole.valueOf(it) }

    @TypeConverter
    fun fromHealthRecordType(value: HealthRecordType?): String? = value?.name

    @TypeConverter
    fun toHealthRecordType(value: String?): HealthRecordType? =
        value?.let { HealthRecordType.valueOf(it) }

    @TypeConverter
    fun fromReminderType(value: ReminderType?): String? = value?.name

    @TypeConverter
    fun toReminderType(value: String?): ReminderType? = value?.let { ReminderType.valueOf(it) }

    @TypeConverter
    fun fromHorseCondition(value: HorseConditionAfterTraining?): String? = value?.name

    @TypeConverter
    fun toHorseCondition(value: String?): HorseConditionAfterTraining? =
        value?.let { HorseConditionAfterTraining.valueOf(it) }

    @TypeConverter
    fun fromHoofCareTypeSet(value: Set<HoofCareType>?): String? =
        value?.joinToString(",") { it.name }

    @TypeConverter
    fun toHoofCareTypeSet(value: String?): Set<HoofCareType>? =
        value?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { HoofCareType.valueOf(it) }
            ?.toSet()
}