package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.Transaction
import com.allubie.nana.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

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
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE category = '' LIMIT 1")
    suspend fun getOverallBudget(): Budget?
    
    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetForCategory(category: String): Budget?
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long
    
    @Update
    suspend fun updateBudget(budget: Budget)
    
    @Delete
    suspend fun deleteBudget(budget: Budget)
    
    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSync(): List<Budget>
    
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
