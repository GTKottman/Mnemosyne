package com.gtnoo.mnemosyne.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.gtnoo.mnemosyne.domain.model.Medicine
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Medicine Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to take your medicines"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminder(medicine: Medicine, notificationId: Int) {
        ensureChannel()

        val timeText = medicine.reminderTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: ""
        val bodyText = buildString {
            if (medicine.notes.isNotBlank()) append(medicine.notes)
            else append("Time to take your ${medicine.name}.")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to take ${medicine.name}")
            .setContentText(bodyText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (timeText.isNotBlank()) {
            builder.setSubText(timeText)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
    }
}
