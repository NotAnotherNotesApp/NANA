package com.allubie.nana.ui.screens.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.dao.BudgetDao
import com.allubie.nana.data.dao.LabelDao
import com.allubie.nana.data.dao.TransactionDao
import com.allubie.nana.data.model.BudgetPeriod
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class FinancesOverviewData(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSavings: Double = 0.0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val budgetComparisons: List<BudgetComparison> = emptyList()
)

data class CategorySpending(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Int = 0
)

data class BudgetComparison(
    val category: String,
    val budgeted: Double,
    val actual: Double
)

class FinancesOverviewViewModel(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val labelDao: LabelDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _overview = MutableStateFlow(FinancesOverviewData())
    val overview: StateFlow<FinancesOverviewData> = _overview.asStateFlow()
    
    val currencySymbol: StateFlow<String> = preferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")
    
    // Labels for overview
    val expenseLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        observeOverview()
    }
    
    private fun observeOverview() {
        viewModelScope.launch {
            combine(
                transactionDao.getAllTransactions(), 
                budgetDao.getAllBudgets(),
                labelDao.getLabelsByType(LabelType.EXPENSE)
            ) { transactions, budgets, labels ->
                Triple(transactions, budgets, labels)
            }.collect { (transactions, budgets, labels) ->
                try {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfMonth = calendar.timeInMillis

                    calendar.add(Calendar.MONTH, 1)
                    val endOfMonth = calendar.timeInMillis

                    val totalIncome = transactions
                        .filter { it.type == TransactionType.INCOME && it.date >= startOfMonth && it.date < endOfMonth }
                        .sumOf { it.amount }

                    val totalExpenses = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.date >= startOfMonth && it.date < endOfMonth }
                        .sumOf { it.amount }
                    
                    // Build label color map
                    val labelColorMap = labels.associate { it.name.lowercase() to it.color }

                    // Group expenses by category - include custom categories
                    val expenseTransactions = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.date >= startOfMonth && it.date < endOfMonth }
                    
                    val categoryBreakdown = expenseTransactions
                        .groupBy { it.category }
                        .map { (category, txs) ->
                            val amount = txs.sumOf { it.amount }
                            CategorySpending(
                                name = category,
                                amount = amount,
                                percentage = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f,
                                color = labelColorMap[category.lowercase()] ?: 0xFF78909C.toInt()
                            )
                        }
                        .filter { it.amount > 0 }
                        .sortedByDescending { it.amount }

                    val budgetComparisons = budgets.map { budget ->
                        // Normalize budget to monthly amount based on period
                        val monthlyBudget = when (budget.period) {
                            BudgetPeriod.WEEKLY -> budget.amount * 4.33 // avg weeks per month
                            BudgetPeriod.MONTHLY -> budget.amount
                            BudgetPeriod.YEARLY -> budget.amount / 12.0
                        }
                        val actual = if (budget.category.isEmpty()) {
                            totalExpenses
                        } else {
                            transactions
                                .filter { it.type == TransactionType.EXPENSE && it.category == budget.category && it.date >= startOfMonth && it.date < endOfMonth }
                                .sumOf { it.amount }
                        }
                        BudgetComparison(
                            category = budget.category,
                            budgeted = monthlyBudget,
                            actual = actual
                        )
                    }

                    _overview.value = FinancesOverviewData(
                        totalIncome = totalIncome,
                        totalExpenses = totalExpenses,
                        netSavings = totalIncome - totalExpenses,
                        categoryBreakdown = categoryBreakdown,
                        budgetComparisons = budgetComparisons
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                FinancesOverviewViewModel(
                    application.database.transactionDao(),
                    application.database.budgetDao(),
                    application.database.labelDao(),
                    application.preferencesManager
                )
            }
        }
    }
}
