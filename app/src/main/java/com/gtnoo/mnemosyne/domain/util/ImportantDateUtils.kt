package com.gtnoo.mnemosyne.domain.util

import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.Person
import com.gtnoo.mnemosyne.domain.model.UpcomingImportantDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ImportantDateUtils {

    fun nextOccurrence(date: ImportantDate, from: LocalDate = LocalDate.now()): LocalDate? {
        val stored = date.date ?: return null
        if (date.isRecurring) {
            var candidate = stored.withYear(from.year)
            if (candidate.isBefore(from)) {
                candidate = candidate.plusYears(1)
            }
            return candidate
        }
        return if (!stored.isBefore(from)) stored else null
    }

    fun isNotificationDue(date: ImportantDate, today: LocalDate = LocalDate.now()): Boolean {
        if (!date.notifyEnabled || date.date == null) return false
        val occurrence = if (date.isRecurring) {
            val stored = date.date
            var candidate = stored.withYear(today.year)
            if (candidate.isBefore(today)) candidate = candidate.plusYears(1)
            candidate
        } else {
            date.date
        }
        if (occurrence.isBefore(today)) return false
        val reminderDate = occurrence.minusDays(date.notifyDaysBefore.toLong())
        return today == reminderDate
    }

    fun buildUpcoming(
        dates: List<ImportantDate>,
        people: List<Person>,
        withinDays: Int,
        from: LocalDate = LocalDate.now()
    ): List<UpcomingImportantDate> {
        val peopleMap = people.associateBy { it.id }
        return dates.mapNotNull { date ->
            val person = peopleMap[date.personId] ?: return@mapNotNull null
            val next = nextOccurrence(date, from) ?: return@mapNotNull null
            val daysUntil = ChronoUnit.DAYS.between(from, next).toInt()
            if (daysUntil in 0..withinDays) {
                UpcomingImportantDate(
                    importantDate = date,
                    person = person,
                    nextOccurrence = next,
                    daysUntil = daysUntil
                )
            } else null
        }.sortedBy { it.nextOccurrence }
    }
}
