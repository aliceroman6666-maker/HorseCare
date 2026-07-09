package com.horsecare.app.data.repository

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
import com.horsecare.app.data.entity.ReminderType
import com.horsecare.app.data.entity.TrainingSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Прострочений запис здоров'я - обгортка для показу банера на головному екрані.
 */
data class OverdueHealthItem(
    val record: HealthRecord,
    val daysOverdue: Long
)

class HorseCareRepository(
    private val horseDao: HorseDao,
    private val trainingSessionDao: TrainingSessionDao,
    private val healthRecordDao: HealthRecordDao,
    private val contactDao: ContactDao,
    private val reminderDao: ReminderDao,
    private val documentDao: DocumentDao
) {
    // --- Horse ---
    fun getAllHorses(): Flow<List<Horse>> = horseDao.getAllHorses()
    fun getHorseById(id: Long): Flow<Horse?> = horseDao.getHorseById(id)
    suspend fun saveHorse(horse: Horse): Long = horseDao.insert(horse)
    suspend fun updateHorse(horse: Horse) = horseDao.update(horse)

    /**
     * Повністю видаляє коня разом з усіма пов'язаними записами
     * (тренування, здоров'я, нагадування, документи), щоб не лишати "сирітських" даних.
     */
    suspend fun deleteHorseCompletely(horse: Horse) {
        reminderDao.deleteAllForHorse(horse.id)
        healthRecordDao.deleteAllForHorse(horse.id)
        trainingSessionDao.deleteAllForHorse(horse.id)
        documentDao.deleteAllForHorse(horse.id)
        horseDao.delete(horse)
    }

    // --- Training ---
    fun getSessionsForHorse(horseId: Long): Flow<List<TrainingSession>> =
        trainingSessionDao.getSessionsForHorse(horseId)

    fun getLastSession(horseId: Long): Flow<TrainingSession?> =
        trainingSessionDao.getLastSession(horseId)

    fun getFrequentTrainingTypes(horseId: Long): Flow<List<String>> =
        trainingSessionDao.getFrequentTrainingTypes(horseId)

    suspend fun saveTrainingSession(session: TrainingSession): Long =
        trainingSessionDao.insert(session)

    // --- Health ---
    fun getHealthRecords(horseId: Long): Flow<List<HealthRecord>> =
        healthRecordDao.getRecordsForHorse(horseId)

    fun getLatestRecordPerType(horseId: Long): Flow<List<HealthRecord>> =
        healthRecordDao.getLatestRecordPerType(horseId)

    fun getFrequentNamesForType(horseId: Long, type: String): Flow<List<String>> =
        healthRecordDao.getFrequentNamesForType(horseId, type)

    suspend fun saveHealthRecordWithReminders(record: HealthRecord): Long {
        val recordId = healthRecordDao.insert(record)
        record.nextDueDate?.let { dueDate ->
            reminderDao.deleteForRecord(recordId)
            reminderDao.insert(
                Reminder(
                    healthRecordId = recordId,
                    triggerDate = dueDate.minusDays(3),
                    type = ReminderType.THREE_DAYS_BEFORE
                )
            )
            reminderDao.insert(
                Reminder(
                    healthRecordId = recordId,
                    triggerDate = dueDate,
                    type = ReminderType.ON_DUE_DATE
                )
            )
        }
        return recordId
    }

    suspend fun rescheduleHealthRecord(recordId: Long, newDate: LocalDate) {
        healthRecordDao.updateNextDueDate(recordId, newDate.toString())
        reminderDao.deleteForRecord(recordId)
        reminderDao.insert(
            Reminder(
                healthRecordId = recordId,
                triggerDate = newDate.minusDays(3),
                type = ReminderType.THREE_DAYS_BEFORE
            )
        )
        reminderDao.insert(
            Reminder(
                healthRecordId = recordId,
                triggerDate = newDate,
                type = ReminderType.ON_DUE_DATE
            )
        )
    }

    // --- Contacts ---
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()
    suspend fun saveContact(contact: Contact): Long = contactDao.insert(contact)

    // --- Documents ---
    fun getDocuments(horseId: Long): Flow<List<Document>> = documentDao.getDocumentsForHorse(horseId)
    suspend fun getDocumentById(id: Long): Document? = documentDao.getById(id)
    suspend fun saveDocument(document: Document): Long = documentDao.insert(document)
    suspend fun deleteDocument(document: Document) = documentDao.delete(document)
}