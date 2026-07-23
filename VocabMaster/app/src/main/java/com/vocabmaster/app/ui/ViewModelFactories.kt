package com.vocabmaster.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vocabmaster.app.appContainer
import com.vocabmaster.app.ui.home.HomeViewModel
import com.vocabmaster.app.ui.learn.LearnViewModel
import com.vocabmaster.app.ui.library.LibraryViewModel
import com.vocabmaster.app.ui.settings.SettingsViewModel

/**
 * 集中构造所有 ViewModel 的工厂，避免每个页面重复样板代码。
 * 通过 androidx.lifecycle.viewmodel.viewModelFactory + initializer 实现。
 * 从 CreationExtras 中取 Application，再访问 AppContainer。
 */
object ViewModelFactories {

    val Home: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
            val container = app.appContainer
            HomeViewModel(container.wordRepository, container.database.wordDao())
        }
    }

    val Library: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
            val container = app.appContainer
            LibraryViewModel(container.database.wordDao(), container.wordRepository)
        }
    }

    val Settings: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
            val container = app.appContainer
            SettingsViewModel(container.settingsStore, container.ttsManager)
        }
    }

    val Learn: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
            val container = app.appContainer
            LearnViewModel(container.learningRepository, container.wordRepository, container.ttsManager)
        }
    }
}
