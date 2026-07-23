package com.vocabmaster.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabmaster.app.data.local.entity.WordEntity
import com.vocabmaster.app.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val totalWords: Int = 0,
    val newWords: Int = 0,
    val learningWords: Int = 0,
    val masteredWords: Int = 0,
    val dueToday: Int = 0,
    val isFetching: Boolean = false,
    val fetchMessage: String? = null,
    val canStart: Boolean = false
)

class HomeViewModel(
    private val wordRepository: WordRepository,
    private val wordDao: com.vocabmaster.app.data.local.dao.WordDao
) : ViewModel() {

    private val _isFetching = MutableStateFlow(false)
    private val _fetchMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        wordDao.observeCount(),
        wordDao.observeCountByStatus(WordEntity.STATUS_NEW),
        wordDao.observeCountByStatus(WordEntity.STATUS_LEARNING),
        wordDao.observeCountByStatus(WordEntity.STATUS_MASTERED),
        wordDao.observeDueCount(System.currentTimeMillis())
    ) { total, new, learning, mastered, due ->
        HomeUiState(
            totalWords = total,
            newWords = new,
            learningWords = learning,
            masteredWords = mastered,
            dueToday = due,
            canStart = total > 0
        )
    }.combine(_isFetching) { state, fetching -> state.copy(isFetching = fetching) }
        .combine(_fetchMessage) { state, msg -> state.copy(fetchMessage = msg) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    init {
        ensureSeeded()
    }

    /** 首次进入时入库种子词并尝试拉取释义。 */
    private fun ensureSeeded() {
        viewModelScope.launch {
            wordRepository.seedIfEmpty()
            refreshFetchStatus()
        }
    }

    /** 主动触发一次词库释义拉取（用户下拉刷新或点击按钮时调用）。 */
    fun fetchDetails() {
        if (_isFetching.value) return
        viewModelScope.launch {
            _isFetching.update { true }
            _fetchMessage.update { null }
            try {
                val success = wordRepository.fetchDetailsFor(batchSize = 20)
                _fetchMessage.update { "已更新 $success 个单词的释义" }
            } catch (e: Exception) {
                _fetchMessage.update { "拉取失败：${e.message ?: "未知错误"}" }
            } finally {
                _isFetching.update { false }
            }
        }
    }

    fun consumeMessage() {
        _fetchMessage.update { null }
    }

    private fun refreshFetchStatus() {
        viewModelScope.launch {
            val unfetched = wordRepository.unfetchedCount()
            if (unfetched > 0) fetchDetails()
        }
    }
}
