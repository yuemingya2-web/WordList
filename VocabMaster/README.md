# VocabMaster

一款面向职场人士的英语词汇学习 Android 应用，基于 Jetpack Compose 构建。内置职场英语核心词表，结合 **SM-2 间隔重复算法**智能调度复习，支持在线释义拉取、离线学习与 TTS 发音，帮助你利用碎片时间精进职场英语。

## 功能特性

- **间隔重复学习**：采用 SM-2 算法，根据答题质量自动安排下次复习时间，高效巩固记忆
- **多种题型**：翻卡闪记、看释义选单词、看单词选释义、听音选词，多维度强化记忆
- **内置职场词表**：覆盖会议协作、项目执行、商务财务、沟通汇报、职场状态等高频场景
- **在线释义拉取**：通过 [Free Dictionary API](https://dictionaryapi.dev/) 获取释义、音标和例句并缓存到本地
- **离线学习**：词表内置，释义拉取后持久化到 Room 数据库，无网络也可学习
- **TTS 发音**：支持英式/美式口音切换与语速调节，听音辨词
- **学习统计**：首页展示待复习数量、整体掌握度及新词/学习中/已掌握三段统计
- **词库管理**：搜索浏览词库，支持手动添加单词并自动拉取释义
- **深色模式**：Material 3 主题，支持跟随系统切换浅色/深色模式

## 技术栈

| 分类 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 语言 | Kotlin |
| 数据库 | Room |
| 网络 | Retrofit + Moshi + OkHttp |
| 依赖注入 | ServiceLocator 模式（AppContainer） |
| 偏好存储 | DataStore Preferences |
| 导航 | Navigation Compose |
| 异步 | Kotlin Coroutines + Flow |
| 发音 | Android TextToSpeech |
| 算法 | SuperMemo 2 (SM-2) |

## 项目结构

```
app/src/main/java/com/vocabmaster/app/
├── MainActivity.kt              # 入口 Activity
├── VocabMasterApp.kt            # Application + AppContainer 依赖容器
├── data/
│   ├── local/                   # Room 数据库、DAO、实体
│   │   ├── AppDatabase.kt
│   │   ├── dao/WordDao.kt
│   │   └── entity/WordEntity.kt
│   ├── remote/                  # Dictionary API 接口与 DTO
│   │   ├── DictionaryApi.kt
│   │   └── dto/DictionaryEntry.kt
│   ├── repository/              # 数据仓库层
│   │   ├── LearningRepository.kt
│   │   └── WordRepository.kt
│   └── settings/                # DataStore 偏好存储
│       └── SettingsStore.kt
├── domain/                      # 领域层
│   ├── SRScheduler.kt           # SM-2 间隔重复算法
│   ├── VocabSeeder.kt           # 内置职场词表
│   └── model/Word.kt            # 领域模型
├── tts/
│   └── TtsManager.kt            # TextToSpeech 封装
└── ui/
    ├── ViewModelFactories.kt    # ViewModel 工厂
    ├── navigation/               # 导航与底部导航
    ├── home/                    # 首页（统计 + 开始学习）
    ├── learn/                   # 学习页（翻卡/选择题/听音题）
    ├── library/                 # 词库管理
    ├── settings/                # 设置（口音/语速）
    └── theme/                   # Material 3 主题与排版
```

## SM-2 间隔重复算法

应用使用简化版 SuperMemo 2 算法调度复习：

- **答题质量**：0-2 答错（重置）、3 勉强、4 正确、5 完美
- **间隔规则**：答错重置为 1 天；首次正确间隔 1 天，第二次 3 天，之后按 `间隔 × 难度系数` 递增
- **难度系数**：根据答题质量动态调整，最低 1.3
- **掌握判定**：连续复习 ≥5 次且间隔 ≥21 天标记为"已掌握"

## 环境要求

- Android Studio (Hedgehog 或更高)
- JDK 17
- Android SDK 34（最低支持 Android 10 / API 29）
- Gradle 8.7（项目自带 Wrapper）

## 构建与运行

```bash
# 使用 Gradle Wrapper 构建 Debug APK
./gradlew assembleDebug

# 安装到已连接设备
./gradlew installDebug
```

或在 Android Studio 中直接打开项目后点击 Run。

## 数据来源

- **内置词表**：应用内置职场英语高频词汇，首次启动自动入库
- **释义/音标/例句**：来自 [Free Dictionary API](https://dictionaryapi.dev/)，拉取后缓存到本地 Room 数据库，后续离线可用

## 许可证

本项目仅供学习交流使用。
