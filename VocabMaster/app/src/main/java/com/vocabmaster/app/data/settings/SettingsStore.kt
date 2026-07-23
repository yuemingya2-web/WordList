package com.vocabmaster.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "vocab_settings")

/** 应用偏好：TTS 语速与英/美音切换。 */
class SettingsStore(context: Context) {

    private val dataStore = context.applicationContext.appDataStore

    val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            speechRate = prefs[SPEECH_RATE] ?: 1.0f,
            isUsAccent = prefs[IS_US_ACCENT] ?: true
        )
    }

    suspend fun setSpeechRate(value: Float) {
        dataStore.edit { it[SPEECH_RATE] = value }
    }

    suspend fun setAccent(isUs: Boolean) {
        dataStore.edit { it[IS_US_ACCENT] = isUs }
    }

    companion object {
        private val SPEECH_RATE = floatPreferencesKey("speech_rate")
        private val IS_US_ACCENT = booleanPreferencesKey("is_us_accent")
    }
}

data class UserSettings(
    val speechRate: Float,
    val isUsAccent: Boolean
)
