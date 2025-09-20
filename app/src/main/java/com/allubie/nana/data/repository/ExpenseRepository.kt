package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.ExpenseDao
import com.allubie.nana.data.entity.ExpenseEntity
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.data.entity.ExpenseFactory
import com.allubie.nana.core.BaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.UUID

class ExpenseRepository(private val expenseDao: ExpenseDao) : BaseRepository() {
    
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()
    
    fun getExpensesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<ExpenseEntity>> = 
        expenseDao.getExpensesInRangeFlow(startDate, endDate)
    
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> = 
        expenseDao.getExpensesByCategoryFlow(category)
    
    fun getAllCategories(): Flow<List<ExpenseCategoryEntity>> = expenseDao.getAllCategoriesFlow()
    
    suspend fun getExpenseById(id: String): ExpenseEntity? = expenseDao.getExpenseById(id)
    
    suspend fun insertExpense(expense: ExpenseEntity) = expenseDao.insertExpense(expense)
    
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
    
    suspend fun getTotalExpensesInRange(startDate: LocalDate, endDate: LocalDate): Double {
        return expenseDao.getTotalExpensesInRange(startDate, endDate) ?: 0.0
    }
    
    suspend fun getTotalExpensesForCategoryInRange(
        category: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Double {
        return expenseDao.getTotalExpensesForCategoryInRange(category, startDate, endDate) ?: 0.0
    }
    
    suspend fun getMonthlyTotal(): Double {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(today.year, today.month, 1)
        return getTotalExpensesInRange(startOfMonth, today)
    }
    
    suspend fun getMonthlyTotalForCategory(category: String): Double {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfMonth = LocalDate(today.year, today.month, 1)
        return getTotalExpensesForCategoryInRange(category, startOfMonth, today)
    }
    
    // Category methods
    suspend fun getCategoryByName(name: String): ExpenseCategoryEntity? = 
        expenseDao.getCategoryByName(name)
    
    suspend fun insertCategory(category: ExpenseCategoryEntity) = expenseDao.insertCategory(category)
    
    suspend fun updateCategory(category: ExpenseCategoryEntity) = expenseDao.updateCategory(category)
    
    suspend fun deleteCategory(category: ExpenseCategoryEntity) = expenseDao.deleteCategory(category)

    suspend fun renameCategory(
        oldName: String,
        newName: String,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double
    ) {
        expenseDao.renameAndUpdateCategory(oldName, newName, iconName, colorHex, monthlyBudget)
    }
    
    suspend fun createExpense(
        title: String,
        amount: Double,
        category: String,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        description: String? = null
    ): ExpenseEntity {
        val expense = ExpenseFactory.create(
            id = idProvider.newId(),
            title = title,
            amount = amount,
            category = category,
            date = date,
            description = description
        )
        insertExpense(expense)
        return expense
    }
    
    suspend fun createCategory(
        name: String,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double = 0.0
    ): ExpenseCategoryEntity {
        val category = ExpenseCategoryEntity(name, iconName, colorHex, monthlyBudget)
        insertCategory(category)
        return category
    }
}
