package com.vocabmaster.app.ui.library

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocabmaster.app.appContainer
import com.vocabmaster.app.domain.model.Word
import com.vocabmaster.app.ui.ViewModelFactories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = ViewModelFactories.Library)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var newWord by remember { mutableStateOf("") }

    LaunchedEffect(state.addResult) {
        state.addResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeAddResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词库", fontWeight = FontWeight.SemiBold) },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("添加单词") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索框
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索单词") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            if (state.words.isEmpty()) {
                EmptyLibrary()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.words, key = { it.id }) { word ->
                        WordRow(word = word)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWordDialog(
            text = newWord,
            onTextChange = { newWord = it },
            isAdding = state.isAdding,
            onConfirm = {
                viewModel.addWord(newWord)
                newWord = ""
                showAddDialog = false
            },
            onDismiss = {
                showAddDialog = false
                newWord = ""
            }
        )
    }
}

@Composable
private fun WordRow(word: Word) {
    val ttsManager = LocalContextSafe()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    word.phonetic?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    label = { Text(word.statusLabel, style = MaterialTheme.typography.labelSmall) }
                )
                IconButton(onClick = { ttsManager.speak(word.word) }) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = "播放发音",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            word.definition?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            word.example?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "\"$it\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "词库为空",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "点击右下角添加单词，或在首页下拉刷新拉取内置词表",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddWordDialog(
    text: String,
    onTextChange: (String) -> Unit,
    isAdding: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!isAdding) onDismiss() },
        title = { Text("添加单词") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    placeholder = { Text("输入英文单词") },
                    enabled = !isAdding
                )
                if (isAdding) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                        Text("正在查询释义…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                enabled = text.isNotBlank() && !isAdding
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                enabled = !isAdding
            ) {
                Text("取消")
            }
        }
    )
}

/** 取得全局 TTS 单例，用于列表项快速播放发音。 */
@Composable
private fun LocalContextSafe(): com.vocabmaster.app.tts.TtsManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return context.appContainer.ttsManager
}
