package com.allubie.nana.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val richContent: String = content, // Legacy structured JSON (kept for backward compatibility)
    val htmlContent: String = richContent, // New canonical rich HTML
    val noteType: String = "text", // "text", "list", "checklist"
    val isPinned: Boolean = false,
    val category: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false
)

/** Factory helpers */
object NoteFactory {
    fun create(
        id: String,
        title: String,
        content: String,
        category: String? = null,
        rich: String = content,
        html: String = rich,
        type: String = "text"
    ) = NoteEntity(
        id = id,
        title = title,
        content = content,
        richContent = rich.ifBlank { content },
        htmlContent = html.ifBlank { rich.ifBlank { content } },
        noteType = type,
        category = category
    )
}
