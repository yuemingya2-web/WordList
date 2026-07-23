package com.vocabmaster.app.data.repository

import com.vocabmaster.app.data.local.dao.WordDao
import com.vocabmaster.app.data.local.entity.WordEntity
import com.vocabmaster.app.data.remote.DictionaryApi
import com.vocabmaster.app.data.remote.dto.DictionaryEntry
import com.vocabmaster.app.domain.VocabSeeder

/**
 * 词库仓库：负责种子词入库 + 通过 Dictionary API 抓取释义后回写本地。
 *
 * 设计要点：
 * - 词列表本身（单词字符串）随 App 内置，保证离线时仍可学习基础内容
 * - 释义/音标/例句来自在线 API（用户选择"在线API"作为词库数据源）
 * - 抓取后持久化到 Room，避免重复请求
 */
class WordRepository(
    private val wordDao: WordDao,
    private val api: DictionaryApi
) {

    /** 首次启动：将内置职场词表写入数据库（已存在的会被 IGNORE，幂等）。 */
    suspend fun seedIfEmpty() {
        val entities = VocabSeeder.words.distinct().map { WordEntity(word = it.lowercase()) }
        wordDao.insertAll(entities)
    }

    /** 列表中尚未抓取释义的单词数。 */
    suspend fun unfetchedCount(): Int = wordDao.countUnfetched()

    /**
     * 从 API 拉取释义并回写数据库。返回成功条数。
     * 单条失败不影响其它单词。
     */
    suspend fun fetchDetailsFor(batchSize: Int = 20): Int {
        val now = System.currentTimeMillis()
        // 取一批 definition 为空且未抓取过的词
        val candidates = wordDao.getDueWords(now, Int.MAX_VALUE)
            .filter { it.definition.isNullOrBlank() }
            .take(batchSize)

        var success = 0
        for (entity in candidates) {
            try {
                val entry = fetchEntry(entity.word) ?: continue
                val merged = mergeEntry(entity, entry)
                // 直接更新（不修改 SRS 状态字段）
                wordDao.updateDetails(
                    id = merged.id,
                    phonetic = merged.phonetic,
                    partOfSpeech = merged.partOfSpeech,
                    definition = merged.definition,
                    example = merged.example
                )
                success++
            } catch (_: Exception) {
                // 单个单词失败时跳过，下次重试
            }
        }
        return success
    }

    /**
     * 按需抓取单个单词（用户搜索添加）。若本地已存在则更新释义；否则新增。
     * 抛出异常时由调用方处理。
     */
    suspend fun fetchAndSave(word: String): WordEntity {
        val normalized = word.trim().lowercase()
        val entry = fetchEntry(normalized)
            ?: throw IllegalStateException("未找到单词 \"$normalized\"")

        val existing = wordDao.getByWord(normalized)
        val merged = mergeEntry(existing ?: WordEntity(word = normalized), entry)
        val id = wordDao.insert(merged).takeIf { it > 0 } ?: existing!!.id
        return wordDao.getById(id) ?: merged
    }

    private suspend fun fetchEntry(word: String): DictionaryEntry? {
        val list = api.fetchEntry(word)
        return list.firstOrNull()
    }

    private fun mergeEntry(entity: WordEntity, entry: DictionaryEntry): WordEntity {
        val phonetic = entry.phonetic
            ?: entry.phonetics?.firstOrNull { !it.text.isNullOrBlank() }?.text
        val firstMeaning = entry.meanings?.firstOrNull()
        val partOfSpeech = firstMeaning?.partOfSpeech
        val firstDef = firstMeaning?.definitions?.firstOrNull()
        val definition = firstDef?.definition
        val example = firstDef?.example
        return entity.copy(
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            definition = definition,
            example = example
        )
    }
}
