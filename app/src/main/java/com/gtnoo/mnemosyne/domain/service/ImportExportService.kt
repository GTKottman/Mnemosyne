package com.gtnoo.mnemosyne.domain.service

import android.content.Context
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ImportExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entryRepository: EntryRepository,
    private val personRepository: PersonRepository,
    private val placeRepository: PlaceRepository,
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository,
    private val interactionRepository: InteractionRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportFullBackup(): File {
        val backup = AppBackup(
            entries = entryRepository.getAll(),
            people = personRepository.getAll(),
            places = placeRepository.getAll(),
            weatherSnapshots = weatherRepository.getAll(),
            settings = settingsRepository.getSettings()
        )
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(context.getExternalFilesDir(null), "mnemosyne_backup_$timestamp.json")
        file.writeText(json.encodeToString(backup.toSerializable()))
        return file
    }

    suspend fun importFromJson(content: String): ImportResult {
        return try {
            val backup = json.decodeFromString<SerializableBackup>(content)
            var imported = 0
            var skipped = 0
            backup.people.forEach { personRepository.save(it.toDomain()) }
            backup.places.forEach { placeRepository.save(it.toDomain()) }
            backup.entries.forEach { serEntry ->
                try {
                    val people = personRepository.getAll()
                    val places = placeRepository.getAll()
                    val peopleMap = people.associateBy { it.id }
                    val entryDate = LocalDate.parse(serEntry.entryDate)
                    entryRepository.save(serEntry.toDomain(people, places))
                    serEntry.interactions.forEach { intDto ->
                        val person = peopleMap[intDto.personId] ?: return@forEach
                        interactionRepository.save(intDto.toDomain(person, entryDate))
                    }
                    imported++
                } catch (e: Exception) {
                    skipped++
                }
            }
            ImportResult(success = true, entriesImported = imported, entriesSkipped = skipped)
        } catch (e: Exception) {
            ImportResult(success = false, errors = listOf(e.message ?: "Unknown error"))
        }
    }

    suspend fun exportToCsv(): File {
        val entries = entryRepository.getAll()
        val sb = StringBuilder()
        sb.appendLine("date,anxious,happy,calm,connected,energy,sleep,interactions")
        entries.forEach { entry ->
            sb.appendLine(
                "${entry.entryDate},${entry.emotionData.anxious},${entry.emotionData.happy}," +
                "${entry.emotionData.calm},${entry.emotionData.connected}," +
                "${entry.healthContextData.energyLevel},${entry.healthContextData.hoursSlept}," +
                "${entry.interactions.size}"
            )
        }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(context.getExternalFilesDir(null), "mnemosyne_export_$timestamp.csv")
        file.writeText(sb.toString())
        return file
    }
}
