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

    // Standard category colors as fallbacks
    private val standardCategoryColors = mapOf(
        "Food" to 0xFFFF7043.toInt(),
        "Transport" to 0xFF42A5F5.toInt(),
        "Entertainment" to 0xFFAB47BC.toInt(),
        "Shopping" to 0xFFEC407A.toInt(),
        "Education" to 0xFF26A69A.toInt(),
        "Health" to 0xFFEF5350.toInt(),
        "Bills" to 0xFF78909C.toInt(),
        "Other" to 0xFF8D6E63.toInt()
    )

    private val fallbackColors = listOf(
        0xFF6750A4.toInt(), // Primary
        0xFF03A9F4.toInt(), // Blue
        0xFF8BC34A.toInt(), // Light Green
        0xFFFFC107.toInt(), // Amber
        0xFF9C27B0.toInt(), // Purple
        0xFFE91E63.toInt(), // Pink
        0xFF00BCD4.toInt(), // Cyan
        0xFFFF5722.toInt()  // Deep Orange
    )
    
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
                        .filter { it.type == TransactionType.INCOME && it.date in startOfMonth..endOfMonth }
                        .sumOf { it.amount }

                    val totalExpenses = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.date in startOfMonth..endOfMonth }
                        .sumOf { it.amount }
                    
                    // Build label color map
                    val labelColorMap = labels.associate { it.name.lowercase() to it.color }

                    // Group expenses by category - include custom categories
                    val expenseTransactions = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.date in startOfMonth..endOfMonth }
                    
                    val categoryBreakdown = expenseTransactions
                        .groupBy { it.category }
                        .entries
                        .mapIndexed { index, (category, txs) ->
                            val amount = txs.sumOf { it.amount }
                            val color = labelColorMap[category.lowercase()] 
                                ?: standardCategoryColors[category]
                                ?: fallbackColors[index % fallbackColors.size]
                            
                            CategorySpending(
                                name = category,
                                amount = amount,
                                percentage = if (totalExpenses > 0.0) (amount / totalExpenses).toFloat() else 0f,
                                color = color
                            )
                        }
                        .filter { it.amount > 0.0 }
                        .sortedByDescending { it.amount }

                    val budgetComparisons = budgets.map { budget ->
                        val actual = if (budget.category.isEmpty()) {
                            totalExpenses
                        } else {
                            transactions
                                .filter { it.type == TransactionType.EXPENSE && it.category == budget.category && it.date in startOfMonth..endOfMonth }
                                .sumOf { it.amount }
                        }
                        BudgetComparison(
                            category = budget.category,
                            budgeted = budget.amount,
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
