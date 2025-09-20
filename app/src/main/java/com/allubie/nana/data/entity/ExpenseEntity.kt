package com.allubie.nana.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: LocalDate,
    val description: String? = null,
    val createdAt: Instant = Clock.System.now()
)

object ExpenseFactory {
    fun create(
        id: String,
        title: String,
        amount: Double,
        category: String,
        date: LocalDate,
        description: String? = null
    ) = ExpenseEntity(id, title, amount, category, date, description)
}
