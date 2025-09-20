package com.allubie.nana.ui.components.richtext

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.theme.Spacing

@Composable
fun RichTextViewer(
    blocks: List<RichTextBlock>,
    modifier: Modifier = Modifier,
    onChecklistItemToggle: ((String, Boolean) -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.itemSpacing)
    ) {
        blocks.forEach { block ->
            if (block.type == BlockType.IMAGE && block.imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(block.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 420.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                )
            } else {
                RichTextBlockViewer(
                    block = block,
                    onToggle = onChecklistItemToggle?.let { toggle ->
                        { isChecked -> toggle(block.id, isChecked) }
                    }
                )
            }
        }
    }
}

@Composable
private fun RichTextBlockViewer(
    block: RichTextBlock,
    onToggle: ((Boolean) -> Unit)? = null
) {
    val leadingWidth = 40.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.width(leadingWidth)) {
            if (block.type == BlockType.CHECKLIST) {
                IconButton(
                    onClick = { onToggle?.invoke(!block.isCompleted) },
                    enabled = onToggle != null,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (block.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = if (block.isCompleted) "Completed" else "Not completed",
                        tint = if (block.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (block.type != BlockType.IMAGE) {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge.merge(block.formatting.toSpanStyle()).copy(
                    textDecoration = if (block.isCompleted && block.type == BlockType.CHECKLIST) {
                        TextDecoration.combine(
                            listOf(
                                TextDecoration.LineThrough,
                                block.formatting.toSpanStyle().textDecoration ?: TextDecoration.None
                            ).filter { it != TextDecoration.None }
                        )
                    } else block.formatting.toSpanStyle().textDecoration
                ),
                color = if (block.isCompleted && block.type == BlockType.CHECKLIST) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                } else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
