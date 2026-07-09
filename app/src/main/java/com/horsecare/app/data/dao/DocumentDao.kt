package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE horseId = :horseId ORDER BY addedDate DESC, id DESC")
    fun getDocumentsForHorse(horseId: Long): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: Document): Long

    @Delete
    suspend fun delete(document: Document)

    @Query("DELETE FROM documents WHERE horseId = :horseId")
    suspend fun deleteAllForHorse(horseId: Long)
}