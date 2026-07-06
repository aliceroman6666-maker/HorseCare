package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE healthRecordId = :healthRecordId")
    suspend fun getRemindersForRecord(healthRecordId: Long): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE healthRecordId = :healthRecordId")
    suspend fun deleteForRecord(healthRecordId: Long)

    @Query("DELETE FROM reminders WHERE healthRecordId IN (SELECT id FROM health_records WHERE horseId = :horseId)")
    suspend fun deleteAllForHorse(horseId: Long)
}