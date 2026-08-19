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
            "HKD" to "HK$", "NZD" to "NZ$", "BDT" to "৳", "MMK" to "K",
            "KRW" to "₩", "UAH" to "₴", "ILS" to "₪", "KZT" to "₸", "GEL" to "₾",
            "NPR" to "₨", "AFN" to "؋", "IRR" to "﷼", "MNT" to "₮", "LAK" to "₭",
            "CRC" to "₡", "PEN" to "S/."
        )

        private fun resolveCurrencySymbol(code: String, rawSymbol: String?): String {
            val cleanCode = code.trim().uppercase()
            // If we have a valid raw symbol that isn't just the code repeated, use it
            if (!rawSymbol.isNullOrBlank() && rawSymbol != cleanCode) {
                return rawSymbol
            }
            // Check our known symbols map
            val mappedSymbol = knownCurrencySymbols[cleanCode]
            if (mappedSymbol != null) {
                return mappedSymbol
            }
            // Try Java's Currency API with the device's default locale
            return try {
                val currency = Currency.getInstance(cleanCode)
                // Try device locale first for better symbol resolution
                val localSymbol = currency.getSymbol(Locale.getDefault())
                if (localSymbol.isNotBlank() && localSymbol != cleanCode) {
                    return localSymbol
                }
                // Fallback to US locale
                val usSymbol = currency.getSymbol(Locale.US)
                if (usSymbol.isNotBlank() && usSymbol != cleanCode) usSymbol else cleanCode
            } catch (e: Exception) {
                cleanCode.ifEmpty { "$" }
            }
        }
        
        // Get default currency from device locale
        private fun getDefaultCurrency(): Pair<String, String> {
            return try {
                val locale = Locale.getDefault()
                val currency = Currency.getInstance(locale)
                val code = currency.currencyCode
                // Try device locale symbol first, then resolve
                val rawSymbol = currency.getSymbol(locale)
                val symbol = resolveCurrencySymbol(code, rawSymbol)
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
