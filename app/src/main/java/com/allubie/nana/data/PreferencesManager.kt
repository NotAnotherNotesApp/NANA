package com.allubie.nana.data

import android.content.Context
import android.text.format.DateFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.allubie.nana.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
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
        
        private val knownCurrencySymbols = mapOf(
            "USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥", "CNY" to "¥",
            "INR" to "₹", "CAD" to "C$", "AUD" to "A$", "CHF" to "Fr", "SEK" to "kr",
            "NOK" to "kr", "DKK" to "kr", "PLN" to "zł", "CZK" to "Kč", "HUF" to "Ft",
            "TRY" to "₺", "RUB" to "₽", "BRL" to "R$", "MXN" to "$", "ARS" to "$",
            "COP" to "$", "CLP" to "$", "ZAR" to "R", "NGN" to "₦", "EGP" to "E£",
            "KES" to "KSh", "GHS" to "₵", "AED" to "د.إ", "SAR" to "﷼", "QAR" to "﷼",
            "KWD" to "د.ك", "THB" to "฿", "MYR" to "RM", "SGD" to "S$", "IDR" to "Rp",
            "PHP" to "₱", "VND" to "₫", "PKR" to "₨", "LKR" to "₨", "TWD" to "NT$",
            "HKD" to "HK$", "NZD" to "NZ$"
        )

        private fun resolveCurrencySymbol(code: String, rawSymbol: String?): String {
            val cleanCode = code.trim().uppercase()
            if (!rawSymbol.isNullOrBlank() && rawSymbol != cleanCode) {
                return rawSymbol
            }
            val mappedSymbol = knownCurrencySymbols[cleanCode]
            if (mappedSymbol != null) {
                return mappedSymbol
            }
            return try {
                val currency = Currency.getInstance(cleanCode)
                val symbol = currency.getSymbol(Locale.US)
                if (symbol.isNotBlank() && symbol != cleanCode) symbol else "$"
            } catch (e: Exception) {
                "$"
            }
        }
        
        // Get default currency from device locale
        private fun getDefaultCurrency(): Pair<String, String> {
            return try {
                val locale = Locale.getDefault()
                val currency = Currency.getInstance(locale)
                val code = currency.currencyCode
                val symbol = resolveCurrencySymbol(code, currency.getSymbol(Locale.US))
                Pair(code, symbol)
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
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
        when (preferences[THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            "amoled" -> ThemeMode.AMOLED
            else -> ThemeMode.SYSTEM
        }
    }
    
    val currencyCode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
        preferences[CURRENCY_CODE] ?: defaultCurrency.first
    }
    
    val currencySymbol: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
        val code = preferences[CURRENCY_CODE] ?: defaultCurrency.first
        val rawSymbol = preferences[CURRENCY_SYMBOL] ?: defaultCurrency.second
        resolveCurrencySymbol(code, rawSymbol)
    }
    
    val timezone: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
        preferences[TIMEZONE] ?: defaultTimezone
    }
    
    val totalBudget: Flow<Double> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
        preferences[TOTAL_BUDGET] ?: 0.0
    }
    
    val use24HourFormat: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
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
