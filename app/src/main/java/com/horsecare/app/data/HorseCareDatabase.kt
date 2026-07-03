package com.horsecare.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.horsecare.app.data.dao.ContactDao
import com.horsecare.app.data.dao.HealthRecordDao
import com.horsecare.app.data.dao.HorseDao
import com.horsecare.app.data.dao.ReminderDao
import com.horsecare.app.data.dao.TrainingSessionDao
import com.horsecare.app.data.entity.Contact
import com.horsecare.app.data.entity.HealthRecord
import com.horsecare.app.data.entity.Horse
import com.horsecare.app.data.entity.Reminder
import com.horsecare.app.data.entity.TrainingSession

@Database(
    entities = [
        Horse::class,
        TrainingSession::class,
        HealthRecord::class,
        Contact::class,
        Reminder::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HorseCareDatabase : RoomDatabase() {

    abstract fun horseDao(): HorseDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun contactDao(): ContactDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: HorseCareDatabase? = null

        fun getInstance(context: Context): HorseCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HorseCareDatabase::class.java,
                    "horsecare.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}