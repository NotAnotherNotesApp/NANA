package com.allubie.nana.ui.screens.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.model.Transaction
import com.allubie.nana.data.repository.LabelRepository
import com.allubie.nana.data.repository.TransactionRepository
import com.allubie.nana.data.model.TransactionType
import com.allubie.nana.widget.requestBudgetWidgetRefresh
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class FinancesViewModel(
    private val transactionRepository: TransactionRepository,
    private val labelRepository: LabelRepository,
    private val preferencesManager: PreferencesManager,
    private val application: NanaApplication
) : ViewModel() {
    
    data class FinancesUiState(
        val selectedMonth: Calendar = Calendar.getInstance(),
        val isLoading: Boolean = true,
        val totalBudget: Double = 0.0,
        val hasBudget: Boolean = false,
        val currencySymbol: String = "",
        val expenseLabels: List<Label> = emptyList(),
        val incomeLabels: List<Label> = emptyList(),
        val filteredTransactions: List<Transaction> = emptyList(),
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0
    )
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    private val _isLoading = MutableStateFlow(true)
    
    private val _monthBudgets = _selectedMonth.flatMapLatest { calendar ->
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        transactionRepository.getBudgetsForMonth(month, year)
    }
    
    private val _totalBudget = _monthBudgets.map { budgets ->
        val overallLimit = budgets.find { it.category.isEmpty() }?.amount ?: 0.0
        val categoryAllocated = budgets.filter { it.category.isNotEmpty() }.sumOf { it.amount }
        if (overallLimit > 0.0) overallLimit else categoryAllocated
    }
    
    private val _hasBudget = _totalBudget.map { it > 0 }
    
    private val _currencySymbol = preferencesManager.currencySymbol
    
    private val _expenseLabels = labelRepository.getLabelsByType(LabelType.EXPENSE)
    private val _incomeLabels = labelRepository.getLabelsByType(LabelType.INCOME)
    
    private val _filteredTransactions = _selectedMonth.flatMapLatest { calendar ->
        val (startOfMonth, endOfMonth) = getMonthRange(calendar)
        transactionRepository.getTransactionsInRange(startOfMonth, endOfMonth)
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }
    
    private val _totalIncome = _filteredTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    
    private val _totalExpenses = _filteredTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val uiState: StateFlow<FinancesUiState> = combine(
        _selectedMonth, _isLoading, _totalBudget, _hasBudget, _currencySymbol,
        _expenseLabels, _incomeLabels, _filteredTransactions, _totalIncome, _totalExpenses
    ) { args: Array<Any> ->
        FinancesUiState(
            selectedMonth = args[0] as Calendar,
            isLoading = args[1] as Boolean,
            totalBudget = args[2] as Double,
            hasBudget = args[3] as Boolean,
            currencySymbol = args[4] as String,
            expenseLabels = (args[5] as List<*>).filterIsInstance<Label>(),
            incomeLabels = (args[6] as List<*>).filterIsInstance<Label>(),
            filteredTransactions = (args[7] as List<*>).filterIsInstance<Transaction>(),
            totalIncome = args[8] as Double,
            totalExpenses = args[9] as Double
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancesUiState())
    
    fun setSelectedMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _selectedMonth.value = calendar
    }
    
    private fun getMonthRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis
        
        cal.add(Calendar.MONTH, 1)
        val endOfMonth = cal.timeInMillis
        
        return startOfMonth to endOfMonth
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            requestBudgetWidgetRefresh(application)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NanaApplication
                val transactionRepository = TransactionRepository(
                    application.database.transactionDao(),
                    application.database.budgetDao()
                )
                val labelRepository = LabelRepository(application.database.labelDao())
                FinancesViewModel(
                    transactionRepository,
                    labelRepository,
                    application.preferencesManager,
                    application
                )
            }
        }
    }
}
