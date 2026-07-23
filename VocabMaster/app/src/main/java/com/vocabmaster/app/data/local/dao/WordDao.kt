package com.vocabmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocabmaster.app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: WordEntity): Long

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun observeAll(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word LIKE :query || '%' ORDER BY word ASC LIMIT 50")
    fun search(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): WordEntity?

    /** 到期需要复习或新词，按到期时间升序。 */
    @Query(
        "SELECT * FROM words " +
            "WHERE due_at <= :now " +
            "ORDER BY (status = 0) DESC, due_at ASC " +
            "LIMIT :limit"
    )
    suspend fun getDueWords(now: Long, limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE status = :status ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomByStatus(status: Int, limit: Int): List<WordEntity>

    @Query("UPDATE words SET status = :status, ease_factor = :ease, interval_days = :interval, repetitions = :reps, due_at = :dueAt, last_reviewed_at = :reviewedAt WHERE id = :id")
    suspend fun updateReviewState(
        id: Long,
        status: Int,
        ease: Float,
        interval: Int,
        reps: Int,
        dueAt: Long,
        reviewedAt: Long
    )

    @Query("UPDATE words SET phonetic = :phonetic, part_of_speech = :partOfSpeech, definition = :definition, example = :example WHERE id = :id")
    suspend fun updateDetails(
        id: Long,
        phonetic: String?,
        partOfSpeech: String?,
        definition: String?,
        example: String?
    )

    @Query("SELECT COUNT(*) FROM words")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE status = :status")
    fun observeCountByStatus(status: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE due_at <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE definition IS NULL OR definition = ''")
    suspend fun countUnfetched(): Int
}
