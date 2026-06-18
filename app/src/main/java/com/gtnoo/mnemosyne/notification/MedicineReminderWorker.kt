package com.gtnoo.mnemosyne.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gtnoo.mnemosyne.domain.repository.MedicineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MedicineReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicineRepository: MedicineRepository,
    private val notificationHelper: MedicineNotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationHelper.ensureChannel()
        val medicines = medicineRepository.getAll()
        medicines
            .filter { it.isActive && it.notifyEnabled }
            .forEach { medicine ->
                val notificationId = medicine.id.hashCode()
                notificationHelper.showReminder(medicine, notificationId)
            }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "medicine_reminder_check"
    }
}

object MedicineReminderScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<MedicineReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MedicineReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
