package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.DismissedTrainingType

@Dao
interface DismissedTrainingTypeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DismissedTrainingType)
}