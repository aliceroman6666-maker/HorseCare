package com.horsecare.app

import android.app.Application
import com.horsecare.app.data.HorseCareDatabase
import com.horsecare.app.data.repository.HorseCareRepository

class HorseCareApp : Application() {

    lateinit var repository: HorseCareRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = HorseCareDatabase.getInstance(this)
        repository = HorseCareRepository(
            horseDao = db.horseDao(),
            trainingSessionDao = db.trainingSessionDao(),
            healthRecordDao = db.healthRecordDao(),
            contactDao = db.contactDao(),
            reminderDao = db.reminderDao(),
            documentDao = db.documentDao()
        )
    }
}