package com.gtnoo.mnemosyne.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gtnoo.mnemosyne.data.local.converter.Converters
import com.gtnoo.mnemosyne.data.local.dao.*
import com.gtnoo.mnemosyne.data.local.entity.*

@Database(
    entities = [
        SavedPlaceEntity::class,
        PersonEntity::class,
        DailyEntryEntity::class,
        PersonInteractionEntity::class,
        ImportantDateEntity::class,
        WeatherSnapshotEntity::class,
        EntryPlaceCrossRef::class,
        EntryWeatherCrossRef::class,
        MedicineEntity::class,
        MedicineBottleEntity::class,
        MedicineDoseEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MnemosyneDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun personDao(): PersonDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun weatherSnapshotDao(): WeatherSnapshotDao
    abstract fun interactionDao(): InteractionDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun medicineDao(): MedicineDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE daily_entries ADD COLUMN contextVacation INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE person_interactions_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        date TEXT NOT NULL,
                        personId TEXT NOT NULL,
                        mode TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        commTheyInitiated INTEGER NOT NULL,
                        commIInitiated INTEGER NOT NULL,
                        commTheyReplied INTEGER NOT NULL,
                        commIReplied INTEGER NOT NULL,
                        commLeftOnRead INTEGER NOT NULL,
                        commIWasLeftOnRead INTEGER NOT NULL,
                        commReacted INTEGER NOT NULL,
                        commSentLong INTEGER NOT NULL,
                        commSentShort INTEGER NOT NULL,
                        commUsedEmoji INTEGER NOT NULL,
                        commUsedExclamation INTEGER NOT NULL,
                        commAskedQuestion INTEGER NOT NULL,
                        commKeptGoing INTEGER NOT NULL,
                        commEndedAbruptly INTEGER NOT NULL,
                        commTotalMessages INTEGER NOT NULL,
                        commTheirMessages INTEGER NOT NULL,
                        commMyMessages INTEGER NOT NULL,
                        commAvgReplyTime REAL NOT NULL,
                        commLongestGap REAL NOT NULL,
                        inpSawInPerson INTEGER NOT NULL,
                        inpTalkedInPerson INTEGER NOT NULL,
                        inpTheyApproached INTEGER NOT NULL,
                        inpIApproached INTEGER NOT NULL,
                        inpSatNear INTEGER NOT NULL,
                        inpWalkedTogether INTEGER NOT NULL,
                        inpTheySmiled INTEGER NOT NULL,
                        inpTheyLaughed INTEGER NOT NULL,
                        inpConversationMinutes INTEGER NOT NULL,
                        inpEyeContact INTEGER NOT NULL,
                        inpPhysicalProximity INTEGER NOT NULL,
                        inpFeltNatural INTEGER NOT NULL,
                        inpFeltAwkward INTEGER NOT NULL,
                        relFeltPrioritized INTEGER NOT NULL,
                        relFeltIgnored INTEGER NOT NULL,
                        relFeltChosen INTEGER NOT NULL,
                        relFeltOptional INTEGER NOT NULL,
                        relFeltSafe INTEGER NOT NULL,
                        relFeltConfused INTEGER NOT NULL,
                        relFeltWarm INTEGER NOT NULL,
                        relFeltDistant INTEGER NOT NULL,
                        relFeltMutual INTEGER NOT NULL,
                        relFeltStable INTEGER NOT NULL,
                        outFeltBetter INTEGER NOT NULL,
                        outFeltWorse INTEGER NOT NULL,
                        outResolved INTEGER NOT NULL,
                        outTalkedAgain INTEGER NOT NULL,
                        outTheyFollowedUp INTEGER NOT NULL,
                        outPredictionCorrect INTEGER NOT NULL,
                        outAnxietyFalseAlarm INTEGER NOT NULL,
                        outConnectionImproved INTEGER NOT NULL,
                        outConnectionDeclined INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO person_interactions_new
                    SELECT pi.id, de.entryDate, pi.personId, pi.mode, pi.notes,
                           pi.commTheyInitiated, pi.commIInitiated, pi.commTheyReplied, pi.commIReplied,
                           pi.commLeftOnRead, pi.commIWasLeftOnRead, pi.commReacted, pi.commSentLong,
                           pi.commSentShort, pi.commUsedEmoji, pi.commUsedExclamation, pi.commAskedQuestion,
                           pi.commKeptGoing, pi.commEndedAbruptly, pi.commTotalMessages, pi.commTheirMessages,
                           pi.commMyMessages, pi.commAvgReplyTime, pi.commLongestGap,
                           pi.inpSawInPerson, pi.inpTalkedInPerson, pi.inpTheyApproached, pi.inpIApproached,
                           pi.inpSatNear, pi.inpWalkedTogether, pi.inpTheySmiled, pi.inpTheyLaughed,
                           pi.inpConversationMinutes, pi.inpEyeContact, pi.inpPhysicalProximity,
                           pi.inpFeltNatural, pi.inpFeltAwkward,
                           pi.relFeltPrioritized, pi.relFeltIgnored, pi.relFeltChosen, pi.relFeltOptional,
                           pi.relFeltSafe, pi.relFeltConfused, pi.relFeltWarm, pi.relFeltDistant,
                           pi.relFeltMutual, pi.relFeltStable,
                           pi.outFeltBetter, pi.outFeltWorse, pi.outResolved, pi.outTalkedAgain,
                           pi.outTheyFollowedUp, pi.outPredictionCorrect, pi.outAnxietyFalseAlarm,
                           pi.outConnectionImproved, pi.outConnectionDeclined
                    FROM person_interactions pi
                    INNER JOIN daily_entries de ON de.id = pi.entryId
                """.trimIndent())

                db.execSQL("DROP TABLE person_interactions")
                db.execSQL("ALTER TABLE person_interactions_new RENAME TO person_interactions")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_person_interactions_date ON person_interactions (date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_person_interactions_personId ON person_interactions (personId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE person_important_dates (
                        id TEXT NOT NULL PRIMARY KEY,
                        personId TEXT NOT NULL,
                        label TEXT NOT NULL,
                        date TEXT,
                        kind TEXT NOT NULL,
                        isRecurring INTEGER NOT NULL,
                        notifyEnabled INTEGER NOT NULL,
                        notifyDaysBefore INTEGER NOT NULL,
                        remindToLogInteraction INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_person_important_dates_personId ON person_important_dates (personId)")
                db.execSQL("""
                    INSERT INTO person_important_dates (id, personId, label, date, kind, isRecurring, notifyEnabled, notifyDaysBefore, remindToLogInteraction)
                    SELECT lower(hex(randomblob(16))), id, 'Birthday', NULL, 'BIRTHDAY', 1, 0, 0, 0
                    FROM people
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE medicines (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE medicine_bottles (
                        id TEXT NOT NULL PRIMARY KEY,
                        medicineId TEXT NOT NULL,
                        mgPerPill INTEGER NOT NULL,
                        pillsTotal INTEGER NOT NULL,
                        pillsRemaining INTEGER NOT NULL,
                        openedDate TEXT NOT NULL,
                        isCurrentBottle INTEGER NOT NULL,
                        FOREIGN KEY (medicineId) REFERENCES medicines(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medicine_bottles_medicineId ON medicine_bottles (medicineId)")
                db.execSQL("""
                    CREATE TABLE medicine_doses (
                        id TEXT NOT NULL PRIMARY KEY,
                        medicineId TEXT NOT NULL,
                        bottleId TEXT NOT NULL,
                        date TEXT NOT NULL,
                        pillsTaken INTEGER NOT NULL,
                        FOREIGN KEY (medicineId) REFERENCES medicines(id) ON DELETE CASCADE,
                        FOREIGN KEY (bottleId) REFERENCES medicine_bottles(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medicine_doses_medicineId ON medicine_doses (medicineId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medicine_doses_date ON medicine_doses (date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medicine_doses_bottleId ON medicine_doses (bottleId)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicines ADD COLUMN reminder_time TEXT")
                db.execSQL("ALTER TABLE medicines ADD COLUMN notify_enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medicine_doses ADD COLUMN taken_at TEXT")
            }
        }

        // Replaces health fields: drops 10 old columns (schoolStress, workStress, ateEnough Boolean,
        // sensoryOverload) and adds 25 new columns across 4 groups (Core State, Stress Categories,
        // Inputs, Environment). Maps: workSchoolPressure=MAX(school,work),
        // sensoryEnvironmentalStrain=sensoryOverload, foodAdequacy from Boolean (ate=7, not=3).
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE daily_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        entryDate TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        lastEditedAt TEXT NOT NULL,
                        freeformNotes TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        contextEntryType TEXT NOT NULL,
                        contextSchoolDay INTEGER NOT NULL,
                        contextWorkDay INTEGER NOT NULL,
                        contextWeekend INTEGER NOT NULL,
                        contextHoliday INTEGER NOT NULL,
                        contextVacation INTEGER NOT NULL,
                        emotionHappiness INTEGER NOT NULL,
                        emotionSafety INTEGER NOT NULL,
                        emotionCalm INTEGER NOT NULL,
                        emotionConnection INTEGER NOT NULL,
                        emotionClarity INTEGER NOT NULL,
                        emotionCapacity INTEGER NOT NULL,
                        emotionHope INTEGER NOT NULL,
                        emotionWorthiness INTEGER NOT NULL,
                        emotionPeace INTEGER NOT NULL,
                        emotionEnergy INTEGER NOT NULL,
                        emotionAgency INTEGER NOT NULL,
                        emotionPresence INTEGER NOT NULL,
                        thoughtRuminationLevel INTEGER NOT NULL,
                        thoughtIntrusiveThoughts INTEGER NOT NULL,
                        thoughtWorryAnticipation INTEGER NOT NULL,
                        thoughtCatastrophizing INTEGER NOT NULL,
                        thoughtReassuranceUrge INTEGER NOT NULL,
                        thoughtMentalClarity INTEGER NOT NULL,
                        thoughtUncertaintyTolerance INTEGER NOT NULL,
                        thoughtDecisionFriction INTEGER NOT NULL,
                        thoughtCognitiveFlexibility INTEGER NOT NULL,
                        thoughtSelfTalkTone INTEGER NOT NULL,
                        healthEnergyLevel INTEGER NOT NULL,
                        healthSocialBattery INTEGER NOT NULL,
                        healthExecutiveFunction INTEGER NOT NULL,
                        healthMentalBandwidth INTEGER NOT NULL,
                        healthBodyDiscomfort INTEGER NOT NULL,
                        healthSensoryEnvironmentalStrain INTEGER NOT NULL,
                        healthSleepQuality INTEGER NOT NULL,
                        healthStressLoad INTEGER NOT NULL,
                        healthWorkSchoolPressure INTEGER NOT NULL,
                        healthMoneyPressure INTEGER NOT NULL,
                        healthRelationshipPressure INTEGER NOT NULL,
                        healthFamilyPressure INTEGER NOT NULL,
                        healthHealthPressure INTEGER NOT NULL,
                        healthTimePressure INTEGER NOT NULL,
                        healthHoursSlept REAL NOT NULL,
                        healthCaffeineIntakeMg INTEGER NOT NULL,
                        healthFoodAdequacy INTEGER NOT NULL,
                        healthHydration INTEGER NOT NULL,
                        healthMovementLevel INTEGER NOT NULL,
                        healthTimeOutsideSunlight INTEGER NOT NULL,
                        healthScreenLoad INTEGER NOT NULL,
                        healthSocialExposure INTEGER NOT NULL,
                        healthNoveltyDisruption INTEGER NOT NULL,
                        healthPhysicalSpaceQuality INTEGER NOT NULL,
                        healthWeatherImpact INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO daily_entries_new
                    SELECT
                        id, entryDate, createdAt, lastEditedAt, freeformNotes, tags,
                        contextEntryType, contextSchoolDay, contextWorkDay, contextWeekend,
                        contextHoliday, contextVacation,
                        emotionHappiness, emotionSafety, emotionCalm, emotionConnection,
                        emotionClarity, emotionCapacity, emotionHope, emotionWorthiness,
                        emotionPeace, emotionEnergy, emotionAgency, emotionPresence,
                        thoughtRuminationLevel, thoughtIntrusiveThoughts, thoughtWorryAnticipation,
                        thoughtCatastrophizing, thoughtReassuranceUrge, thoughtMentalClarity,
                        thoughtUncertaintyTolerance, thoughtDecisionFriction, thoughtCognitiveFlexibility,
                        thoughtSelfTalkTone,
                        healthEnergyLevel,
                        healthSocialBattery,
                        healthExecutiveFunction,
                        5,
                        healthBodyDiscomfort,
                        healthSensoryOverload,
                        5,
                        0,
                        MAX(healthSchoolStress, healthWorkStress),
                        0,
                        0,
                        0,
                        0,
                        0,
                        healthHoursSlept,
                        healthCaffeineIntakeMg,
                        CASE WHEN healthAteEnough = 1 THEN 7 ELSE 3 END,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        5,
                        0
                    FROM daily_entries
                """.trimIndent())

                db.execSQL("DROP TABLE daily_entries")
                db.execSQL("ALTER TABLE daily_entries_new RENAME TO daily_entries")
            }
        }

        // Replaces thought fields: drops 4 booleans + wantedToPullAway + wantedToReachOut,
        // renames needForReassurance->reassuranceUrge, clarityLevel->mentalClarity,
        // uncertaintyLevel->uncertaintyTolerance, and adds 6 new Int columns.
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE daily_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        entryDate TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        lastEditedAt TEXT NOT NULL,
                        freeformNotes TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        contextEntryType TEXT NOT NULL,
                        contextSchoolDay INTEGER NOT NULL,
                        contextWorkDay INTEGER NOT NULL,
                        contextWeekend INTEGER NOT NULL,
                        contextHoliday INTEGER NOT NULL,
                        contextVacation INTEGER NOT NULL,
                        emotionHappiness INTEGER NOT NULL,
                        emotionSafety INTEGER NOT NULL,
                        emotionCalm INTEGER NOT NULL,
                        emotionConnection INTEGER NOT NULL,
                        emotionClarity INTEGER NOT NULL,
                        emotionCapacity INTEGER NOT NULL,
                        emotionHope INTEGER NOT NULL,
                        emotionWorthiness INTEGER NOT NULL,
                        emotionPeace INTEGER NOT NULL,
                        emotionEnergy INTEGER NOT NULL,
                        emotionAgency INTEGER NOT NULL,
                        emotionPresence INTEGER NOT NULL,
                        thoughtRuminationLevel INTEGER NOT NULL,
                        thoughtIntrusiveThoughts INTEGER NOT NULL,
                        thoughtWorryAnticipation INTEGER NOT NULL,
                        thoughtCatastrophizing INTEGER NOT NULL,
                        thoughtReassuranceUrge INTEGER NOT NULL,
                        thoughtMentalClarity INTEGER NOT NULL,
                        thoughtUncertaintyTolerance INTEGER NOT NULL,
                        thoughtDecisionFriction INTEGER NOT NULL,
                        thoughtCognitiveFlexibility INTEGER NOT NULL,
                        thoughtSelfTalkTone INTEGER NOT NULL,
                        healthHoursSlept REAL NOT NULL,
                        healthAteEnough INTEGER NOT NULL,
                        healthCaffeineIntakeMg INTEGER NOT NULL,
                        healthSchoolStress INTEGER NOT NULL,
                        healthWorkStress INTEGER NOT NULL,
                        healthSensoryOverload INTEGER NOT NULL,
                        healthEnergyLevel INTEGER NOT NULL,
                        healthBodyDiscomfort INTEGER NOT NULL,
                        healthExecutiveFunction INTEGER NOT NULL,
                        healthSocialBattery INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO daily_entries_new
                    SELECT
                        id, entryDate, createdAt, lastEditedAt, freeformNotes, tags,
                        contextEntryType, contextSchoolDay, contextWorkDay, contextWeekend,
                        contextHoliday, contextVacation,
                        emotionHappiness, emotionSafety, emotionCalm, emotionConnection,
                        emotionClarity, emotionCapacity, emotionHope, emotionWorthiness,
                        emotionPeace, emotionEnergy, emotionAgency, emotionPresence,
                        thoughtRuminationLevel,
                        0,
                        0,
                        0,
                        thoughtNeedReassurance,
                        thoughtClarityLevel,
                        thoughtUncertaintyLevel,
                        0,
                        0,
                        5,
                        healthHoursSlept, healthAteEnough, healthCaffeineIntakeMg,
                        healthSchoolStress, healthWorkStress, healthSensoryOverload,
                        healthEnergyLevel, healthBodyDiscomfort, healthExecutiveFunction,
                        healthSocialBattery
                    FROM daily_entries
                """.trimIndent())

                db.execSQL("DROP TABLE daily_entries")
                db.execSQL("ALTER TABLE daily_entries_new RENAME TO daily_entries")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE people ADD COLUMN interactionReminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE people ADD COLUMN interactionReminderTime TEXT")
            }
        }

        // Replaces 14 separate emotion columns with 12 bipolar-axis columns (0=distress, 5=neutral, 10=supportive).
        // Old values are mapped where a clear directional equivalent exists; others default to 5 (neutral).
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE daily_entries_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        entryDate TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        lastEditedAt TEXT NOT NULL,
                        freeformNotes TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        contextEntryType TEXT NOT NULL,
                        contextSchoolDay INTEGER NOT NULL,
                        contextWorkDay INTEGER NOT NULL,
                        contextWeekend INTEGER NOT NULL,
                        contextHoliday INTEGER NOT NULL,
                        contextVacation INTEGER NOT NULL,
                        emotionHappiness INTEGER NOT NULL,
                        emotionSafety INTEGER NOT NULL,
                        emotionCalm INTEGER NOT NULL,
                        emotionConnection INTEGER NOT NULL,
                        emotionClarity INTEGER NOT NULL,
                        emotionCapacity INTEGER NOT NULL,
                        emotionHope INTEGER NOT NULL,
                        emotionWorthiness INTEGER NOT NULL,
                        emotionPeace INTEGER NOT NULL,
                        emotionEnergy INTEGER NOT NULL,
                        emotionAgency INTEGER NOT NULL,
                        emotionPresence INTEGER NOT NULL,
                        thoughtRuminationLevel INTEGER NOT NULL,
                        thoughtCheckedPhone INTEGER NOT NULL,
                        thoughtRereadMessages INTEGER NOT NULL,
                        thoughtImaginedNegative INTEGER NOT NULL,
                        thoughtImaginedPositive INTEGER NOT NULL,
                        thoughtNeedReassurance INTEGER NOT NULL,
                        thoughtClarityLevel INTEGER NOT NULL,
                        thoughtUncertaintyLevel INTEGER NOT NULL,
                        thoughtWantedToPullAway INTEGER NOT NULL,
                        thoughtWantedToReachOut INTEGER NOT NULL,
                        healthHoursSlept REAL NOT NULL,
                        healthAteEnough INTEGER NOT NULL,
                        healthCaffeineIntakeMg INTEGER NOT NULL,
                        healthSchoolStress INTEGER NOT NULL,
                        healthWorkStress INTEGER NOT NULL,
                        healthSensoryOverload INTEGER NOT NULL,
                        healthEnergyLevel INTEGER NOT NULL,
                        healthBodyDiscomfort INTEGER NOT NULL,
                        healthExecutiveFunction INTEGER NOT NULL,
                        healthSocialBattery INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO daily_entries_new
                    SELECT
                        id, entryDate, createdAt, lastEditedAt, freeformNotes, tags,
                        contextEntryType, contextSchoolDay, contextWorkDay, contextWeekend,
                        contextHoliday, contextVacation,
                        COALESCE(emotionHappy, 5),
                        5,
                        MAX(0, MIN(10, 10 - COALESCE(emotionAnxious, 5))),
                        COALESCE(emotionConnected, 5),
                        MAX(0, MIN(10, 10 - COALESCE(emotionConfused, 5))),
                        MAX(0, MIN(10, 10 - COALESCE(emotionOverwhelmed, 5))),
                        COALESCE(emotionHopeful, 5),
                        5,
                        5,
                        5,
                        5,
                        5,
                        thoughtRuminationLevel, thoughtCheckedPhone, thoughtRereadMessages,
                        thoughtImaginedNegative, thoughtImaginedPositive, thoughtNeedReassurance,
                        thoughtClarityLevel, thoughtUncertaintyLevel, thoughtWantedToPullAway,
                        thoughtWantedToReachOut,
                        healthHoursSlept, healthAteEnough, healthCaffeineIntakeMg,
                        healthSchoolStress, healthWorkStress, healthSensoryOverload,
                        healthEnergyLevel, healthBodyDiscomfort, healthExecutiveFunction,
                        healthSocialBattery
                    FROM daily_entries
                """.trimIndent())

                db.execSQL("DROP TABLE daily_entries")
                db.execSQL("ALTER TABLE daily_entries_new RENAME TO daily_entries")
            }
        }
    }
}
