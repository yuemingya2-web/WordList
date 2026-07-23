package com.vocabmaster.app.data.remote

import com.vocabmaster.app.data.remote.dto.DictionaryEntry
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    /**
     * Free Dictionary API：返回一个数组，包含释义、音标、例句。
     * 文档：https://dictionaryapi.dev/
     */
    @GET("api/v2/entries/en/{word}")
    suspend fun fetchEntry(@Path("word") word: String): List<DictionaryEntry>
}
