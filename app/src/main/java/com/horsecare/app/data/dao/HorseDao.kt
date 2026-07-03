package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.horsecare.app.data.entity.Horse
import kotlinx.coroutines.flow.Flow

@Dao
interface HorseDao {

    @Query("SELECT * FROM horses ORDER BY createdAt ASC")
    fun getAllHorses(): Flow<List<Horse>>

    @Query("SELECT * FROM horses WHERE id = :horseId")
    fun getHorseById(horseId: Long): Flow<Horse?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(horse: Horse): Long

    @Update
    suspend fun update(horse: Horse)

    @Delete
    suspend fun delete(horse: Horse)
}