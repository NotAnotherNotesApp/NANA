package com.allubie.nana.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"]),
        Index(value = ["category"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["category"], unique = true)
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // Empty string for overall budget
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val iconName: String = "", // Icon name for custom categories
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    WEEKLY, MONTHLY, YEARLY
}

// Predefined expense categories
object ExpenseCategories {
    val list = listOf(
        "Food",
        "Transport",
        "Entertainment",
        "Shopping",
        "Education",
        "Health",
        "Bills",
        "Other"
    )
}

// Predefined income categories
object IncomeCategories {
    val list = listOf(
        "Allowance",
        "Part-time Job",
        "Scholarship",
        "Gift",
        "Other"
    )
}
