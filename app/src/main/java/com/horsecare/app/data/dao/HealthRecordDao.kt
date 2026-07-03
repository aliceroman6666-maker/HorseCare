package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Query("SELECT * FROM health_records WHERE horseId = :horseId ORDER BY date DESC")
    fun getRecordsForHorse(horseId: Long): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE horseId = :horseId AND type = :type ORDER BY date DESC")
    fun getRecordsByType(horseId: Long, type: String): Flow<List<HealthRecord>>

    // Для кожного типу - найновіший запис (щоб визначити чи прострочено nextDueDate)
    @Query("""
        SELECT hr.* FROM health_records hr
        INNER JOIN (
            SELECT type, MAX(date) as maxDate
            FROM health_records
            WHERE horseId = :horseId
            GROUP BY type
        ) latest ON hr.type = latest.type AND hr.date = latest.maxDate
        WHERE hr.horseId = :horseId
    """)
    fun getLatestRecordPerType(horseId: Long): Flow<List<HealthRecord>>

    // Autocomplete: найчастіші назви (вакцини/препарати) для конкретного типу запису
    @Query("""
        SELECT name FROM health_records
        WHERE horseId = :horseId AND type = :type
        GROUP BY name
        ORDER BY COUNT(*) DESC, MAX(date) DESC
        LIMIT 10
    """)
    fun getFrequentNamesForType(horseId: Long, type: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord): Long

    @Query("UPDATE health_records SET nextDueDate = :newDate WHERE id = :recordId")
    suspend fun updateNextDueDate(recordId: Long, newDate: String)
}