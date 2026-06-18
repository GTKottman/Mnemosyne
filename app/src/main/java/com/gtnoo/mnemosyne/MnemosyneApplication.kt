package com.gtnoo.mnemosyne

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.gtnoo.mnemosyne.notification.ImportantDateNotificationHelper
import com.gtnoo.mnemosyne.notification.ImportantDateScheduler
import com.gtnoo.mnemosyne.notification.MedicineReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MnemosyneApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        ImportantDateScheduler.schedule(this)
        MedicineReminderScheduler.schedule(this)
    }
}
