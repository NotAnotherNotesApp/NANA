package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["isDeleted", "isArchived"]),
        Index(value = ["isPinned"]),
        Index(value = ["updatedAt"])
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "", // Now stores HTML from rich text editor
    val color: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isChecklist: Boolean = false,
    val labels: String = "", // Comma-separated labels
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "note_images",
    indices = [Index(value = ["noteId"])]
)
data class NoteImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val imagePath: String, // Internal storage path
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "checklist_items",
    indices = [Index(value = ["noteId"])]
)
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0
)
