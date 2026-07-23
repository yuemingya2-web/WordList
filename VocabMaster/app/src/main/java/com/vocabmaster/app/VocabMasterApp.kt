package com.vocabmaster.app

import android.app.Application
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vocabmaster.app.data.local.AppDatabase
import com.vocabmaster.app.data.remote.DictionaryApi
import com.vocabmaster.app.data.repository.LearningRepository
import com.vocabmaster.app.data.repository.WordRepository
import com.vocabmaster.app.data.settings.SettingsStore
import com.vocabmaster.app.tts.TtsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 应用入口与简易依赖容器（ServiceLocator 模式）。
 *
 * 不引入 Hilt，保持项目最小依赖。所有单例在 Application 创建时初始化，
 * ViewModel 通过 [appContainer] 获取。
 */
class VocabMasterApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

/** 通过 Context 取得 [AppContainer] 的便捷扩展。 */
val Context.appContainer: AppContainer
    get() = (applicationContext as VocabMasterApp).appContainer

class AppContainer(context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.dictionaryapi.dev/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val dictionaryApi: DictionaryApi = retrofit.create(DictionaryApi::class.java)

    val database: AppDatabase = AppDatabase.getInstance(context)

    val wordRepository: WordRepository = WordRepository(database.wordDao(), dictionaryApi)

    val learningRepository: LearningRepository = LearningRepository(database.wordDao())

    val settingsStore: SettingsStore = SettingsStore(context)

    val ttsManager: TtsManager = TtsManager(context)
}
