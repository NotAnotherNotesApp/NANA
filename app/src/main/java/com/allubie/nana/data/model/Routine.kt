package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RoutineType {
    SIMPLE,      // Just mark as done
    COUNTER,     // Count-based (e.g., water cups)
    TIMER        // Duration-based (e.g., 30 min reading)
}

@Entity(
    tableName = "routines",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["isPinned"])
    ]
)
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val iconName: String = "check_circle",
    val color: Int = 0,
    val reminderTime: String? = null, // HH:mm format
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Comma-separated: 1=Mon, 7=Sun
    val scheduledDays: String = "0,1,2,3,4,5,6", // Comma-separated: 0=Sun, 6=Sat (for AlarmManager)
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val currentStreak: Int = 0,
    val routineType: String = "SIMPLE", // SIMPLE, COUNTER, TIMER
    val targetCount: Int = 1, // For COUNTER type (e.g., 8 cups of water)
    val durationMinutes: Int = 0, // For TIMER type (e.g., 30 minutes)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_completions",
    indices = [
        Index(value = ["routineId"]),
        Index(value = ["date"]),
        Index(value = ["routineId", "date"], unique = true)
    ]
)
data class RoutineCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val date: String, // yyyy-MM-dd format
    val isCompleted: Boolean = false, // Whether the routine is fully completed for the day
    val currentCount: Int = 0, // For counter routines
    val elapsedSeconds: Int = 0, // For timer routines
    val completedAt: Long = System.currentTimeMillis()
)
