package com.horsecare.app.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Планує нотифікацію на 9:00 ранку дати triggerDate.
     * Якщо дата вже минула - нотифікація спрацює майже одразу (WorkManager не дозволяє
     * від'ємну затримку, тому це edge-case на момент першого запуску після пропуску).
     */
    fun schedule(
        context: Context,
        uniqueWorkName: String,
        title: String,
        message: String,
        notificationId: Int,
        triggerDate: LocalDate
    ) {
        val triggerAtMillis = triggerDate
            .atTime(LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0)

        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_MESSAGE, message)
            .putInt(ReminderWorker.KEY_NOTIFICATION_ID, notificationId)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueWorkName, androidx.work.ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, uniqueWorkName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
    }
}