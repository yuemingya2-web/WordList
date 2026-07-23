package com.vocabmaster.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabmaster.app.data.settings.SettingsStore
import com.vocabmaster.app.data.settings.UserSettings
import com.vocabmaster.app.tts.TtsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val speechRate: Float = 1.0f,
    val isUsAccent: Boolean = true
)

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val ttsManager: TtsManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsStore.settings
        .map { settings: UserSettings ->
            SettingsUiState(
                speechRate = settings.speechRate,
                isUsAccent = settings.isUsAccent
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    init {
        // 初始化时把当前语速/口音应用到 TTS
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                ttsManager.setSpeechRate(settings.speechRate)
                ttsManager.setLanguage(settings.isUsAccent)
            }
        }
    }

    fun setSpeechRate(value: Float) {
        viewModelScope.launch {
            settingsStore.setSpeechRate(value)
            ttsManager.setSpeechRate(value)
        }
    }

    fun setAccent(isUs: Boolean) {
        viewModelScope.launch {
            settingsStore.setAccent(isUs)
            ttsManager.setLanguage(isUs)
        }
    }

    fun testPronunciation() {
        ttsManager.speak("Hello, this is a pronunciation test.")
    }
}
