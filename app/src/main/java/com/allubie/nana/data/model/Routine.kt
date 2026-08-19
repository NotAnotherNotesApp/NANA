package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ],
    foreignKeys = [ForeignKey(
        entity = Routine::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.CASCADE
    )]
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

/**
 * Checks if this routine is scheduled for the given date based on its [daysOfWeek].
 * Model days: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun.
 */
fun Routine.isScheduledFor(date: java.util.Date): Boolean {
    if (!this.isActive) return false
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val modelDayOfWeek = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> 1
        java.util.Calendar.TUESDAY -> 2
        java.util.Calendar.WEDNESDAY -> 3
        java.util.Calendar.THURSDAY -> 4
        java.util.Calendar.FRIDAY -> 5
        java.util.Calendar.SATURDAY -> 6
        java.util.Calendar.SUNDAY -> 7
        else -> 1
    }
    val days = this.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
    if (days.isEmpty()) return true
    return modelDayOfWeek in days
}

fun Routine.isScheduledFor(timestamp: Long): Boolean {
    return isScheduledFor(java.util.Date(timestamp))
}

