package com.vocabmaster.app.domain

import com.vocabmaster.app.data.local.entity.WordEntity

/**
 * 简化版 SM-2 间隔重复算法 (SuperMemo 2)。
 *
 * quality: 答题质量 0-5
 *   0-2 答错(几乎忘记), 3 勉强, 4 正确但有迟疑, 5 完美
 *
 * 核心规则：
 * - quality < 3：重置 repetitions=0, interval=1 (重新学习)
 * - quality >= 3：repetitions++
 *   - 第1次：interval=1 天
 *   - 第2次：interval=3 天
 *   - 之后：interval = round(previousInterval * easeFactor)
 * - easeFactor 调整：EF = EF + (0.1 - (5-q)*(0.08 + (5-q)*0.02))，最低 1.3
 * - 掌握判定：repetitions >= 5 && interval >= 21 → 已掌握
 */
object SRScheduler {

    const val QUALITY_FORGOT = 1      // 完全答错
    const val QUALITY_HARD = 3        // 勉强答对
    const val QUALITY_GOOD = 4        // 正确
    const val QUALITY_EASY = 5        // 轻松答对

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    data class ReviewResult(
        val easeFactor: Float,
        val intervalDays: Int,
        val repetitions: Int,
        val dueAt: Long,
        val status: Int
    )

    fun next(word: WordEntity, quality: Int, now: Long = System.currentTimeMillis()): ReviewResult {
        var ease = word.easeFactor
        var reps = word.repetitions
        var interval: Int

        if (quality < 3) {
            // 答错，重新开始
            reps = 0
            interval = 1
        } else {
            reps += 1
            interval = when (reps) {
                1 -> 1
                2 -> 3
                else -> ((word.intervalDays.coerceAtLeast(1)) * ease).toInt().coerceAtLeast(1)
            }
        }

        // 调整 EF
        val q = quality.coerceIn(0, 5)
        ease = (ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))).toFloat()
        if (ease < 1.3f) ease = 1.3f

        val dueAt = now + interval * DAY_MILLIS
        val status = when {
            reps >= 5 && interval >= 21 -> WordEntity.STATUS_MASTERED
            reps >= 1 -> WordEntity.STATUS_LEARNING
            else -> WordEntity.STATUS_NEW
        }

        return ReviewResult(ease, interval, reps, dueAt, status)
    }
}
