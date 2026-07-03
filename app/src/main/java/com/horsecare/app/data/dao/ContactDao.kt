package com.horsecare.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.horsecare.app.data.entity.Contact
import com.horsecare.app.data.entity.ContactRole
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts WHERE role = :role ORDER BY fullName ASC")
    fun getContactsByRole(role: ContactRole): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY fullName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long
}