package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.*
import com.allubie.nana.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>> = transactionDao.getTransactionsInRange(startDate, endDate)
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = transactionDao.getTransactionsByType(type)
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> = transactionDao.getTransactionsByCategory(category)
    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> = transactionDao.getRecentTransactions(limit)
    suspend fun getTotalByTypeInRange(type: TransactionType, startDate: Long, endDate: Long): Double? = transactionDao.getTotalByTypeInRange(type, startDate, endDate)
    suspend fun getTotalByCategoryInRange(type: TransactionType, category: String, startDate: Long, endDate: Long): Double? = transactionDao.getTotalByCategoryInRange(type, category, startDate, endDate)
    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)

    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> = budgetDao.getBudgetsForMonth(month, year)
    suspend fun getOverallBudget(): Budget? = budgetDao.getOverallBudget()
    suspend fun getBudgetForCategory(category: String): Budget? = budgetDao.getBudgetForCategory(category)
    suspend fun getBudgetForCategoryInMonth(category: String, month: Int, year: Int): Budget? = budgetDao.getBudgetForCategoryInMonth(category, month, year)
    suspend fun getBudgetById(id: Long): Budget? = budgetDao.getBudgetById(id)
    suspend fun insertBudget(budget: Budget): Long = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    fun getCategorySpending(startDate: Long, endDate: Long): Flow<Map<String, Double>> {
        return transactionDao.getCategoryTotalsInRange(TransactionType.EXPENSE, startDate, endDate)
            .map { totals -> totals.associate { it.category to it.total } }
    }
}
