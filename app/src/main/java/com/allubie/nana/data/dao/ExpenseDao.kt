package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.entity.ExpenseEntity
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesInRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategoryFlow(category: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpensesInRange(startDate: LocalDate, endDate: LocalDate): Double?
    
    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpensesForCategoryInRange(category: String, startDate: LocalDate, endDate: LocalDate): Double?
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    // Category methods
    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<ExpenseCategoryEntity>>
    
    @Query("SELECT * FROM expense_categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): ExpenseCategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ExpenseCategoryEntity)
    
    @Update
    suspend fun updateCategory(category: ExpenseCategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: ExpenseCategoryEntity)

    @Query("DELETE FROM expense_categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    @Query("UPDATE expenses SET category = :newName WHERE category = :oldName")
    suspend fun renameCategoryInExpenses(oldName: String, newName: String)

    @Transaction
    suspend fun renameAndUpdateCategory(
        oldName: String,
        newName: String,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double
    ) {
        // Update dependent expenses first to maintain referential consistency
        if (oldName != newName) {
            renameCategoryInExpenses(oldName, newName)
            deleteCategoryByName(oldName)
        }
        insertCategory(ExpenseCategoryEntity(newName, iconName, colorHex, monthlyBudget))
    }
}
