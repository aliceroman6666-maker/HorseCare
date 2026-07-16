package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.TrainingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {

    @Query("SELECT * FROM training_sessions WHERE horseId = :horseId ORDER BY date DESC, createdAt DESC")
    fun getSessionsForHorse(horseId: Long): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE horseId = :horseId ORDER BY date DESC, createdAt DESC LIMIT 1")
    fun getLastSession(horseId: Long): Flow<TrainingSession?>

    @Query("""
        SELECT type FROM training_sessions
        WHERE horseId = :horseId
        AND type NOT IN (SELECT type FROM dismissed_training_types WHERE horseId = :horseId)
        GROUP BY type
        ORDER BY COUNT(*) DESC, MAX(date) DESC
        LIMIT 10
    """)
    fun getFrequentTrainingTypes(horseId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TrainingSession): Long

    @Delete
    suspend fun delete(session: TrainingSession)

    @Query("DELETE FROM training_sessions WHERE horseId = :horseId")
    suspend fun deleteAllForHorse(horseId: Long)
}