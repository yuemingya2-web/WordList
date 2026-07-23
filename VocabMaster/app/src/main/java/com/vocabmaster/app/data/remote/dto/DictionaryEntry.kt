package com.vocabmaster.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Free Dictionary API (https://api.dictionaryapi.dev/api/v2/entries/en/{word}) 响应。
 * 响应是一个数组，取第一个元素。
 */
@JsonClass(generateAdapter = true)
data class DictionaryEntry(
    @Json(name = "word") val word: String? = null,
    @Json(name = "phonetic") val phonetic: String? = null,
    @Json(name = "phonetics") val phonetics: List<Phonetic>? = null,
    @Json(name = "meanings") val meanings: List<Meaning>? = null
)

@JsonClass(generateAdapter = true)
data class Phonetic(
    @Json(name = "text") val text: String? = null,
    @Json(name = "audio") val audio: String? = null
)

@JsonClass(generateAdapter = true)
data class Meaning(
    @Json(name = "partOfSpeech") val partOfSpeech: String? = null,
    @Json(name = "definitions") val definitions: List<Definition>? = null
)

@JsonClass(generateAdapter = true)
data class Definition(
    @Json(name = "definition") val definition: String? = null,
    @Json(name = "example") val example: String? = null
)
