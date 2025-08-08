package com.allubie.nana.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.components.SwipeableItemCard
import com.allubie.nana.ui.theme.Spacing
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.utils.getCurrencySymbol

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val spent: Double,
    val budget: Double
)

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val time: String,
    val description: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    appPreferences: AppPreferences,
    onAddExpense: () -> Unit = {},
    onEditExpense: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val allExpenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    var showOverflowMenu by remember { mutableStateOf(false) }
    val currencySymbol = getCurrencySymbol(appPreferences.currency)
    
    val displayExpenses = allExpenses.map { expense: com.allubie.nana.data.entity.ExpenseEntity ->
        Expense(
            id = expense.id.toString(),
            title = expense.title,
            amount = expense.amount,
            category = expense.category,
            date = formatExpenseDate(expense.date),
            time = formatExpenseTime(expense.createdAt),
            description = expense.description
        )
    }
    
    // Use actual category data from ViewModel instead of hardcoded values
    val expenseCategories = uiState.categoriesWithSpending.map { categoryWithSpending ->
        ExpenseCategory(
            name = categoryWithSpending.category.name,
            icon = getIconFromName(categoryWithSpending.category.iconName),
            color = Color(android.graphics.Color.parseColor(categoryWithSpending.category.colorHex)),
            spent = categoryWithSpending.spent,
            budget = categoryWithSpending.category.monthlyBudget
        )
    }.filter { it.spent > 0 || it.budget > 0 }
    
    val totalSpent = uiState.totalSpent
    val totalBudget = uiState.totalBudget
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    IconButton(onClick = { 
                        // Show statistics - could expand the monthly overview card or navigate to detailed stats
                    }) {
                        Icon(Icons.Default.StackedBarChart, contentDescription = "Statistics")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showOverflowMenu = false
                                    onSettingsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage Budget") },
                                onClick = {
                                    showOverflowMenu = false
                                    onBudgetClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage Categories") },
                                onClick = {
                                    showOverflowMenu = false
                                    onCategoriesClick()
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        if (displayExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "No expenses",
                        modifier = Modifier.size(Spacing.iconMassive),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No expenses yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    MonthlyOverviewCard(
                        totalSpent = totalSpent, 
                        totalBudget = totalBudget,
                        currencySymbol = currencySymbol
                    )
                }
                
                if (expenseCategories.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No budget categories",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Create categories with budgets to track your spending effectively",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onBudgetClick,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Manage Budget")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(expenseCategories.chunked(2)) { categoryPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryPair.forEach { category ->
                                CategoryCard(
                                    category = category,
                                    currencySymbol = currencySymbol,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number of categories
                            if (categoryPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(displayExpenses.sortedByDescending { it.date }) { expense ->
                    val originalExpense = allExpenses.find { it.id.toString() == expense.id }
                    val categoryEntity = uiState.categoriesWithSpending.find { it.category.name == expense.category }?.category
                    SwipeableItemCard(
                        showPin = false,
                        showArchive = false,
                        onDelete = { originalExpense?.let { viewModel.deleteExpense(it) } }
                    ) {
                        ExpenseCard(
                            expense = expense,
                            categoryEntity = categoryEntity,
                            currencySymbol = currencySymbol,
                            onClick = { onEditExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyOverviewCard(totalSpent: Double, totalBudget: Double, currencySymbol: String) {
    val budgetProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val remainingBudget = totalBudget - totalSpent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$currencySymbol${"%.2f".format(totalSpent)} spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(budgetProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "used",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = budgetProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (budgetProgress > 0.8f) Color.Red else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget: $currencySymbol${"%.2f".format(totalBudget)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "Remaining: $currencySymbol${"%.2f".format(remainingBudget)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (remainingBudget < 0) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CategoryCard(category: ExpenseCategory, currencySymbol: String, modifier: Modifier = Modifier) {
    val progress = if (category.budget > 0) (category.spent / category.budget).toFloat() else 0f
    
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(category.color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = category.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currencySymbol${"%.0f".format(category.spent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = category.color,
                trackColor = category.color.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Budget: $currencySymbol${"%.0f".format(category.budget)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    categoryEntity: com.allubie.nana.data.entity.ExpenseCategoryEntity?,
    currencySymbol: String,
    onClick: () -> Unit = {}
) {
    val categoryIcon = categoryEntity?.let { getIconFromName(it.iconName) } ?: Icons.Default.AttachMoney
    val categoryColor = categoryEntity?.let { Color(android.graphics.Color.parseColor(it.colorHex)) } ?: MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        categoryColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = expense.category,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                expense.description?.let { description ->
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                categoryColor.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${expense.date} • ${expense.time}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "-$currencySymbol${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun getIconFromName(iconName: String): ImageVector {
    return when (iconName) {
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Fastfood" -> Icons.Default.Fastfood
        "LocalGasStation" -> Icons.Default.LocalGasStation
        "School" -> Icons.Default.School
        "Home" -> Icons.Default.Home
        "Work" -> Icons.Default.Work
        "Flight" -> Icons.Default.Flight
        "LocalHospital" -> Icons.Default.LocalHospital
        "SportsEsports" -> Icons.Default.SportsEsports
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Movie" -> Icons.Default.Movie
        "MusicNote" -> Icons.Default.MusicNote
        "Pets" -> Icons.Default.Pets
        "Build" -> Icons.Default.Build
        "AttachMoney" -> Icons.Default.AttachMoney
        else -> Icons.Default.AttachMoney
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "food" -> Icons.Default.Fastfood
        "education" -> Icons.Default.School
        "transport" -> Icons.Default.LocalGasStation
        "shopping" -> Icons.Default.ShoppingCart
        "general" -> Icons.Default.AttachMoney
        else -> Icons.Default.AttachMoney
    }
}

private fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Color(0xFFFF6B6B)
        "education" -> Color(0xFF4ECDC4)
        "transport" -> Color(0xFF45B7D1)
        "shopping" -> Color(0xFF96CEB4)
        "general" -> Color(0xFFDDA0DD)
        else -> Color(0xFFDDA0DD)
    }
}

private fun formatExpenseDate(date: kotlinx.datetime.LocalDate): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when {
        date == now -> "Today"
        date.toEpochDays() == now.toEpochDays() - 1 -> "Yesterday"
        else -> {
            val daysDiff = now.toEpochDays() - date.toEpochDays()
            when {
                daysDiff < 7 -> "$daysDiff days ago"
                daysDiff < 30 -> "${daysDiff / 7} week${if (daysDiff / 7 > 1) "s" else ""} ago"
                else -> "${daysDiff / 30} month${if (daysDiff / 30 > 1) "s" else ""} ago"
            }
        }
    }
}

private fun formatExpenseTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
    val period = if (localDateTime.hour < 12) "AM" else "PM"
    val minute = if (localDateTime.minute < 10) "0${localDateTime.minute}" else localDateTime.minute.toString()
    return "$hour:$minute $period"
}
