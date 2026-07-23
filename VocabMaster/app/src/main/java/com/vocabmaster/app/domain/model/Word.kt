package com.vocabmaster.app.domain.model

/**
 * UI 层使用的单词模型，与 Room 实体解耦。
 */
data class Word(
    val id: Long,
    val word: String,
    val phonetic: String?,
    val partOfSpeech: String?,
    val definition: String?,
    val example: String?,
    val exampleTranslation: String?,
    val status: Int,
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val dueAt: Long
) {
    val isFetched: Boolean
        get() = !definition.isNullOrBlank()

    val statusLabel: String
        get() = when (status) {
            STATUS_NEW -> "新词"
            STATUS_LEARNING -> "学习中"
            STATUS_MASTERED -> "已掌握"
            else -> "新词"
        }

    companion object {
        const val STATUS_NEW = 0
        const val STATUS_LEARNING = 1
        const val STATUS_MASTERED = 2
    }
}
