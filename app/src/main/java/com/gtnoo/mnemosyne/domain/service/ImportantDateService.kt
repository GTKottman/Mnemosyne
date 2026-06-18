package com.gtnoo.mnemosyne.domain.service

import com.gtnoo.mnemosyne.domain.model.ImportantDate
import com.gtnoo.mnemosyne.domain.model.ImportantDateKind
import com.gtnoo.mnemosyne.domain.model.UpcomingImportantDate
import com.gtnoo.mnemosyne.domain.repository.ImportantDateRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.util.ImportantDateUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ImportantDateService @Inject constructor(
    private val importantDateRepository: ImportantDateRepository,
    private val personRepository: PersonRepository
) {
    suspend fun createDefaultBirthday(personId: String): ImportantDate {
        val existing = importantDateRepository.getBirthdayForPerson(personId)
        if (existing != null) return existing
        val birthday = ImportantDate(
            personId = personId,
            label = "Birthday",
            kind = ImportantDateKind.BIRTHDAY,
            isRecurring = true
        )
        return importantDateRepository.save(birthday)
    }

    suspend fun saveImportantDate(date: ImportantDate): ImportantDate =
        importantDateRepository.save(date)

    suspend fun deleteImportantDate(id: String) {
        val date = importantDateRepository.getById(id) ?: return
        if (date.kind == ImportantDateKind.BIRTHDAY) return
        importantDateRepository.delete(id)
    }

    suspend fun deleteAllForPerson(personId: String) {
        importantDateRepository.deleteAllForPerson(personId)
    }

    suspend fun getBirthdayForPerson(personId: String): ImportantDate? =
        importantDateRepository.getBirthdayForPerson(personId)

    suspend fun updateBirthday(personId: String, date: LocalDate?) {
        val birthday = importantDateRepository.getBirthdayForPerson(personId)
            ?: createDefaultBirthday(personId)
        importantDateRepository.save(birthday.copy(date = date))
    }

    fun observeByPersonId(personId: String): Flow<List<ImportantDate>> =
        importantDateRepository.observeByPersonId(personId)

    fun observeUpcoming(withinDays: Int = 30): Flow<List<UpcomingImportantDate>> =
        importantDateRepository.observeUpcoming(withinDays)

    suspend fun getDatesDueForNotification(today: LocalDate = LocalDate.now()): List<ImportantDate> =
        importantDateRepository.getAllWithNotificationsEnabled()
            .filter { ImportantDateUtils.isNotificationDue(it, today) }

    suspend fun getUpcoming(withinDays: Int = 30): List<UpcomingImportantDate> {
        val dates = importantDateRepository.getAll().filter { it.date != null }
        val people = personRepository.getAll()
        return ImportantDateUtils.buildUpcoming(dates, people, withinDays)
    }
}
