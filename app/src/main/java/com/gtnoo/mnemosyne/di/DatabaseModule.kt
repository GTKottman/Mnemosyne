package com.gtnoo.mnemosyne.di

import android.content.Context
import androidx.room.Room
import com.gtnoo.mnemosyne.data.local.MnemosyneDatabase
import com.gtnoo.mnemosyne.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MnemosyneDatabase =
        Room.databaseBuilder(context, MnemosyneDatabase::class.java, "mnemosyne.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSavedPlaceDao(db: MnemosyneDatabase): SavedPlaceDao = db.savedPlaceDao()
    @Provides fun providePersonDao(db: MnemosyneDatabase): PersonDao = db.personDao()
    @Provides fun provideDailyEntryDao(db: MnemosyneDatabase): DailyEntryDao = db.dailyEntryDao()
    @Provides fun provideWeatherSnapshotDao(db: MnemosyneDatabase): WeatherSnapshotDao = db.weatherSnapshotDao()
}
