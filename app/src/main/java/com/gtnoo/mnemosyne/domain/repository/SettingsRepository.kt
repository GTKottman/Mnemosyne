package com.gtnoo.mnemosyne.domain.repository

import com.gtnoo.mnemosyne.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
}
