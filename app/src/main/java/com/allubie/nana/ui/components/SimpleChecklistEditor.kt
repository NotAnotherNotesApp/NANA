package com.allubie.nana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.theme.Spacing
import java.util.*

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isCompleted: Boolean = false
)

@Composable
fun SimpleChecklistEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    items: List<ChecklistItem>,
    onItemsChange: (List<ChecklistItem>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Add item..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        // Checklist Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            itemsIndexed(items) { index, item ->
                ChecklistItemRow(
                    item = item,
                    onItemChange = { updatedItem ->
                        val updatedItems = items.toMutableList()
                        updatedItems[index] = updatedItem
                        onItemsChange(updatedItems)
                    },
                    onDeleteItem = {
                        val updatedItems = items.toMutableList()
                        updatedItems.removeAt(index)
                        onItemsChange(updatedItems)
                    }
                )
            }
            
            // Add item button
            item {
                // Left-aligned add row with consistent start padding
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = {
                        val newItem = ChecklistItem()
                        onItemsChange(items + newItem)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text("Add item")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onItemChange: (ChecklistItem) -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(item.id) { mutableStateOf(item.text) }
    
    LaunchedEffect(item.text) {
        if (text != item.text) {
            text = item.text
        }
    }

    // Standardized gutter to align with bullets/numbers/checkbox
    val leadingWidth = 40.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(leadingWidth)) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { isChecked -> onItemChange(item.copy(isCompleted = isChecked)) },
                modifier = Modifier.size(28.dp)
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onItemChange(item.copy(text = newText))
            },
            placeholder = { Text("Add item...") },
            modifier = Modifier
                .weight(1f),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            textStyle = LocalTextStyle.current.copy(
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
        )
        IconButton(
            onClick = onDeleteItem,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
