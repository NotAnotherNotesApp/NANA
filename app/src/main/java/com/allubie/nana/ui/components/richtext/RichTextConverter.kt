package com.allubie.nana.ui.components.richtext

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SerializableRichTextBlock(
    val id: String,
    val type: String,
    val text: String,
    val isCompleted: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val isStrikethrough: Boolean = false,
    val imageUri: String? = null
)

object RichTextConverter {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun blocksToJson(blocks: List<RichTextBlock>): String = try {
        val serializableBlocks = blocks.map { block ->
            SerializableRichTextBlock(
                id = block.id,
                type = block.type.name,
                text = block.text,
                isCompleted = block.isCompleted,
                isBold = block.formatting.isBold,
                isItalic = block.formatting.isItalic,
                isUnderlined = block.formatting.isUnderlined,
                isStrikethrough = block.formatting.isStrikethrough,
                imageUri = block.imageUri
            )
        }
        json.encodeToString(serializableBlocks)
    } catch (e: Exception) {
        blocksToPlainText(blocks)
    }

    fun jsonToBlocks(jsonString: String?): List<RichTextBlock> {
        if (jsonString.isNullOrEmpty()) return listOf(RichTextBlock(id = "block_0", type = BlockType.TEXT, text = ""))
        return try {
            val serializableBlocks = json.decodeFromString<List<SerializableRichTextBlock>>(jsonString)
            serializableBlocks.map { s ->
                RichTextBlock(
                    id = s.id,
                    type = BlockType.valueOf(s.type),
                    text = s.text,
                    isCompleted = s.isCompleted,
                    formatting = TextFormatting(
                        isBold = s.isBold,
                        isItalic = s.isItalic,
                        isUnderlined = s.isUnderlined,
                        isStrikethrough = s.isStrikethrough
                    ),
                    imageUri = s.imageUri
                )
            }
        } catch (e: Exception) {
            plainTextToBlocks(jsonString)
        }
    }

    fun blocksToPlainText(blocks: List<RichTextBlock>): String = blocks.joinToString("\n") { block ->
        when (block.type) {
            BlockType.CHECKLIST -> {
                val checkbox = if (block.isCompleted) "☑" else "☐"; "$checkbox ${block.text}".trimEnd()
            }
            else -> block.text
        }
    }

    fun plainTextToBlocks(plainText: String?): List<RichTextBlock> {
        if (plainText.isNullOrBlank()) return listOf(RichTextBlock(id = "block_0", type = BlockType.TEXT, text = ""))
        return try {
            plainText.split("\n").mapIndexed { index, line ->
                when {
                    line.startsWith("☑ ") || line.startsWith("☐ ") -> RichTextBlock(
                        id = "block_$index",
                        type = BlockType.CHECKLIST,
                        text = line.drop(2),
                        isCompleted = line.startsWith("☑")
                    )
                    else -> RichTextBlock(id = "block_$index", type = BlockType.TEXT, text = line)
                }
            }.filter { it.text.isNotBlank() }.ifEmpty { listOf(RichTextBlock(id = "block_0", type = BlockType.TEXT, text = "")) }
        } catch (e: Exception) {
            listOf(RichTextBlock(id = "block_0", type = BlockType.TEXT, text = ""))
        }
    }
}
