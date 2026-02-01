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
import com.allubie.nana.data.model.Transaction
import com.allubie.nana.data.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class FinancesViewModel(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val labelDao: LabelDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val hasBudget: StateFlow<Boolean> = budgetDao.getAllBudgets()
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val totalBudget: StateFlow<Double> = budgetDao.getAllBudgets()
        .map { budgets -> budgets.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val currencySymbol: StateFlow<String> = preferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")
    
    // Labels from database
    val expenseLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val incomeLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val filteredTransactions: StateFlow<List<Transaction>> = _selectedMonth.flatMapLatest { calendar ->
        val (startOfMonth, endOfMonth) = getMonthRange(calendar)
        transactionDao.getTransactionsInRange(startOfMonth, endOfMonth)
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Make totals reactive - derived from filteredTransactions
    val totalIncome: StateFlow<Double> = filteredTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val totalExpenses: StateFlow<Double> = filteredTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
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
            transactionDao.deleteTransaction(transaction)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                FinancesViewModel(
                    application.database.transactionDao(),
                    application.database.budgetDao(),
                    application.database.labelDao(),
                    application.preferencesManager
                )
            }
        }
    }
}
