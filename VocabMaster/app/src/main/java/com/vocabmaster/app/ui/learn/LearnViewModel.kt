package com.vocabmaster.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabmaster.app.data.repository.LearningRepository
import com.vocabmaster.app.data.repository.WordRepository
import com.vocabmaster.app.domain.SRScheduler
import com.vocabmaster.app.domain.model.Word
import com.vocabmaster.app.tts.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 单张学习卡片：单词 + 题型 + 选项（选项仅用于选择题/听音题）。 */
data class LearnCard(
    val word: Word,
    val type: QuestionType,
    val options: List<String> = emptyList()
)

enum class QuestionType {
    /** 翻卡闪记：自评掌握度 */
    FLASHCARD,

    /** 看释义选单词 */
    CHOICE_MEANING,

    /** 看单词选释义 */
    CHOICE_WORD,

    /** 听发音选单词 */
    LISTEN;

    companion object {
        /** 为新词优先翻卡，已学过的词混合三种题型。 */
        fun pickFor(word: Word, indexInSession: Int): QuestionType {
            return if (word.status == Word.STATUS_NEW && word.repetitions == 0) {
                FLASHCARD
            } else {
                // 已学单词轮换三种主动题型
                values().filter { it != FLASHCARD }[indexInSession % 3]
            }
        }
    }
}

data class LearnUiState(
    val isLoading: Boolean = true,
    val cards: List<LearnCard> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswered: Boolean = false,
    val selectedIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val isComplete: Boolean = false,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val errorMessage: String? = null
) {
    val currentCard: LearnCard?
        get() = cards.getOrNull(currentIndex)

    val progress: Float
        get() = if (totalCount == 0) 0f else currentIndex.toFloat() / totalCount
}

class LearnViewModel(
    private val learningRepository: LearningRepository,
    private val wordRepository: WordRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _state = MutableStateFlow(LearnUiState())
    val state: StateFlow<LearnUiState> = _state.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 确保词库已就绪（释义已拉取）
                wordRepository.seedIfEmpty()
                wordRepository.fetchDetailsFor(batchSize = 10)

                val sessionWords = learningRepository.fetchSession()
                if (sessionWords.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isComplete = true,
                            errorMessage = "暂无可用单词，请稍后重试或下拉刷新首页拉取释义"
                        )
                    }
                    return@launch
                }

                val cards = sessionWords.mapIndexed { index, word ->
                    val type = QuestionType.pickFor(word, index)
                    val options = if (type == QuestionType.FLASHCARD) {
                        emptyList()
                    } else {
                        buildOptions(word, type)
                    }
                    LearnCard(word = word, type = type, options = options)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        cards = cards,
                        currentIndex = 0,
                        totalCount = cards.size,
                        correctCount = 0
                    )
                }
                // 听音题自动播放
                tryAutoPlayTts()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载失败：${e.message ?: "未知错误"}"
                    )
                }
            }
        }
    }

    /** 为选择题/听音题生成 4 个选项（含正确答案，打乱顺序）。 */
    private suspend fun buildOptions(word: Word, type: QuestionType): List<String> {
        val distractors = learningRepository.fetchDistractors(word.id, count = 3)
        val correctOption = when (type) {
            QuestionType.CHOICE_MEANING, QuestionType.LISTEN -> word.word
            QuestionType.CHOICE_WORD -> word.definition ?: word.word
            QuestionType.FLASHCARD -> word.word
        }
        val wrongOptions = distractors.mapNotNull { d ->
            when (type) {
                QuestionType.CHOICE_MEANING, QuestionType.LISTEN -> d.word
                QuestionType.CHOICE_WORD -> d.definition
                QuestionType.FLASHCARD -> null
            }?.takeIf { it.isNotBlank() }
        }
        val all = (wrongOptions + correctOption).distinct().shuffled()
        // 兜底：若释义不足导致选项不足 4 个，重复填充以保证 UI 一致
        return if (all.size >= 4) all.take(4) else {
            val padded = all.toMutableList()
            while (padded.size < 4) padded.add(correctOption)
            padded.take(4)
        }
    }

    /**
     * 翻卡闪记：用户自评掌握度。
     * @param quality 见 [SRScheduler]
     */
    fun submitFlashcard(quality: Int) {
        val card = _state.value.currentCard ?: return
        viewModelScope.launch {
            learningRepository.submitReview(card.word.id, quality)
            advance(quality >= 3)
        }
    }

    /**
     * 选择题/听音题：提交选项索引。
     */
    fun submitChoice(optionIndex: Int) {
        val card = _state.value.currentCard ?: return
        if (_state.value.isAnswered) return

        val correctText = when (card.type) {
            QuestionType.CHOICE_MEANING, QuestionType.LISTEN -> card.word.word
            QuestionType.CHOICE_WORD -> card.word.definition ?: card.word.word
            QuestionType.FLASHCARD -> card.word.word
        }
        val selected = card.options.getOrNull(optionIndex)
        val isCorrect = selected == correctText

        _state.update {
            it.copy(
                isAnswered = true,
                selectedIndex = optionIndex,
                isCorrect = isCorrect
            )
        }

        // 正确→EASY(5)，错误→FORGOT(1)
        val quality = if (isCorrect) SRScheduler.QUALITY_EASY else SRScheduler.QUALITY_FORGOT
        viewModelScope.launch {
            learningRepository.submitReview(card.word.id, quality)
        }
    }

    /** 选择题/听音题答完后进入下一题。 */
    fun nextQuestion() {
        val isCorrect = _state.value.isCorrect ?: false
        advance(isCorrect)
    }

    /** 重新开启一次学习会话。 */
    fun restart() {
        _state.value = LearnUiState()
        loadSession()
    }

    private fun advance(wasCorrect: Boolean) {
        val current = _state.value
        val nextIndex = current.currentIndex + 1
        val newCorrect = current.correctCount + (if (wasCorrect) 1 else 0)

        if (nextIndex >= current.cards.size) {
            _state.update {
                it.copy(
                    isComplete = true,
                    correctCount = newCorrect,
                    isAnswered = false,
                    selectedIndex = null,
                    isCorrect = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    currentIndex = nextIndex,
                    isAnswered = false,
                    selectedIndex = null,
                    isCorrect = null,
                    correctCount = newCorrect
                )
            }
            tryAutoPlayTts()
        }
    }

    /** 当前卡片是听音题时，自动播放一次发音。 */
    private fun tryAutoPlayTts() {
        val card = _state.value.currentCard ?: return
        if (card.type == QuestionType.LISTEN) {
            playTts(card.word.word)
        }
    }

    /** 用户点击喇叭按钮主动播放发音。 */
    fun playPronunciation() {
        val card = _state.value.currentCard ?: return
        playTts(card.word.word)
    }

    private fun playTts(text: String) {
        ttsManager.speak(text)
    }

    /** 退出时释放（实际由 Application 持有 TTS 单例，此处不释放）。 */
    fun exit() {
        // no-op：TTS 由 AppContainer 管理，跟随进程生命周期
    }
}
