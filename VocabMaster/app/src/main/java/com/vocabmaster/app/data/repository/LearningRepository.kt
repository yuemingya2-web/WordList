package com.vocabmaster.app.data.repository

import com.vocabmaster.app.data.local.dao.WordDao
import com.vocabmaster.app.data.local.entity.WordEntity
import com.vocabmaster.app.domain.SRScheduler
import com.vocabmaster.app.domain.model.Word

/**
 * 学习仓库：负责调度今日学习卡片、应用间隔重复算法更新单词状态。
 */
class LearningRepository(
    private val wordDao: WordDao
) {

    /** 一次学习会话的卡片数。 */
    private val sessionSize = 12

    /**
     * 取出本次学习会话的卡片：
     * 优先返回到期复习 + 未学新词，总数不超过 sessionSize。
     */
    suspend fun fetchSession(now: Long = System.currentTimeMillis()): List<Word> {
        val due = wordDao.getDueWords(now, sessionSize)
        // 不足则补充未掌握的随机词
        if (due.size < sessionSize) {
            val need = sessionSize - due.size
            val exclude = due.map { it.id }
            val extra = wordDao.getRandomByStatus(WordEntity.STATUS_LEARNING, need * 3)
                .filter { it.id !in exclude }
                .take(need)
            due + extra
        } else {
            due
        }.map { it.toDomain() }
    }

    /**
     * 提交一次答题结果。quality 见 [SRScheduler]。
     */
    suspend fun submitReview(wordId: Long, quality: Int) {
        val word = wordDao.getById(wordId) ?: return
        val result = SRScheduler.next(word, quality)
        wordDao.updateReviewState(
            id = word.id,
            status = result.status,
            ease = result.easeFactor,
            interval = result.intervalDays,
            reps = result.repetitions,
            dueAt = result.dueAt,
            reviewedAt = System.currentTimeMillis()
        )
    }

    /** 用于选择题/听音选词的干扰项候选：从词库随机取若干释义非空的词。 */
    suspend fun fetchDistractors(excludeId: Long, count: Int): List<Word> {
        // 复用 getDueWords 取一批，再过滤
        val pool = wordDao.getRandomByStatus(WordEntity.STATUS_LEARNING, count * 5)
            .filter { it.id != excludeId && !it.definition.isNullOrBlank() }
            .take(count)
            .map { it.toDomain() }
        if (pool.size >= count) return pool
        // 不够时再从全部词补
        val all = wordDao.getDueWords(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000, count * 5)
            .filter { it.id != excludeId && !it.definition.isNullOrBlank() }
            .take(count - pool.size)
            .map { it.toDomain() }
        return (pool + all).distinctBy { it.id }
    }

    private fun WordEntity.toDomain() = Word(
        id = id,
        word = word,
        phonetic = phonetic,
        partOfSpeech = partOfSpeech,
        definition = definition,
        example = example,
        exampleTranslation = exampleTranslation,
        status = status,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitions = repetitions,
        dueAt = dueAt
    )
}
