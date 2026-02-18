package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LabelType {
    NOTE,       // Note tags/labels
    EXPENSE,    // Finance expense categories
    INCOME,     // Finance income categories
    EVENT       // Schedule event categories
}

@Entity(
    tableName = "labels",
    indices = [
        Index(value = ["type"]),
        Index(value = ["name", "type"], unique = true)
    ]
)
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: LabelType,
    val iconName: String? = null,   // Material icon name (null for note labels - color only)
    val color: Int,                  // ARGB color value
    val isPreset: Boolean = false,   // Preset labels cannot be deleted
    val isHidden: Boolean = false,   // Soft-delete for presets
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// Preset colors for labels
object LabelColors {
    val noteColors = listOf(
        0xFF3B82F6.toInt(),  // Blue
        0xFF06B6D4.toInt(),  // Cyan
        0xFF8B5CF6.toInt(),  // Purple
        0xFFEC4899.toInt(),  // Pink
        0xFFF97316.toInt(),  // Orange
        0xFF22C55E.toInt(),  // Green
        0xFFEF4444.toInt(),  // Red
        0xFFF59E0B.toInt(),  // Amber
        0xFF6366F1.toInt(),  // Indigo
        0xFF14B8A6.toInt()   // Teal
    )
    
    val categoryColors = listOf(
        0xFFF97316.toInt(),  // Orange - Food
        0xFF3B82F6.toInt(),  // Blue - Transport
        0xFFEC4899.toInt(),  // Pink - Shopping
        0xFF8B5CF6.toInt(),  // Purple - Entertainment
        0xFFF59E0B.toInt(),  // Amber - Bills
        0xFF22C55E.toInt(),  // Green - Health
        0xFF06B6D4.toInt(),  // Cyan - Education
        0xFF6B7280.toInt()   // Gray - Other
    )
}

// Preset labels to be seeded on first launch
object PresetLabels {
    
    val noteLabels = listOf(
        Label(name = "Personal", type = LabelType.NOTE, color = 0xFF3B82F6.toInt(), isPreset = true, sortOrder = 0),
        Label(name = "Work", type = LabelType.NOTE, color = 0xFF06B6D4.toInt(), isPreset = true, sortOrder = 1),
        Label(name = "Ideas", type = LabelType.NOTE, color = 0xFF8B5CF6.toInt(), isPreset = true, sortOrder = 2),
        Label(name = "Important", type = LabelType.NOTE, color = 0xFFEF4444.toInt(), isPreset = true, sortOrder = 3)
    )
    
    val expenseCategories = listOf(
        Label(name = "Food", type = LabelType.EXPENSE, iconName = "restaurant", color = 0xFFF97316.toInt(), isPreset = true, sortOrder = 0),
        Label(name = "Transport", type = LabelType.EXPENSE, iconName = "directions_bus", color = 0xFF3B82F6.toInt(), isPreset = true, sortOrder = 1),
        Label(name = "Shopping", type = LabelType.EXPENSE, iconName = "shopping_bag", color = 0xFFEC4899.toInt(), isPreset = true, sortOrder = 2),
        Label(name = "Entertainment", type = LabelType.EXPENSE, iconName = "movie", color = 0xFF8B5CF6.toInt(), isPreset = true, sortOrder = 3),
        Label(name = "Bills", type = LabelType.EXPENSE, iconName = "receipt_long", color = 0xFFF59E0B.toInt(), isPreset = true, sortOrder = 4),
        Label(name = "Health", type = LabelType.EXPENSE, iconName = "favorite", color = 0xFF22C55E.toInt(), isPreset = true, sortOrder = 5),
        Label(name = "Education", type = LabelType.EXPENSE, iconName = "school", color = 0xFF06B6D4.toInt(), isPreset = true, sortOrder = 6),
        Label(name = "Other", type = LabelType.EXPENSE, iconName = "more_horiz", color = 0xFF6B7280.toInt(), isPreset = true, sortOrder = 7)
    )
    
    val incomeCategories = listOf(
        Label(name = "Salary", type = LabelType.INCOME, iconName = "payments", color = 0xFF22C55E.toInt(), isPreset = true, sortOrder = 0),
        Label(name = "Allowance", type = LabelType.INCOME, iconName = "wallet", color = 0xFF3B82F6.toInt(), isPreset = true, sortOrder = 1),
        Label(name = "Gift", type = LabelType.INCOME, iconName = "card_giftcard", color = 0xFFF59E0B.toInt(), isPreset = true, sortOrder = 2),
        Label(name = "Scholarship", type = LabelType.INCOME, iconName = "school", color = 0xFF8B5CF6.toInt(), isPreset = true, sortOrder = 3),
        Label(name = "Part-time Job", type = LabelType.INCOME, iconName = "work", color = 0xFF06B6D4.toInt(), isPreset = true, sortOrder = 4),
        Label(name = "Other", type = LabelType.INCOME, iconName = "more_horiz", color = 0xFF6B7280.toInt(), isPreset = true, sortOrder = 5)
    )
    
    val eventCategories = listOf(
        Label(name = "Meeting", type = LabelType.EVENT, iconName = "event", color = 0xFF6366F1.toInt(), isPreset = true, sortOrder = 0),
        Label(name = "Class", type = LabelType.EVENT, iconName = "school", color = 0xFF14B8A6.toInt(), isPreset = true, sortOrder = 1),
        Label(name = "Personal", type = LabelType.EVENT, iconName = "person", color = 0xFF3B82F6.toInt(), isPreset = true, sortOrder = 2),
        Label(name = "Work", type = LabelType.EVENT, iconName = "work", color = 0xFF06B6D4.toInt(), isPreset = true, sortOrder = 3),
        Label(name = "Health", type = LabelType.EVENT, iconName = "favorite", color = 0xFF22C55E.toInt(), isPreset = true, sortOrder = 4),
        Label(name = "Social", type = LabelType.EVENT, iconName = "groups", color = 0xFFEC4899.toInt(), isPreset = true, sortOrder = 5),
        Label(name = "Other", type = LabelType.EVENT, iconName = "more_horiz", color = 0xFF6B7280.toInt(), isPreset = true, sortOrder = 6)
    )
    
    val all: List<Label>
        get() = noteLabels + expenseCategories + incomeCategories + eventCategories
}
