package com.gtnoo.mnemosyne.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.gtnoo.mnemosyne.MainActivity
import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.Person
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportantDateNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Important Dates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for birthdays and other important dates"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminder(
        importantDate: ImportantDate,
        person: Person,
        notificationId: Int
    ) {
        ensureChannel()

        val title = "${importantDate.label}: ${person.displayName}"
        val daysText = when (importantDate.notifyDaysBefore) {
            0 -> "Today is ${importantDate.label.lowercase()}!"
            1 -> "${importantDate.label} is tomorrow"
            else -> "${importantDate.label} in ${importantDate.notifyDaysBefore} days"
        }
        val content = if (importantDate.remindToLogInteraction) {
            "$daysText Tap to view or log an interaction."
        } else {
            daysText
        }

        val detailIntent = PendingIntent.getActivity(
            context,
            notificationId,
            MainActivity.createIntent(
                context = context,
                personId = person.id
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(detailIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (importantDate.remindToLogInteraction) {
            val interactionIntent = PendingIntent.getActivity(
                context,
                notificationId + INTERACTION_REQUEST_OFFSET,
                MainActivity.createIntent(
                    context = context,
                    personId = person.id,
                    openInteraction = true
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_input_add,
                "Log Interaction",
                interactionIntent
            )
        }

        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "important_dates"
        const val EXTRA_PERSON_ID = "extra_person_id"
        const val EXTRA_OPEN_INTERACTION = "extra_open_interaction"
        private const val INTERACTION_REQUEST_OFFSET = 100_000
    }
}
