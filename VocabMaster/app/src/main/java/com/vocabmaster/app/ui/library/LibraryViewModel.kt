package com.vocabmaster.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabmaster.app.data.local.dao.WordDao
import com.vocabmaster.app.data.repository.WordRepository
import com.vocabmaster.app.domain.model.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val query: String = "",
    val words: List<Word> = emptyList(),
    val isAdding: Boolean = false,
    val addResult: String? = null
)

class LibraryViewModel(
    private val wordDao: WordDao,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _isAdding = MutableStateFlow(false)
    private val _addResult = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val wordsFlow = _query.flatMapLatest { query ->
        if (query.isBlank()) wordDao.observeAll()
        else wordDao.search(query.trim())
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        _query, wordsFlow, _isAdding, _addResult
    ) { query, words, isAdding, addResult ->
        LibraryUiState(
            query = query,
            words = words.map { entity ->
                Word(
                    id = entity.id,
                    word = entity.word,
                    phonetic = entity.phonetic,
                    partOfSpeech = entity.partOfSpeech,
                    definition = entity.definition,
                    example = entity.example,
                    exampleTranslation = entity.exampleTranslation,
                    status = entity.status,
                    easeFactor = entity.easeFactor,
                    intervalDays = entity.intervalDays,
                    repetitions = entity.repetitions,
                    dueAt = entity.dueAt
                )
            },
            isAdding = isAdding,
            addResult = addResult
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState()
    )

    fun onQueryChange(query: String) {
        _query.update { query }
    }

    /** 通过 API 查询单词并加入词库。 */
    fun addWord(word: String) {
        if (word.isBlank() || _isAdding.value) return
        viewModelScope.launch {
            _isAdding.update { true }
            _addResult.update { null }
            try {
                val saved = wordRepository.fetchAndSave(word)
                _addResult.update { "已添加：${saved.word}" }
            } catch (e: Exception) {
                _addResult.update { "添加失败：${e.message ?: "未找到该单词"}" }
            } finally {
                _isAdding.update { false }
            }
        }
    }

    fun consumeAddResult() {
        _addResult.update { null }
    }
}
