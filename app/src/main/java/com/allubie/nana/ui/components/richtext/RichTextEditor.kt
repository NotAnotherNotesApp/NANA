package com.allubie.nana.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.theme.Spacing
import java.util.UUID

// Minimal RichTextEditor (lists removed). Toolbar & formatting temporarily omitted for stability.

@Composable
fun RichTextEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    blocks: List<RichTextBlock>,
    onBlocksChange: (List<RichTextBlock>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        SimpleRichTextEditor(blocks, onBlocksChange, Modifier.weight(1f))
    }
}

@Composable
fun RichTextEditor(
    blocks: List<RichTextBlock>,
    onBlocksChange: (List<RichTextBlock>) -> Unit,
    modifier: Modifier = Modifier,
) = SimpleRichTextEditor(blocks, onBlocksChange, modifier)

@Composable
fun SimpleRichTextEditor(
    blocks: List<RichTextBlock>,
    onBlocksChange: (List<RichTextBlock>) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        if (blocks.isEmpty()) {
            item {
                TextButton(onClick = {
                    onBlocksChange(listOf(RichTextBlock(UUID.randomUUID().toString(), BlockType.TEXT, "")))
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Add first block")
                }
            }
        } else {
            itemsIndexed(blocks) { index, block ->
                RichTextBlockItem(
                    block = block,
                    onBlockChange = { updated ->
                        val copy = blocks.toMutableList(); copy[index] = updated; onBlocksChange(copy)
                    },
                    onDeleteBlock = {
                        val copy = blocks.toMutableList(); copy.removeAt(index); onBlocksChange(copy)
                    },
                    onFocusGained = { /* no-op focus handling removed */ },
                    onFocusLost = { },
                    onInsertNewBlockAfter = { newBlock ->
                        val copy = blocks.toMutableList(); copy.add(index + 1, newBlock); onBlocksChange(copy)
                    }
                )
            }
            item {
                TextButton(onClick = {
                    onBlocksChange(blocks + RichTextBlock(UUID.randomUUID().toString(), BlockType.TEXT, ""))
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Add block")
                }
            }
        }
    }
}
