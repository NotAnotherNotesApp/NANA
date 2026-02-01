package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["endTime"]),
        Index(value = ["isPinned"]),
        Index(value = ["category"])
    ]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val location: String = "", // Location for the event
    val category: String = "Event", // Category name: Class, Study, Exam, Gym, Social, etc.
    val startTime: Long,
    val endTime: Long? = null,
    val isAllDay: Boolean = false,
    val isPinned: Boolean = false,
    val reminderMinutes: String = "", // Comma-separated reminder times in minutes
    val recurrenceRule: String? = null, // RRULE format for recurring events
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
}
