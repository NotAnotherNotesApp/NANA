package com.allubie.nana.util

import java.text.SimpleDateFormat
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
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if two Calendar instances represent the same day.
     */
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if two Date instances represent the same day.
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    
    /**
     * Get end of day (23:59:59.999) for the given calendar.
     */
    fun getEndOfDay(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
    
    /**
     * Get start of month for the given calendar.
     */
    fun getStartOfMonth(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    
    /**
     * Get start of next month for the given calendar (exclusive end for range queries).
     */
    fun getStartOfNextMonth(calendar: Calendar): Calendar {
        return getStartOfMonth(calendar).apply {
            add(Calendar.MONTH, 1)
        }
    }
}

/**
 * Centralized date formatters to avoid repeated SimpleDateFormat instantiation.
 * Note: SimpleDateFormat is not thread-safe, so these should be used within remember {} in composables.
 */
object DateFormatters {
    fun monthYear(): SimpleDateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    fun monthShort(): SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    fun dayOfMonth(): SimpleDateFormat = SimpleDateFormat("d", Locale.getDefault())
    fun dayName(): SimpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    fun dayNameFull(): SimpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    fun time(): SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    fun time12Hour(): SimpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    fun dateShort(): SimpleDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    fun dateFull(): SimpleDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    fun dateTime(): SimpleDateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
}
