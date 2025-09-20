package com.allubie.nana.ui.components.richtext

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Stable
data class RichTextBlock(
    val id: String,
    val type: BlockType,
    val text: String,
    val isCompleted: Boolean = false,
    val formatting: TextFormatting = TextFormatting(),
    val listIndex: Int = 0,
    val imageUri: String? = null
)

enum class BlockType {
    TEXT,
    CHECKLIST,
    IMAGE
}

@Stable
data class TextFormatting(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val isStrikethrough: Boolean = false
) {
    fun toSpanStyle(): SpanStyle {
        return SpanStyle(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = when {
                isStrikethrough && isUnderlined -> TextDecoration.combine(
                    listOf(TextDecoration.LineThrough, TextDecoration.Underline)
                )
                isStrikethrough -> TextDecoration.LineThrough
                isUnderlined -> TextDecoration.Underline
                else -> TextDecoration.None
            }
        )
    }
}

@Stable
data class RichTextState(
    val blocks: List<RichTextBlock> = emptyList(),
    val currentSelection: Int = 0,
    val currentFormatting: TextFormatting = TextFormatting()
)
