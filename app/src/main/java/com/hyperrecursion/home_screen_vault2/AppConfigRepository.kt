package com.hyperrecursion.home_screen_vault2

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigRepoModule {

    @Provides
    @Singleton
    fun provideAppConfigRepository(
        @ApplicationContext context: Context
    ): AppConfigRepository {
        return AppConfigRepository(
            context
        )
    }
}

class AppConfigRepository(
    context: Context
) {

    private val Context.appConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_config")
    private val dataStore = context.appConfigDataStore

    private val highFreqScanIntervalSecsKey = intPreferencesKey("high_freq_scan_interval_secs")
    private val lowFreqScanIntervalSecsKey = intPreferencesKey("low_freq_scan_interval_secs")
    private val vaultPathKey = stringPreferencesKey("vault_path")
    private val enableDebugLineKey = booleanPreferencesKey("enable_debug_line")
    private val widthCorrectionKey = doublePreferencesKey("width_correction")

    fun getAppConfigFlow(): Flow<AppConfig> = dataStore.data.map { preferences ->
            AppConfig().apply {
                preferences[highFreqScanIntervalSecsKey]?.let { highFreqScanIntervalSecs = it }
                preferences[lowFreqScanIntervalSecsKey]?.let { lowFreqScanIntervalSecs = it }
                preferences[vaultPathKey]?.let { vaultPath = it }
                preferences[enableDebugLineKey]?.let { enableDebugLine = it }
                preferences[widthCorrectionKey]?.let { widthCorrection = it }
            }
        }

    suspend fun saveAppConfig(appConfig: AppConfig) {
        dataStore.edit { preferences ->
            preferences[highFreqScanIntervalSecsKey] = appConfig.highFreqScanIntervalSecs
            preferences[lowFreqScanIntervalSecsKey] = appConfig.lowFreqScanIntervalSecs
            preferences[vaultPathKey] = appConfig.vaultPath
            preferences[enableDebugLineKey] = appConfig.enableDebugLine
            preferences[widthCorrectionKey] = appConfig.widthCorrection
        }
    }

    companion object {
        fun checkPathExists(path: String): Boolean {
            val publicStorageDir = Environment.getExternalStorageDirectory()
            val folderPath = File(publicStorageDir, path)
            return folderPath.exists()
        }
    }
}