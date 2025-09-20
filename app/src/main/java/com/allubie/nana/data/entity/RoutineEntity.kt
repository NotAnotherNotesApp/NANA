package com.allubie.nana.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val frequency: String, // "Daily", "Weekly", etc.
    val isPinned: Boolean = false,
    val reminderTime: String? = null,
    val createdAt: Instant = Clock.System.now(),
    val isActive: Boolean = true
)

object RoutineFactory {
    fun create(
        id: String,
        title: String,
        description: String,
        frequency: String,
        reminderTime: String? = null
    ) = RoutineEntity(id, title, description, frequency, reminderTime = reminderTime)
}
