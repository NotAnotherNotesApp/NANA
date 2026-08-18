package com.allubie.nana.ui.screens.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.BudgetPeriod
import com.allubie.nana.data.repository.TransactionRepository
import com.allubie.nana.data.model.TransactionType
import com.allubie.nana.widget.requestBudgetWidgetRefresh
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class BudgetManagerViewModel(
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: PreferencesManager,
    private val application: NanaApplication
) : ViewModel() {
    
    data class BudgetManagerUiState(
        val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
        val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        val currencySymbol: String = "",
        val totalBudgetLimit: Double = 0.0,
        val budgets: List<Budget> = emptyList(),
        val totalAllocated: Double = 0.0,
        val totalBudget: Double = 0.0,
        val categorySpending: Map<String, Double> = emptyMap(),
        val totalSpent: Double = 0.0
    )
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    
    private val _currencySymbol = preferencesManager.currencySymbol
    
    // All budgets (overall + category) scoped to selected month/year
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _allBudgetsForMonth = combine(_selectedMonth, _selectedYear) { month, year ->
        Pair(month, year)
    }.flatMapLatest { (month, year) ->
        transactionRepository.getBudgetsForMonth(month, year)
    }
    
    // Overall budget limit for this specific month (category == "")
    private val _totalBudgetLimit = _allBudgetsForMonth.map { list ->
        list.find { it.category.isEmpty() }?.amount ?: 0.0
    }
    
    // Category-specific budgets for this month (category != "")
    private val _categoryBudgets = _allBudgetsForMonth.map { list ->
        list.filter { it.category.isNotEmpty() }
    }
    
    private val _totalAllocated = _categoryBudgets.map { budgetList ->
        budgetList.sumOf { it.amount }
    }
    
    private val _totalBudget = combine(_totalBudgetLimit, _totalAllocated) { limit, allocated ->
        if (limit > 0) limit else allocated
    }
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _categorySpending = combine(_selectedMonth, _selectedYear) { month, year ->
        Pair(month, year)
    }.flatMapLatest { (month, year) ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis
        
        transactionRepository.getCategorySpending(startOfMonth, endOfMonth)
    }
    
    private val _totalSpent = _categorySpending.map { spending ->
        spending.values.sum()
    }
    
    val uiState: StateFlow<BudgetManagerUiState> = combine(
        _selectedMonth, _selectedYear, _currencySymbol, _totalBudgetLimit, _categoryBudgets,
        _totalAllocated, _totalBudget, _categorySpending, _totalSpent
    ) { args: Array<Any> ->
        BudgetManagerUiState(
            selectedMonth = args[0] as Int,
            selectedYear = args[1] as Int,
            currencySymbol = args[2] as String,
            totalBudgetLimit = args[3] as Double,
            budgets = (args[4] as List<*>).filterIsInstance<Budget>(),
            totalAllocated = args[5] as Double,
            totalBudget = args[6] as Double,
            categorySpending = args[7] as Map<String, Double>,
            totalSpent = args[8] as Double
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetManagerUiState()
    )
    
    fun selectMonth(month: Int) {
        _selectedMonth.value = month
    }
    
    fun selectYear(year: Int) {
        _selectedYear.value = year
    }
    
    fun addBudget(category: String, amount: Double, iconName: String = "") {
        viewModelScope.launch {
            val budget = Budget(
                category = category,
                amount = amount,
                period = BudgetPeriod.MONTHLY,
                startDate = System.currentTimeMillis(),
                iconName = iconName,
                budgetMonth = _selectedMonth.value,
                budgetYear = _selectedYear.value
            )
            transactionRepository.insertBudget(budget)
            requestBudgetWidgetRefresh(application)
        }
    }
    
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            transactionRepository.updateBudget(budget)
            requestBudgetWidgetRefresh(application)
        }
    }
    
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            transactionRepository.deleteBudget(budget)
            requestBudgetWidgetRefresh(application)
        }
    }
    
    fun setTotalBudgetLimit(amount: Double) {
        viewModelScope.launch {
            val month = _selectedMonth.value
            val year = _selectedYear.value
            val existingOverallBudget = transactionRepository.getBudgetForCategoryInMonth("", month, year)
            if (amount > 0) {
                val budget = existingOverallBudget?.copy(amount = amount) ?: Budget(
                    category = "",
                    amount = amount,
                    period = BudgetPeriod.MONTHLY,
                    startDate = System.currentTimeMillis(),
                    budgetMonth = month,
                    budgetYear = year
                )
                transactionRepository.insertBudget(budget)
            } else if (existingOverallBudget != null) {
                transactionRepository.deleteBudget(existingOverallBudget)
            }
            requestBudgetWidgetRefresh(application)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NanaApplication)
                val database = application.database
                val transactionRepository = TransactionRepository(
                    database.transactionDao(),
                    database.budgetDao()
                )
                BudgetManagerViewModel(
                    transactionRepository = transactionRepository,
                    preferencesManager = application.preferencesManager,
                    application = application
                )
            }
        }
    }
}
