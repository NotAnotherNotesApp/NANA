package com.allubie.nana.ui.screens.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.dao.BudgetDao
import com.allubie.nana.data.dao.TransactionDao
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.BudgetPeriod
import com.allubie.nana.data.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class BudgetManagerViewModel(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    
    val currencySymbol: StateFlow<String> = preferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")
    
    // Total budget from preferences (user-set overall limit)
    val totalBudgetLimit: StateFlow<Double> = preferencesManager.totalBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val budgets: StateFlow<List<Budget>> = budgetDao.getAllBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Sum of all category allocations
    val totalAllocated: StateFlow<Double> = budgets.map { budgetList ->
        budgetList.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    // Effective total budget: use user-set limit if > 0, otherwise sum of allocations
    val totalBudget: StateFlow<Double> = combine(totalBudgetLimit, totalAllocated) { limit, allocated ->
        if (limit > 0) limit else allocated
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    // Calculate spending per category for the selected month
    val categorySpending: StateFlow<Map<String, Double>> = combine(
        transactionDao.getAllTransactions(),
        _selectedMonth
    ) { transactions, month ->
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        
        // Filter transactions for selected month and calculate per category
        transactions
            .filter { transaction ->
                transaction.type == TransactionType.EXPENSE &&
                Calendar.getInstance().apply { timeInMillis = transaction.date }.let {
                    it.get(Calendar.MONTH) == month && it.get(Calendar.YEAR) == year
                }
            }
            .groupBy { it.category }
            .mapValues { (_, categoryTransactions) ->
                categoryTransactions.sumOf { it.amount }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    val totalSpent: StateFlow<Double> = categorySpending.map { spending ->
        spending.values.sum()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    fun selectMonth(month: Int) {
        _selectedMonth.value = month
    }
    
    fun addBudget(category: String, amount: Double, iconName: String = "") {
        viewModelScope.launch {
            val budget = Budget(
                category = category,
                amount = amount,
                period = BudgetPeriod.MONTHLY,
                startDate = System.currentTimeMillis(),
                iconName = iconName
            )
            budgetDao.insertBudget(budget)
        }
    }
    
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.updateBudget(budget)
        }
    }
    
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }
    
    fun setTotalBudgetLimit(amount: Double) {
        viewModelScope.launch {
            preferencesManager.setTotalBudget(amount)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NanaApplication)
                val database = application.database
                BudgetManagerViewModel(
                    budgetDao = database.budgetDao(),
                    transactionDao = database.transactionDao(),
                    preferencesManager = application.preferencesManager
                )
            }
        }
    }
}
