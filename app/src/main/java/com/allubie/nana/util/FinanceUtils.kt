package com.allubie.nana.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Centralized utility for currency formatting across the app.
 */
object CurrencyFormatter {
    
    /**
     * Format amount with currency symbol.
     * @param amount The amount to format
     * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR", "BDT")
     * @param showSymbol Whether to show currency symbol or code
     * @return Formatted currency string
     */
    fun format(amount: Double, currencyCode: String, showSymbol: Boolean = true): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val format = NumberFormat.getCurrencyInstance().apply {
                this.currency = currency
                maximumFractionDigits = if (amount == amount.toLong().toDouble()) 0 else 2
            }
            if (showSymbol) {
                format.format(amount)
            } else {
                // Format without symbol, just number with proper grouping
                val numberFormat = NumberFormat.getNumberInstance().apply {
                    maximumFractionDigits = if (amount == amount.toLong().toDouble()) 0 else 2
                }
                numberFormat.format(amount)
            }
        } catch (e: Exception) {
            // Fallback formatting
            val formatted = if (amount == amount.toLong().toDouble()) {
                amount.toLong().toString()
            } else {
                String.format("%.2f", amount)
            }
            if (showSymbol) "$currencyCode $formatted" else formatted
        }
    }
    
    /**
     * Get currency symbol from currency code.
     */
    fun getSymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            currencyCode
        }
    }
    
    /**
     * Format with explicit symbol prefix.
     */
    fun formatWithSymbol(amount: Double, symbol: String): String {
        val formatted = if (amount == amount.toLong().toDouble()) {
            NumberFormat.getNumberInstance().format(amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
        return "$symbol$formatted"
    }
}

/**
 * Centralized icon mapping for categories.
 */
object CategoryIcons {
    
    private val iconMap: Map<String, ImageVector> = mapOf(
        // Food & Dining
        "restaurant" to Icons.Outlined.Restaurant,
        "lunch_dining" to Icons.Outlined.LunchDining,
        "local_cafe" to Icons.Outlined.LocalCafe,
        "fastfood" to Icons.Outlined.Fastfood,
        "local_bar" to Icons.Outlined.LocalBar,
        "local_pizza" to Icons.Outlined.LocalPizza,
        "icecream" to Icons.Outlined.Icecream,
        "bakery_dining" to Icons.Outlined.BakeryDining,
        
        // Transport
        "directions_bus" to Icons.Outlined.DirectionsBus,
        "directions_car" to Icons.Outlined.DirectionsCar,
        "flight" to Icons.Outlined.Flight,
        "train" to Icons.Outlined.Train,
        "local_taxi" to Icons.Outlined.LocalTaxi,
        "two_wheeler" to Icons.Outlined.TwoWheeler,
        "directions_bike" to Icons.Outlined.DirectionsBike,
        
        // Shopping
        "shopping_bag" to Icons.Outlined.ShoppingBag,
        "shopping_cart" to Icons.Outlined.ShoppingCart,
        "store" to Icons.Outlined.Store,
        "local_mall" to Icons.Outlined.LocalMall,
        
        // Entertainment
        "movie" to Icons.Outlined.Movie,
        "sports_esports" to Icons.Outlined.SportsEsports,
        "music_note" to Icons.Outlined.MusicNote,
        "theater_comedy" to Icons.Outlined.TheaterComedy,
        "sports_soccer" to Icons.Outlined.SportsSoccer,
        "sports_basketball" to Icons.Outlined.SportsBasketball,
        "casino" to Icons.Outlined.Casino,
        "nightlife" to Icons.Outlined.Nightlife,
        
        // Bills & Finance
        "receipt_long" to Icons.Outlined.ReceiptLong,
        "receipt" to Icons.Outlined.Receipt,
        "payments" to Icons.Outlined.Payments,
        "account_balance" to Icons.Outlined.AccountBalance,
        "credit_card" to Icons.Outlined.CreditCard,
        "savings" to Icons.Outlined.Savings,
        "wallet" to Icons.Outlined.Wallet,
        "attach_money" to Icons.Outlined.AttachMoney,
        
        // Health
        "favorite" to Icons.Outlined.Favorite,
        "local_hospital" to Icons.Outlined.LocalHospital,
        "medication" to Icons.Outlined.Medication,
        "fitness_center" to Icons.Outlined.FitnessCenter,
        "spa" to Icons.Outlined.Spa,
        "self_improvement" to Icons.Outlined.SelfImprovement,
        
        // Education
        "school" to Icons.Outlined.School,
        "menu_book" to Icons.Outlined.MenuBook,
        "auto_stories" to Icons.Outlined.AutoStories,
        "science" to Icons.Outlined.Science,
        
        // Work & Business
        "work" to Icons.Outlined.Work,
        "business_center" to Icons.Outlined.BusinessCenter,
        "laptop" to Icons.Outlined.Laptop,
        "phone_android" to Icons.Outlined.PhoneAndroid,
        
        // Home & Living
        "home" to Icons.Outlined.Home,
        "apartment" to Icons.Outlined.Apartment,
        "chair" to Icons.Outlined.Chair,
        "lightbulb" to Icons.Outlined.Lightbulb,
        "local_laundry_service" to Icons.Outlined.LocalLaundryService,
        "cleaning_services" to Icons.Outlined.CleaningServices,
        
        // People & Social
        "person" to Icons.Outlined.Person,
        "people" to Icons.Outlined.People,
        "groups" to Icons.Outlined.Groups,
        "family_restroom" to Icons.Outlined.FamilyRestroom,
        "child_care" to Icons.Outlined.ChildCare,
        "pets" to Icons.Outlined.Pets,
        
        // Travel
        "beach_access" to Icons.Outlined.BeachAccess,
        "hotel" to Icons.Outlined.Hotel,
        "luggage" to Icons.Outlined.Luggage,
        "map" to Icons.Outlined.Map,
        "explore" to Icons.Outlined.Explore,
        
        // Events & Calendar
        "event" to Icons.Outlined.Event,
        "calendar_today" to Icons.Outlined.CalendarToday,
        "schedule" to Icons.Outlined.Schedule,
        "celebration" to Icons.Outlined.Celebration,
        "cake" to Icons.Outlined.Cake,
        
        // Communication
        "mail" to Icons.Outlined.Mail,
        "chat" to Icons.Outlined.Chat,
        "call" to Icons.Outlined.Call,
        "notifications" to Icons.Outlined.Notifications,
        
        // Misc
        "card_giftcard" to Icons.Outlined.CardGiftcard,
        "volunteer_activism" to Icons.Outlined.VolunteerActivism,
        "handshake" to Icons.Outlined.Handshake,
        "redeem" to Icons.Outlined.Redeem,
        "more_horiz" to Icons.Outlined.MoreHoriz,
        "category" to Icons.Outlined.Category,
        "label" to Icons.Outlined.Label,
        "bookmark" to Icons.Outlined.Bookmark,
        "star" to Icons.Outlined.Star,
        "check_circle" to Icons.Outlined.CheckCircle
    )
    
    /**
     * Get icon by name. Falls back to MoreHoriz if not found.
     */
    fun getIcon(iconName: String?): ImageVector {
        return iconMap[iconName] ?: Icons.Outlined.MoreHoriz
    }
    
    /**
     * Get all available icons for category picker.
     */
    fun getAllIcons(): List<Pair<String, ImageVector>> {
        return iconMap.toList()
    }
    
    /**
     * Get icons grouped by category for better UX.
     */
    fun getIconsByGroup(): Map<String, List<Pair<String, ImageVector>>> {
        return mapOf(
            "Food" to listOf(
                "restaurant", "lunch_dining", "local_cafe", "fastfood", 
                "local_bar", "local_pizza", "icecream", "bakery_dining"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Transport" to listOf(
                "directions_bus", "directions_car", "flight", "train",
                "local_taxi", "two_wheeler", "directions_bike"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Shopping" to listOf(
                "shopping_bag", "shopping_cart", "store", "local_mall"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Entertainment" to listOf(
                "movie", "sports_esports", "music_note", "theater_comedy",
                "sports_soccer", "sports_basketball", "casino", "nightlife"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Finance" to listOf(
                "receipt_long", "receipt", "payments", "account_balance",
                "credit_card", "savings", "wallet", "attach_money"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Health" to listOf(
                "favorite", "local_hospital", "medication", "fitness_center",
                "spa", "self_improvement"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Education" to listOf(
                "school", "menu_book", "auto_stories", "science"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Work" to listOf(
                "work", "business_center", "laptop", "phone_android"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Home" to listOf(
                "home", "apartment", "chair", "lightbulb",
                "local_laundry_service", "cleaning_services"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "People" to listOf(
                "person", "people", "groups", "family_restroom",
                "child_care", "pets"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Travel" to listOf(
                "beach_access", "hotel", "luggage", "map", "explore"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Events" to listOf(
                "event", "calendar_today", "schedule", "celebration", "cake"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } },
            
            "Other" to listOf(
                "card_giftcard", "volunteer_activism", "handshake", "redeem",
                "more_horiz", "category", "label", "bookmark", "star", "check_circle"
            ).mapNotNull { name -> iconMap[name]?.let { name to it } }
        )
    }
}

/**
 * Utility for color operations.
 */
object ColorUtils {
    
    /**
     * Convert Int color to Compose Color.
     */
    fun intToColor(colorInt: Int): Color = Color(colorInt)
    
    /**
     * Available colors for label/category picker.
     */
    val availableColors = listOf(
        0xFF3B82F6.toInt(),  // Blue
        0xFF06B6D4.toInt(),  // Cyan
        0xFF14B8A6.toInt(),  // Teal
        0xFF22C55E.toInt(),  // Green
        0xFF84CC16.toInt(),  // Lime
        0xFFF59E0B.toInt(),  // Amber
        0xFFF97316.toInt(),  // Orange
        0xFFEF4444.toInt(),  // Red
        0xFFEC4899.toInt(),  // Pink
        0xFF8B5CF6.toInt(),  // Purple
        0xFF6366F1.toInt(),  // Indigo
        0xFF6B7280.toInt()   // Gray
    )
}
