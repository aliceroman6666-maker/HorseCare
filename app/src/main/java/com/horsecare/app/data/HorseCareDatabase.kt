package com.horsecare.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.horsecare.app.data.dao.ContactDao
import com.horsecare.app.data.dao.DocumentDao
import com.horsecare.app.data.dao.HealthRecordDao
import com.horsecare.app.data.dao.HorseDao
import com.horsecare.app.data.dao.ReminderDao
import com.horsecare.app.data.dao.TrainingSessionDao
import com.horsecare.app.data.entity.Contact
import com.horsecare.app.data.entity.Document
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
        Reminder::class,
        Document::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HorseCareDatabase : RoomDatabase() {

    abstract fun horseDao(): HorseDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun contactDao(): ContactDao
    abstract fun reminderDao(): ReminderDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: HorseCareDatabase? = null

        // Додає таблицю documents, не чіпаючи вже наявні дані (коня, тренування тощо).
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS documents (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        horseId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        uri TEXT NOT NULL,
                        mimeType TEXT,
                        addedDate TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): HorseCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HorseCareDatabase::class.java,
                    "horsecare.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}