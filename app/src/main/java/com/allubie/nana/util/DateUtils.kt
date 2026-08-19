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
