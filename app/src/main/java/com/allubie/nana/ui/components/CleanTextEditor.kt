package com.allubie.nana.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.components.richtext.*
import com.allubie.nana.ui.theme.Spacing
import java.util.*

@Composable
fun CleanTextEditor(
    blocks: List<RichTextBlock>,
    onBlocksChange: (List<RichTextBlock>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing..."
) {
    // Formatting toolbar removed for simplification
    var currentBlockIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
    val displayBlocks = blocks

    Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            displayBlocks.forEachIndexed { index, block ->
                CleanTextBlock(
                    block = block,
                    onBlockChange = { updatedBlock ->
                        val updatedBlocks = blocks.toMutableList(); updatedBlocks[index] = updatedBlock; onBlocksChange(updatedBlocks)
                    },
                    onInsertNewBlockAfter = { newBlock ->
                        val updated = blocks.toMutableList(); updated.add(index + 1, newBlock); onBlocksChange(updated)
                    },
                    onFocusGained = {
                        currentBlockIndex = index
                        // formatting ignored for now
                    },
                    onFocusLost = { blocks },
                    placeholder = if (index == 0 && blocks.size == 1) placeholder else ""
                )
            }
        }
    }
}

@Composable
private fun CleanTextBlock(
    block: RichTextBlock,
    onBlockChange: (RichTextBlock) -> Unit,
    onInsertNewBlockAfter: (RichTextBlock) -> Unit,
    onFocusGained: () -> Unit,
    onFocusLost: () -> List<RichTextBlock>,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    var text by remember(block.id) { mutableStateOf(block.text) }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(block.text) { if (text != block.text) text = block.text }

    val leadingWidth = 32.dp

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.Top) {
        Box(modifier = Modifier.width(leadingWidth).padding(top = 6.dp)) {
            if (block.type == BlockType.CHECKLIST) {
                Checkbox(checked = block.isCompleted, onCheckedChange = { checked -> onBlockChange(block.copy(isCompleted = checked)) })
            }
        }
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                val isChecklist = block.type == BlockType.CHECKLIST
                val justPressedEnterAtEnd = newText.endsWith("\n") && !text.endsWith("\n")
                if (isChecklist && justPressedEnterAtEnd) {
                    val beforeLast = newText.dropLast(1).lastOrNull()
                    if (beforeLast == '\n' || newText.trim().isEmpty()) {
                        val trimmed = newText.trimEnd('\n')
                        text = trimmed
                        onBlockChange(block.copy(text = trimmed))
                        val newBlock = block.copy(id = UUID.randomUUID().toString(), text = "", isCompleted = false)
                        onInsertNewBlockAfter(newBlock)
                    } else {
                        text = newText
                        onBlockChange(block.copy(text = newText))
                    }
                } else {
                    text = newText
                    onBlockChange(block.copy(text = newText))
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
                .onFocusChanged { focusState ->
                    val wasFocused = hasFocus
                    hasFocus = focusState.isFocused
                    if (hasFocus && !wasFocused) onFocusGained() else if (!hasFocus && wasFocused) onFocusLost()
                },
            textStyle = MaterialTheme.typography.bodyLarge.merge(block.formatting.toSpanStyle()),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.None),
            keyboardActions = KeyboardActions(),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (text.isEmpty() && placeholder.isNotEmpty()) {
                        Text(text = placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    inner()
                }
            }
        )
    }
}
