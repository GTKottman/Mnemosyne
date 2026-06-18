package com.gtnoo.mnemosyne.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.gtnoo.mnemosyne.MainActivity
import com.gtnoo.mnemosyne.domain.model.Person
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionReminderNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Interaction Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to log interactions with people"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminder(person: Person, notificationId: Int) {
        ensureChannel()

        val timeText = person.interactionReminderTime
            ?.format(DateTimeFormatter.ofPattern("h:mm a"))
            ?: ""

        val contentText = buildString {
            append("Don't forget to log your interaction with ${person.displayName} today.")
            if (timeText.isNotBlank()) append(" (Reminder set for $timeText)")
        }

        val tapIntent = PendingIntent.getActivity(
            context,
            notificationId,
            MainActivity.createIntent(
                context = context,
                personId = person.id,
                openInteraction = true
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Log interaction: ${person.displayName}")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "interaction_reminders"
    }
}
