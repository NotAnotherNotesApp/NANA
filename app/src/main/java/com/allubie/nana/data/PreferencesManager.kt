package com.allubie.nana.data

import android.content.Context
import android.text.format.DateFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.allubie.nana.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale
import java.util.TimeZone

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nana_settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val TIMEZONE = stringPreferencesKey("timezone")
        val TOTAL_BUDGET = doublePreferencesKey("total_budget")
        val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
        
        // Get default currency from device locale
        private fun getDefaultCurrency(): Pair<String, String> {
            return try {
                val locale = Locale.getDefault()
                val currency = Currency.getInstance(locale)
                Pair(currency.currencyCode, currency.symbol)
            } catch (e: Exception) {
                Pair("USD", "$")
            }
        }
        
        // Get default timezone from device
        private fun getDefaultTimezone(): String = TimeZone.getDefault().id
    }
    
    // Cache locale defaults
    private val defaultCurrency = getDefaultCurrency()
    private val defaultTimezone = getDefaultTimezone()
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            "amoled" -> ThemeMode.AMOLED
            else -> ThemeMode.SYSTEM
        }
    }
    
    val currencyCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_CODE] ?: defaultCurrency.first
    }
    
    val currencySymbol: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_SYMBOL] ?: defaultCurrency.second
    }
    
    val timezone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TIMEZONE] ?: defaultTimezone
    }
    
    val totalBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_BUDGET] ?: 0.0
    }
    
    val use24HourFormat: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_24_HOUR_FORMAT] ?: DateFormat.is24HourFormat(context)
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.AMOLED -> "amoled"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
    
    suspend fun setCurrency(code: String, symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
            preferences[CURRENCY_SYMBOL] = symbol
        }
    }
    
    suspend fun setTimezone(timezone: String) {
        context.dataStore.edit { preferences ->
            preferences[TIMEZONE] = timezone
        }
    }
    
    suspend fun setTotalBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_BUDGET] = amount
        }
    }
    
    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_24_HOUR_FORMAT] = use24Hour
        }
    }
}
