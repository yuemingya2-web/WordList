package com.vocabmaster.app.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocabmaster.app.ui.ViewModelFactories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onExit: () -> Unit,
    viewModel: LearnViewModel = viewModel(factory = ViewModelFactories.Learn)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onExit() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "学习中",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${state.currentIndex + 1} / ${state.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 进度条
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
            )

            when {
                state.isLoading -> LoadingView()
                state.isComplete -> CompleteView(
                    correct = state.correctCount,
                    total = state.totalCount,
                    errorMessage = state.errorMessage,
                    onExit = onExit,
                    onRestart = { viewModel.restart() }
                )
                state.errorMessage != null && state.currentCard == null -> ErrorView(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.restart() },
                    onExit = onExit
                )
                else -> {
                    val card = state.currentCard
                    if (card != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 12.dp)
                        ) {
                            when (card.type) {
                                QuestionType.FLASHCARD -> QuestionFlashcardView(
                                    word = card.word,
                                    onPlayPronunciation = { viewModel.playPronunciation() },
                                    onRate = { quality -> viewModel.submitFlashcard(quality) }
                                )
                                QuestionType.CHOICE_MEANING, QuestionType.CHOICE_WORD -> QuestionChoiceView(
                                    word = card.word,
                                    type = card.type,
                                    options = card.options,
                                    selectedIndex = state.selectedIndex,
                                    isAnswered = state.isAnswered,
                                    onPlayPronunciation = { viewModel.playPronunciation() },
                                    onSelect = { idx -> viewModel.submitChoice(idx) },
                                    onNext = { viewModel.nextQuestion() }
                                )
                                QuestionType.LISTEN -> QuestionListenView(
                                    word = card.word,
                                    options = card.options,
                                    selectedIndex = state.selectedIndex,
                                    isAnswered = state.isAnswered,
                                    onPlay = { viewModel.playPronunciation() },
                                    onSelect = { idx -> viewModel.submitChoice(idx) },
                                    onNext = { viewModel.nextQuestion() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                "正在准备单词…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onExit) { Text("返回") }
            Button(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
private fun CompleteView(
    correct: Int,
    total: Int,
    errorMessage: String?,
    onExit: () -> Unit,
    onRestart: () -> Unit
) {
    val accuracy = if (total == 0) 0 else (correct.toFloat() / total * 100).toInt()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (errorMessage != null) errorMessage else "本次学习完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        if (errorMessage == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "正确 $correct / $total  ·  正确率 $accuracy%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("再来一组")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("返回首页")
        }
    }
}
