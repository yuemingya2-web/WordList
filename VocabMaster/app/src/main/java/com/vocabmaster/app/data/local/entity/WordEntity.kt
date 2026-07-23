package com.vocabmaster.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 单词实体：包含词典信息与间隔重复(SM-2)调度状态。
 *
 * status: 0=新词, 1=学习中, 2=已掌握
 */
@Entity(
    tableName = "words",
    indices = [Index(value = ["word"], unique = true)]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "phonetic")
    val phonetic: String? = null,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String? = null,

    @ColumnInfo(name = "definition")
    val definition: String? = null,

    @ColumnInfo(name = "example")
    val example: String? = null,

    @ColumnInfo(name = "example_translation")
    val exampleTranslation: String? = null,

    @ColumnInfo(name = "status")
    val status: Int = STATUS_NEW,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Float = 2.5f,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 0,

    @ColumnInfo(name = "due_at")
    val dueAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_reviewed_at")
    val lastReviewedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_NEW = 0
        const val STATUS_LEARNING = 1
        const val STATUS_MASTERED = 2
    }
}
