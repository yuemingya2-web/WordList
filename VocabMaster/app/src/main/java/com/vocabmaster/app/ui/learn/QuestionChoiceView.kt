package com.vocabmaster.app.ui.learn

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vocabmaster.app.domain.model.Word

/**
 * 选择题视图：根据 [type] 决定题面是释义还是单词。
 * - CHOICE_MEANING：题面是释义，选项是单词
 * - CHOICE_WORD：题面是单词，选项是释义
 */
@Composable
fun QuestionChoiceView(
    word: Word,
    type: QuestionType,
    options: List<String>,
    selectedIndex: Int?,
    isAnswered: Boolean,
    onPlayPronunciation: () -> Unit,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val questionText = when (type) {
        QuestionType.CHOICE_MEANING -> word.definition ?: word.word
        QuestionType.CHOICE_WORD -> word.word
        else -> word.word
    }
    val isMeaningQuestion = type == QuestionType.CHOICE_MEANING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // 题面卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMeaningQuestion) "选择对应的单词" else "选择对应的释义",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (!isMeaningQuestion) {
                    word.phonetic?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    IconButton(onClick = onPlayPronunciation) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "播放发音",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 选项
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEachIndexed { index, option ->
                OptionButton(
                    text = option,
                    isSelected = selectedIndex == index,
                    isCorrect = isOptionCorrect(word, type, option),
                    isAnswered = isAnswered,
                    onClick = { onSelect(index) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (isAnswered) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("下一题", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isAnswered && isCorrect -> MaterialTheme.colorScheme.secondary
        isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isAnswered && (isCorrect || (isSelected && !isCorrect)) -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    OutlinedButton(
        onClick = onClick,
        enabled = !isAnswered,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            if (isAnswered && isCorrect) {
                Icon(Icons.Filled.Check, contentDescription = null)
            } else if (isAnswered && isSelected && !isCorrect) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        }
    }
}

private fun isOptionCorrect(word: Word, type: QuestionType, option: String): Boolean {
    return when (type) {
        QuestionType.CHOICE_MEANING -> option == word.word
        QuestionType.CHOICE_WORD -> option == (word.definition ?: word.word)
        else -> option == word.word
    }
}
