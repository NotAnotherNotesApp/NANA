package com.allubie.nana.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.allubie.nana.data.entity.ExpenseEntity
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ExpensesViewModel(private val repository: ExpenseRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()
    
    val allExpenses = repository.getAllExpenses()
    val allCategories = repository.getAllCategories()
    
    init {
        loadMonthlyData()
    }
    
    private fun loadMonthlyData() {
        viewModelScope.launch {
            allCategories.combine(allExpenses) { categories, expenses ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val startOfMonth = LocalDate(today.year, today.month, 1)
                
                val monthlyExpenses = expenses.filter { 
                    it.date >= startOfMonth && it.date <= today 
                }
                
                val categoriesWithSpending = categories.map { category ->
                    val spent = monthlyExpenses
                        .filter { it.category == category.name }
                        .sumOf { it.amount }
                    
                    CategoryWithSpending(
                        category = category,
                        spent = spent,
                        progress = if (category.monthlyBudget > 0) spent / category.monthlyBudget else 0.0
                    )
                }
                
                val totalSpent = monthlyExpenses.sumOf { it.amount }
                val totalBudget = categories.sumOf { it.monthlyBudget }
                
                ExpensesUiState(
                    categoriesWithSpending = categoriesWithSpending,
                    monthlyExpenses = monthlyExpenses,
                    totalSpent = totalSpent,
                    totalBudget = totalBudget,
                    budgetProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun createExpense(
        title: String,
        amount: Double,
        category: String,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        description: String? = null
    ) {
        viewModelScope.launch {
            repository.createExpense(title, amount, category, date, description)
        }
    }
    
    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }
    
    fun getExpenseById(id: String, callback: (ExpenseEntity?) -> Unit) {
        viewModelScope.launch {
            val expense = repository.getExpenseById(id)
            callback(expense)
        }
    }
    
    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
    
    fun createCategory(
        name: String,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.createCategory(name, iconName, colorHex, monthlyBudget)
        }
    }
    
    fun updateCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun renameCategory(
        oldName: String,
        newName: String,
        iconName: String,
        colorHex: String,
        monthlyBudget: Double
    ) {
        viewModelScope.launch {
            repository.renameCategory(oldName, newName, iconName, colorHex, monthlyBudget)
        }
    }
    
    fun deleteCategory(category: ExpenseCategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
    
    fun getExpensesForDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            repository.getExpensesInRange(startDate, endDate).collect { expenses ->
                _uiState.value = _uiState.value.copy(filteredExpenses = expenses)
            }
        }
    }
    
    fun getExpensesForCategory(category: String) {
        viewModelScope.launch {
            repository.getExpensesByCategory(category).collect { expenses ->
                _uiState.value = _uiState.value.copy(filteredExpenses = expenses)
            }
        }
    }
    
    fun clearFilter() {
        _uiState.value = _uiState.value.copy(filteredExpenses = emptyList())
    }
}

data class CategoryWithSpending(
    val category: ExpenseCategoryEntity,
    val spent: Double,
    val progress: Double
)

data class ExpensesUiState(
    val categoriesWithSpending: List<CategoryWithSpending> = emptyList(),
    val monthlyExpenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalBudget: Double = 0.0,
    val budgetProgress: Float = 0f,
    val selectedCategory: String? = null,
    val isLoading: Boolean = false
)

class ExpensesViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
