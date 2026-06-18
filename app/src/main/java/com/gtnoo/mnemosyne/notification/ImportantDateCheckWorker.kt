package com.gtnoo.mnemosyne.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.service.ImportantDateService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ImportantDateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val importantDateService: ImportantDateService,
    private val personRepository: PersonRepository,
    private val notificationHelper: ImportantDateNotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationHelper.ensureChannel()
        val dueDates = importantDateService.getDatesDueForNotification()
        dueDates.forEach { date ->
            val person = personRepository.getById(date.personId) ?: return@forEach
            val notificationId = date.id.hashCode()
            notificationHelper.showReminder(date, person, notificationId)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "important_date_check"
    }
}

object ImportantDateScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ImportantDateCheckWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ImportantDateCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
