package com.gtnoo.mnemosyne.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gtnoo.mnemosyne.domain.model.AppSettings
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val HOME_PLACE_ID = stringPreferencesKey("home_place_id")
        val AUTO_FETCH_WEATHER = booleanPreferencesKey("auto_fetch_weather")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val APP_VERSION = stringPreferencesKey("app_version")
    }

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            homePlaceId = prefs[Keys.HOME_PLACE_ID],
            autoFetchWeather = prefs[Keys.AUTO_FETCH_WEATHER] ?: true,
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
            appVersion = prefs[Keys.APP_VERSION] ?: "0.1.0"
        )
    }

    override suspend fun getSettings(): AppSettings = observeSettings().first()

    override suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            settings.homePlaceId?.let { prefs[Keys.HOME_PLACE_ID] = it }
                ?: prefs.remove(Keys.HOME_PLACE_ID)
            prefs[Keys.AUTO_FETCH_WEATHER] = settings.autoFetchWeather
            prefs[Keys.ONBOARDING_COMPLETE] = settings.onboardingComplete
            prefs[Keys.APP_VERSION] = settings.appVersion
        }
    }
}
