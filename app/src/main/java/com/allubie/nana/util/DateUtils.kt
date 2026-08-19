package com.allubie.nana.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Centralized date utilities and formatters for consistent date handling across the app.
 */
object DateUtils {
    
    /**
     * Check if two timestamps represent the same calendar day.
     */
    fun isSameDay(date1: Long, date2: Long): Boolean {
        val ld1 = Instant.ofEpochMilli(date1).atZone(ZoneId.systemDefault()).toLocalDate()
        val ld2 = Instant.ofEpochMilli(date2).atZone(ZoneId.systemDefault()).toLocalDate()
        return ld1 == ld2
    }
    
    /**
     * Check if two Calendar instances represent the same day.
     */
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return isSameDay(cal1.timeInMillis, cal2.timeInMillis)
    }
    
    /**
     * Check if two Date instances represent the same day.
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        return isSameDay(date1.time, date2.time)
    }
    
    /**
     * Check if the given timestamp is today.
     */
    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }
    
    /**
     * Get start of day (midnight) for the given calendar.
     */
    fun getStartOfDay(calendar: Calendar): Calendar {
        val date = Instant.ofEpochMilli(calendar.timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Calendar.getInstance().apply { timeInMillis = startOfDay.toEpochMilli() }
    }
    
    fun getStartOfDay(timestamp: Long): Long {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    /**
     * Get end of day (23:59:59.999) for the given calendar.
     */
    fun getEndOfDay(calendar: Calendar): Calendar {
        val date = Instant.ofEpochMilli(calendar.timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1_000_000).toInstant()
        return Calendar.getInstance().apply { timeInMillis = endOfDay.toEpochMilli() }
    }
    
    fun getEndOfDay(timestamp: Long): Long {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1_000_000).toInstant().toEpochMilli()
    }
    
    /**
     * Get start of month for the given calendar.
     */
    fun getStartOfMonth(calendar: Calendar): Calendar {
        val date = Instant.ofEpochMilli(calendar.timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Calendar.getInstance().apply { timeInMillis = startOfMonth.toEpochMilli() }
    }
    
    fun getStartOfMonth(timestamp: Long): Long {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    /**
     * Get start of next month for the given calendar (exclusive end for range queries).
     */
    fun getStartOfNextMonth(calendar: Calendar): Calendar {
        val date = Instant.ofEpochMilli(calendar.timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfNextMonth = date.withDayOfMonth(1).plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        return Calendar.getInstance().apply { timeInMillis = startOfNextMonth.toEpochMilli() }
    }
    
    fun getStartOfNextMonth(timestamp: Long): Long {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.withDayOfMonth(1).plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * Centralized date formatters using immutable DateTimeFormatter.
 * DateTimeFormatter is thread-safe, so these are properties.
 */
object DateFormatters {
    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val monthShort: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    val dayOfMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())
    val dayName: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    val dayNameFull: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
    val time: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val time12Hour: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val dateShort: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val dateFull: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    val dateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.getDefault())
}

/**
 * Checks if an event occurs on a target date.
 * Handles single-day, multi-day, and recurring events (Daily, Weekly, Monthly, Yearly).
 */
fun com.allubie.nana.data.model.Event.occursOn(targetDate: Long): Boolean {
    val targetStartOfDay = DateUtils.getStartOfDay(targetDate)
    val eventStartOfDay = DateUtils.getStartOfDay(this.startTime)

    // Events cannot occur before their start date
    if (targetStartOfDay < eventStartOfDay) return false

    val rule = this.recurrenceRule?.trim()
    if (rule.isNullOrEmpty() || rule.equals("Never", ignoreCase = true) || rule.equals("none", ignoreCase = true)) {
        val eventEndOfDay = if (this.endTime != null && this.endTime > this.startTime) {
            DateUtils.getStartOfDay(this.endTime)
        } else {
            eventStartOfDay
        }
        return targetStartOfDay in eventStartOfDay..eventEndOfDay
    }

    val calTarget = Calendar.getInstance().apply { timeInMillis = targetDate }
    val calEvent = Calendar.getInstance().apply { timeInMillis = this@occursOn.startTime }

    return when (rule.lowercase()) {
        "daily", "every day", "everyday" -> true
        "weekly", "every week" -> {
            calTarget.get(Calendar.DAY_OF_WEEK) == calEvent.get(Calendar.DAY_OF_WEEK)
        }
        "monthly", "every month" -> {
            val eventDom = calEvent.get(Calendar.DAY_OF_MONTH)
            val maxDomInTargetMonth = calTarget.getActualMaximum(Calendar.DAY_OF_MONTH)
            val targetDom = calTarget.get(Calendar.DAY_OF_MONTH)
            if (eventDom > maxDomInTargetMonth) {
                targetDom == maxDomInTargetMonth
            } else {
                targetDom == eventDom
            }
        }
        "yearly", "every year", "annually" -> {
            calTarget.get(Calendar.MONTH) == calEvent.get(Calendar.MONTH) &&
            calTarget.get(Calendar.DAY_OF_MONTH) == calEvent.get(Calendar.DAY_OF_MONTH)
        }
        else -> false
    }
}

/**
 * Projects an event's startTime and endTime to match the specified targetDate,
 * preserving the original time-of-day (hours, minutes, seconds) and event duration.
 */
fun com.allubie.nana.data.model.Event.projectForDay(targetDate: Long): com.allubie.nana.data.model.Event {
    if (DateUtils.isSameDay(this.startTime, targetDate)) return this

    val calEvent = Calendar.getInstance().apply { timeInMillis = this@projectForDay.startTime }
    val calTarget = Calendar.getInstance().apply {
        timeInMillis = targetDate
        set(Calendar.HOUR_OF_DAY, calEvent.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, calEvent.get(Calendar.MINUTE))
        set(Calendar.SECOND, calEvent.get(Calendar.SECOND))
        set(Calendar.MILLISECOND, calEvent.get(Calendar.MILLISECOND))
    }
    val projectedStartTime = calTarget.timeInMillis
    val duration = if (this.endTime != null && this.endTime > this.startTime) {
        this.endTime - this.startTime
    } else null
    val projectedEndTime = duration?.let { projectedStartTime + it }

    return this.copy(
        startTime = projectedStartTime,
        endTime = projectedEndTime
    )
}
