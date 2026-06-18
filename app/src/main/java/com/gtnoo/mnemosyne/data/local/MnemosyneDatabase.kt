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
        WeatherSnapshotEntity::class,
        EntryPlaceCrossRef::class,
        EntryWeatherCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MnemosyneDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun personDao(): PersonDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun weatherSnapshotDao(): WeatherSnapshotDao
    abstract fun interactionDao(): InteractionDao

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
    }
}
