package com.allubie.nana.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AppPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("nana_preferences", Context.MODE_PRIVATE)
    
    var isDarkTheme by mutableStateOf(preferences.getBoolean(DARK_THEME_KEY, false))
        private set
        
    var isAmoledTheme by mutableStateOf(preferences.getBoolean(AMOLED_THEME_KEY, false))
        private set
        
    var currency by mutableStateOf(preferences.getString(CURRENCY_KEY, "USD") ?: "USD")
        private set
        
    var is24HourFormat by mutableStateOf(preferences.getBoolean(HOUR_FORMAT_KEY, false))
        private set
        
    var notificationsEnabled by mutableStateOf(preferences.getBoolean(NOTIFICATIONS_KEY, true))
        private set
    
    var routineRemindersEnabled by mutableStateOf(preferences.getBoolean(ROUTINE_REMINDERS_KEY, true))
        private set
    
    var scheduleRemindersEnabled by mutableStateOf(preferences.getBoolean(SCHEDULE_REMINDERS_KEY, true))
        private set
    
    var defaultReminderMinutes by mutableStateOf(preferences.getInt(DEFAULT_REMINDER_MINUTES_KEY, 15))
        private set

    // Global total monthly budget cap (separate from per-category sum)
    var totalMonthlyBudget by mutableStateOf(
        preferences.getString(TOTAL_MONTHLY_BUDGET_KEY, "0.0")?.toDoubleOrNull() ?: 0.0
    )
        private set
    
    fun updateDarkTheme(enabled: Boolean) {
        isDarkTheme = enabled
        preferences.edit().putBoolean(DARK_THEME_KEY, enabled).apply()
    }
    
    fun updateAmoledTheme(enabled: Boolean) {
        isAmoledTheme = enabled
        preferences.edit().putBoolean(AMOLED_THEME_KEY, enabled).apply()
    }
    
    fun updateCurrency(newCurrency: String) {
        currency = newCurrency
        preferences.edit().putString(CURRENCY_KEY, newCurrency).apply()
    }
    
    fun updateTimeFormat(is24Hour: Boolean) {
        is24HourFormat = is24Hour
        preferences.edit().putBoolean(HOUR_FORMAT_KEY, is24Hour).apply()
    }
    
    fun updateNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        preferences.edit().putBoolean(NOTIFICATIONS_KEY, enabled).apply()
    }
    
    fun updateRoutineReminders(enabled: Boolean) {
        routineRemindersEnabled = enabled
        preferences.edit().putBoolean(ROUTINE_REMINDERS_KEY, enabled).apply()
    }
    
    fun updateScheduleReminders(enabled: Boolean) {
        scheduleRemindersEnabled = enabled
        preferences.edit().putBoolean(SCHEDULE_REMINDERS_KEY, enabled).apply()
    }
    
    fun updateDefaultReminderMinutes(minutes: Int) {
        defaultReminderMinutes = minutes
        preferences.edit().putInt(DEFAULT_REMINDER_MINUTES_KEY, minutes).apply()
    }

    fun updateTotalMonthlyBudget(value: Double) {
        totalMonthlyBudget = value
        // Store as String to avoid precision loss (SharedPreferences has no Double)
        preferences.edit().putString(TOTAL_MONTHLY_BUDGET_KEY, value.toString()).apply()
    }
    
    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
        private const val AMOLED_THEME_KEY = "amoled_theme"
        private const val CURRENCY_KEY = "currency"
        private const val HOUR_FORMAT_KEY = "hour_format_24"
        private const val NOTIFICATIONS_KEY = "notifications_enabled"
        private const val ROUTINE_REMINDERS_KEY = "routine_reminders_enabled"
        private const val SCHEDULE_REMINDERS_KEY = "schedule_reminders_enabled"
        private const val DEFAULT_REMINDER_MINUTES_KEY = "default_reminder_minutes"
        private const val TOTAL_MONTHLY_BUDGET_KEY = "total_monthly_budget"
    }
}
