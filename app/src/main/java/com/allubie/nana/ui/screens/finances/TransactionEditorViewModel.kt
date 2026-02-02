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
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.model.Transaction
import com.allubie.nana.data.model.TransactionType
import com.allubie.nana.data.repository.LabelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",  // Will be set from available labels
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

class TransactionEditorViewModel(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val labelDao: LabelDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionEditorUiState())
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()
    
    private val _customBudgets = MutableStateFlow<List<Budget>>(emptyList())
    val customBudgets: StateFlow<List<Budget>> = _customBudgets.asStateFlow()
    
    // Labels from database
    val expenseLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val incomeLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val currencySymbol: StateFlow<String> = preferencesManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")
    
    val currencyCode: StateFlow<String> = preferencesManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")
    
    private val labelRepository = LabelRepository(labelDao)
    
    init {
        loadCustomBudgets()
        seedLabelsIfNeeded()
    }
    
    private fun seedLabelsIfNeeded() {
        viewModelScope.launch {
            labelRepository.seedPresetsIfNeeded()
        }
    }
    
    private fun loadCustomBudgets() {
        viewModelScope.launch {
            budgetDao.getAllBudgets().collect { budgets ->
                // Filter for custom categories (non-empty category that's not a preset)
                val presetCategories = listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Education", "Other", "")
                _customBudgets.value = budgets.filter { it.category !in presetCategories }
            }
        }
    }
    
    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val transaction = transactionDao.getTransactionById(transactionId)
            if (transaction != null) {
                _uiState.update {
                    it.copy(
                        id = transaction.id,
                        title = transaction.title,
                        amount = transaction.amount.toString(),
                        type = transaction.type,
                        category = transaction.category,
                        note = transaction.note,
                        date = transaction.date,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun updateAmount(amount: String) {
        // Only allow valid decimal input
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = amount) }
        }
    }
    
    fun updateType(type: TransactionType) {
        viewModelScope.launch {
            val labels = if (type == TransactionType.EXPENSE) expenseLabels.value else incomeLabels.value
            val firstCategory = labels.firstOrNull()?.name ?: ""
            _uiState.update { 
                it.copy(
                    type = type,
                    category = firstCategory
                ) 
            }
        }
    }
    
    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }
    
    fun saveTransaction(): Boolean {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        
        // Validate amount is positive
        if (amount == null || amount <= 0) {
            return false
        }
        
        viewModelScope.launch {
            val transaction = Transaction(
                id = state.id ?: 0,
                title = state.title,
                amount = amount,
                type = state.type,
                category = state.category,
                note = state.note,
                date = state.date,
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)
        }
        return true
    }
    
    fun deleteTransaction() {
        viewModelScope.launch {
            _uiState.value.id?.let { id ->
                transactionDao.deleteTransactionById(id)
            }
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                TransactionEditorViewModel(
                    application.database.transactionDao(),
                    application.database.budgetDao(),
                    application.database.labelDao(),
                    application.preferencesManager
                )
            }
        }
    }
}
