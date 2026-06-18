package com.gtnoo.mnemosyne.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gtnoo.mnemosyne.domain.repository.InteractionRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class InteractionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val personRepository: PersonRepository,
    private val interactionRepository: InteractionRepository,
    private val notificationHelper: InteractionReminderNotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationHelper.ensureChannel()

        val today = LocalDate.now()
        val now = LocalTime.now()
        val todaysInteractions = interactionRepository.getByDate(today)
        val peopleLoggedToday = todaysInteractions.map { it.person.id }.toSet()

        personRepository.getAll()
            .filter { person ->
                person.interactionReminderEnabled &&
                    person.id !in peopleLoggedToday &&
                    (person.interactionReminderTime == null || !now.isBefore(person.interactionReminderTime))
            }
            .forEach { person ->
                notificationHelper.showReminder(person, person.id.hashCode())
            }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "interaction_reminder_check"
    }
}

object InteractionReminderScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<InteractionReminderWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            InteractionReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
