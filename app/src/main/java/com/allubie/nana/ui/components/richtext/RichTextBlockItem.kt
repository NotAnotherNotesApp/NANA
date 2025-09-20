package com.allubie.nana.ui.components.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.theme.Spacing
import java.util.*

@Composable
fun RichTextBlockItem(
    block: RichTextBlock,
    onBlockChange: (RichTextBlock) -> Unit,
    onDeleteBlock: () -> Unit,
    onFocusGained: () -> Unit,
    onFocusLost: () -> Unit,
    onInsertNewBlockAfter: (RichTextBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(block.id) { mutableStateOf(block.text) }
    var hasFocus by remember { mutableStateOf(false) }

    // Update text when block changes externally
    LaunchedEffect(block.text) {
        if (text != block.text) {
            text = block.text
        }
    }

    // Reserve consistent leading width so text across different block types aligns nicely.
    // Slightly wider to accommodate 2+ digit numbers without shifting text.
    val leadingWidth = 40.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(leadingWidth)
                .padding(top = 6.dp),
        ) {
            when (block.type) {
                BlockType.CHECKLIST -> {
                    Checkbox(
                        checked = block.isCompleted,
                        onCheckedChange = { isChecked ->
                            onBlockChange(block.copy(isCompleted = isChecked))
                        }
                    )
                }
                else -> { /* Spacer only */ }
            }
        }

        // Text field (multiline, Enter inserts newline)
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                val isChecklist = block.type == BlockType.CHECKLIST
                val justPressedEnterAtEnd = newText.endsWith("\n") && !text.endsWith("\n")
                if (isChecklist && justPressedEnterAtEnd) {
                    // If previous char (before the newly added newline) is already a newline OR trimmed text blank => create new block
                    val beforeLast = newText.dropLast(1).lastOrNull()
                    if (beforeLast == '\n' || newText.trim().isEmpty()) {
                        val trimmed = newText.trimEnd('\n')
                        text = trimmed
                        onBlockChange(block.copy(text = trimmed))
                        val newBlock = block.copy(id = UUID.randomUUID().toString(), text = "", isCompleted = false)
                        onInsertNewBlockAfter(newBlock)
                    } else {
                        // First Enter: keep newline inside same list item (multiline list item)
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
                .padding(vertical = 4.dp)
                .onFocusChanged { focusState ->
                    val wasFocused = hasFocus
                    hasFocus = focusState.isFocused
                    if (hasFocus && !wasFocused) onFocusGained() else if (!hasFocus && wasFocused) onFocusLost()
                },
            textStyle = MaterialTheme.typography.bodyLarge.merge(
                block.formatting.toSpanStyle()
            ).copy(
                textDecoration = if (block.isCompleted && block.type == BlockType.CHECKLIST) {
                    TextDecoration.combine(
                        listOfNotNull(TextDecoration.LineThrough)
                    )
                } else {
                    block.formatting.toSpanStyle().textDecoration
                }
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.None // ensure Enter inserts newline instead of IME action
            ),
            keyboardActions = KeyboardActions(),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (text.isEmpty()) {
                        Text(
                            text = if (block.type == BlockType.CHECKLIST) "Checklist item" else "Write something...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (hasFocus) {
            IconButton(
                onClick = onDeleteBlock,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
