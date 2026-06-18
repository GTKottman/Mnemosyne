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
        val USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val APP_VERSION = stringPreferencesKey("app_version")
        val SHOW_WORK_SCHOOL_PRESSURE = booleanPreferencesKey("show_work_school_pressure")
        val SHOW_MONEY_PRESSURE = booleanPreferencesKey("show_money_pressure")
        val SHOW_RELATIONSHIP_PRESSURE = booleanPreferencesKey("show_relationship_pressure")
        val SHOW_FAMILY_PRESSURE = booleanPreferencesKey("show_family_pressure")
        val SHOW_HEALTH_PRESSURE = booleanPreferencesKey("show_health_pressure")
        val SHOW_TIME_PRESSURE = booleanPreferencesKey("show_time_pressure")
    }

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            homePlaceId = prefs[Keys.HOME_PLACE_ID],
            autoFetchWeather = prefs[Keys.AUTO_FETCH_WEATHER] ?: true,
            useFahrenheit = prefs[Keys.USE_FAHRENHEIT] ?: false,
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
            appVersion = prefs[Keys.APP_VERSION] ?: "0.1.0",
            showWorkSchoolPressure = prefs[Keys.SHOW_WORK_SCHOOL_PRESSURE] ?: true,
            showMoneyPressure = prefs[Keys.SHOW_MONEY_PRESSURE] ?: true,
            showRelationshipPressure = prefs[Keys.SHOW_RELATIONSHIP_PRESSURE] ?: true,
            showFamilyPressure = prefs[Keys.SHOW_FAMILY_PRESSURE] ?: true,
            showHealthPressure = prefs[Keys.SHOW_HEALTH_PRESSURE] ?: true,
            showTimePressure = prefs[Keys.SHOW_TIME_PRESSURE] ?: true
        )
    }

    override suspend fun getSettings(): AppSettings = observeSettings().first()

    override suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            settings.homePlaceId?.let { prefs[Keys.HOME_PLACE_ID] = it }
                ?: prefs.remove(Keys.HOME_PLACE_ID)
            prefs[Keys.AUTO_FETCH_WEATHER] = settings.autoFetchWeather
            prefs[Keys.USE_FAHRENHEIT] = settings.useFahrenheit
            prefs[Keys.ONBOARDING_COMPLETE] = settings.onboardingComplete
            prefs[Keys.APP_VERSION] = settings.appVersion
            prefs[Keys.SHOW_WORK_SCHOOL_PRESSURE] = settings.showWorkSchoolPressure
            prefs[Keys.SHOW_MONEY_PRESSURE] = settings.showMoneyPressure
            prefs[Keys.SHOW_RELATIONSHIP_PRESSURE] = settings.showRelationshipPressure
            prefs[Keys.SHOW_FAMILY_PRESSURE] = settings.showFamilyPressure
            prefs[Keys.SHOW_HEALTH_PRESSURE] = settings.showHealthPressure
            prefs[Keys.SHOW_TIME_PRESSURE] = settings.showTimePressure
        }
    }
}
