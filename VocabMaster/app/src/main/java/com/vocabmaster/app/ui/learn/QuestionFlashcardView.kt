package com.vocabmaster.app.ui.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vocabmaster.app.domain.model.Word

/**
 * 翻卡闪记视图：点击翻转查看释义，然后自评掌握度。
 */
@Composable
fun QuestionFlashcardView(
    word: Word,
    onPlayPronunciation: () -> Unit,
    onRate: (quality: Int) -> Unit
) {
    var revealed by remember(word.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 单词
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    // 音标
                    word.phonetic?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 发音按钮
                    IconButton(onClick = onPlayPronunciation) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "播放发音",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(visible = revealed, enter = fadeIn(), exit = fadeOut()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            word.partOfSpeech?.let { pos ->
                                Text(
                                    text = pos,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            Text(
                                text = word.definition ?: "(暂无释义)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            word.example?.let { ex ->
                                Spacer(Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "\"$ex\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (!revealed) {
                        Text(
                            text = "点击下方按钮查看释义",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!revealed) {
            Button(
                onClick = { revealed = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("显示答案", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Text(
                text = "你掌握了吗？",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingButton("忘记", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError) {
                    onRate(1)
                }
                RatingButton("困难", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary) {
                    onRate(3)
                }
                RatingButton("良好", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary) {
                    onRate(4)
                }
                RatingButton("简单", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary) {
                    onRate(5)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RatingButton(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
