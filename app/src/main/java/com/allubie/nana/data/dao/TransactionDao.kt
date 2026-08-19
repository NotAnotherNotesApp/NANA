package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.Transaction
import com.allubie.nana.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val category: String,
    val total: Double
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date >= :startDate AND date < :endDate")
    suspend fun getTotalByTypeInRange(type: TransactionType, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND category = :category AND date >= :startDate AND date < :endDate")
    suspend fun getTotalByCategoryInRange(type: TransactionType, category: String, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND date >= :startDate AND date < :endDate GROUP BY category")
    fun getCategoryTotalsInRange(type: TransactionType, startDate: Long, endDate: Long): Flow<List<CategoryTotal>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
    
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<Transaction>
    
    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory AND type = :type")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String, type: TransactionType)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE budgetMonth = :month AND budgetYear = :year ORDER BY createdAt DESC")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE category = '' LIMIT 1")
    suspend fun getOverallBudget(): Budget?
    
    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetForCategory(category: String): Budget?
    
    @Query("SELECT * FROM budgets WHERE category = :category AND budgetMonth = :month AND budgetYear = :year LIMIT 1")
    suspend fun getBudgetForCategoryInMonth(category: String, month: Int, year: Int): Budget?
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long
    
    @Update
    suspend fun updateBudget(budget: Budget)
    
    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    @Query("UPDATE budgets SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String)

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSync(): List<Budget>
    
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
