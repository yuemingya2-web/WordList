package com.vocabmaster.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * 封装 Android 系统 TextToSpeech 引擎。
 * 支持英/美音切换与语速调节；初始化结果通过 [isReady] 暴露。
 */
class TtsManager(context: Context) {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val utteranceCounter = AtomicInteger(0)

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            _isReady.value = true
            // 默认美音，调用方仍可在 setLanguage 后生效
            tts.language = Locale.US
        }
    }

    /** 当前使用的 locale，调用 setLanguage 后才真正切换。 */
    private var currentLocale: Locale = Locale.US

    fun setLanguage(isUs: Boolean) {
        currentLocale = if (isUs) Locale.US else Locale.UK
        val result = tts.setLanguage(currentLocale)
        _isReady.value = (result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED)
    }

    fun setSpeechRate(rate: Float) {
        // rate: 0.5 - 2.0，正常 1.0
        tts.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * 朗读单词。仅当 TTS 初始化成功时执行。
     * @param onDone 朗读结束回调（即使未真正播放也会触发，便于 UI 复位）
     */
    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!_isReady.value) {
            onDone()
            return
        }
        val utteranceId = "utt_${utteranceCounter.incrementAndGet()}"
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onDone() }
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun release() {
        tts.stop()
        tts.shutdown()
    }
}
