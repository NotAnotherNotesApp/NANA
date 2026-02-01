package com.allubie.nana.ui.screens.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.Label
import com.allubie.nana.util.CategoryIcons
import com.allubie.nana.util.CurrencyFormatter
import com.allubie.nana.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddCategory: () -> Unit = {},
    viewModel: BudgetManagerViewModel = viewModel(factory = BudgetManagerViewModel.Factory)
) {
    val budgets by viewModel.budgets.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val totalBudgetLimit by viewModel.totalBudgetLimit.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val categorySpending by viewModel.categorySpending.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val expenseLabels by viewModel.expenseLabels.collectAsState()
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var showTotalBudgetDialog by remember { mutableStateOf(false) }
    
    val remainingBudget = totalBudget - totalSpent
    val isOverBudget = remainingBudget < 0
    val remainingPercentage = if (totalBudget > 0) {
        ((remainingBudget / totalBudget) * 100).toInt().coerceIn(-100, 100)
    } else 0
    
    // Format currency using centralized formatter
    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.formatWithSymbol(amount, currencySymbol)
    }
    
    // Generate months starting from current month with year info
    val currentCalendar = remember { Calendar.getInstance() }
    val currentMonthIndex = currentCalendar.get(Calendar.MONTH)
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val monthNames = listOf("January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December")
    // Reorder months to start from current month, track year for each
    val months = (0..11).map { offset ->
        val monthIndex = (currentMonthIndex + offset) % 12
        val year = if (currentMonthIndex + offset >= 12) currentYear + 1 else currentYear
        Triple(monthIndex, year, monthNames[monthIndex])
    }
    
    if (showAddBudgetDialog || editingBudget != null) {
        BudgetDialog(
            budget = editingBudget,
            existingCategories = budgets.map { it.category },
            expenseLabels = expenseLabels,
            currencySymbol = currencySymbol,
            onDismiss = { 
                showAddBudgetDialog = false
                editingBudget = null
            },
            onSave = { category, amount, iconName, color ->
                if (editingBudget != null) {
                    viewModel.updateBudget(editingBudget!!.copy(category = category, amount = amount, iconName = iconName))
                } else {
                    viewModel.addBudget(category, amount, iconName)
                }
                showAddBudgetDialog = false
                editingBudget = null
            },
            onDelete = { budget ->
                viewModel.deleteBudget(budget)
                editingBudget = null
            },
            onAddCategory = onNavigateToAddCategory
        )
    }
    
    // Total Budget Dialog
    if (showTotalBudgetDialog) {
        TotalBudgetDialog(
            currentBudget = totalBudgetLimit,
            currencySymbol = currencySymbol,
            onDismiss = { showTotalBudgetDialog = false },
            onSave = { amount ->
                viewModel.setTotalBudgetLimit(amount)
                showTotalBudgetDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Monthly Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        // Find index of selected month in the reordered list
        val selectedMonthListIndex = remember(selectedMonth, selectedYear) {
            months.indexOfFirst { it.first == selectedMonth && it.second == selectedYear }.coerceAtLeast(0)
        }
        
        val monthListState = rememberLazyListState()
        
        // Auto-scroll to selected month on first composition
        LaunchedEffect(selectedMonthListIndex) {
            monthListState.animateScrollToItem(selectedMonthListIndex)
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Month selector with full names
            item {
                LazyRow(
                    state = monthListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(months.size, key = { index -> "month_${months[index].first}_${months[index].second}" }) { index ->
                        val (monthValue, year, monthName) = months[index]
                        val isSelected = monthValue == selectedMonth && year == selectedYear
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = if (isSelected) 8.dp else 0.dp,
                            modifier = Modifier.clickable { viewModel.selectMonth(monthValue, year) }
                        ) {
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
            
            // Budget overview card with semi-circle gauge
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Circular gauge
                        Box(
                            modifier = Modifier
                                .size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { if (isOverBudget) 1f else (remainingPercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 16.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                strokeCap = StrokeCap.Round
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isOverBudget) "Over" else "$remainingPercentage%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (isOverBudget) "Over Budget" else "Remaining Budget",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isOverBudget) formatCurrency(kotlin.math.abs(remainingBudget)) else formatCurrency(remainingBudget),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isOverBudget) "Spent ${formatCurrency(totalSpent)} of ${formatCurrency(totalBudget)}" else "of ${formatCurrency(totalBudget)} Total Limit",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Set Total Budget Button
                        OutlinedButton(
                            onClick = { showTotalBudgetDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (totalBudgetLimit > 0) "Edit Budget Limit" else "Set Budget Limit",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Alert pill if over 80% AND budget is actually set
            val usagePercentage = 100 - remainingPercentage
            if (usagePercentage >= 80 && totalBudget > 0) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = AlertWarningBackground,
                        border = BorderStroke(1.dp, AlertWarningBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = AlertWarningIcon,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Budget Alert",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AlertWarningTitle
                                )
                                Text(
                                    text = "You've spent $usagePercentage% of your budget this month.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlertWarningText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Allocations section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allocations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Budget category items
            items(budgets, key = { it.id }) { budget ->
                val spent = categorySpending[budget.category] ?: 0.0
                val progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
                val matchingLabel = expenseLabels.find { it.name == budget.category }
                val categoryIconName = matchingLabel?.iconName ?: budget.iconName
                
                BudgetCategoryItem(
                    category = budget.category,
                    budgeted = budget.amount,
                    spent = spent,
                    progress = progress,
                    iconName = categoryIconName,
                    currencySymbol = currencySymbol,
                    onClick = { editingBudget = budget }
                )
            }
            
            // Add category button - dashed border style
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { showAddBudgetDialog = true },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Category",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetCategoryItem(
    category: String,
    budgeted: Double,
    spent: Double,
    progress: Float,
    iconName: String?,
    currencySymbol: String,
    onClick: () -> Unit
) {
    // Use icon from label's iconName or fallback
    val icon = if (!iconName.isNullOrEmpty()) {
        CategoryIcons.getIcon(iconName)
    } else {
        Icons.Outlined.Category
    }
    val usagePercent = (progress * 100).toInt()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$usagePercent% used",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatWithSymbol(spent, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of ${CurrencyFormatter.formatWithSymbol(budgeted, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    progress > 1f -> MaterialTheme.colorScheme.error
                    progress > 0.8f -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetDialog(
    budget: Budget?,
    existingCategories: List<String>,
    expenseLabels: List<Label>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Int) -> Unit,
    onDelete: (Budget) -> Unit,
    onAddCategory: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(budget?.category ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Available labels not yet assigned (or current budget's label)
    val availableLabels = expenseLabels.filter { 
        it.name !in existingCategories || it.name == budget?.category 
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && budget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete the budget for \"${budget.category}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(budget)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (budget != null) "Edit Budget" else "Add Budget")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category selector
                if (budget == null) {
                    if (availableLabels.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "All categories have budgets assigned.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    onDismiss()
                                    onAddCategory()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add New Category")
                            }
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = showCategoryDropdown,
                            onExpandedChange = { showCategoryDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                availableLabels.forEach { label ->
                                    val labelIcon = CategoryIcons.getIcon(label.iconName)
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = labelIcon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(label.name)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = label.name
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                                // Add category option at bottom
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Add,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                "Add New Category",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showCategoryDropdown = false
                                        onDismiss()
                                        onAddCategory()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Editing existing budget - show read-only category name
                    OutlinedTextField(
                        value = budget.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Budget Amount") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("0.00") },
                    isError = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0,
                    supportingText = if (amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) <= 0) {
                        { Text("Amount must be greater than 0") }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val finalCategory = budget?.category ?: selectedCategory
                    val selectedLabel = expenseLabels.find { it.name == finalCategory }
                    val finalIconName = selectedLabel?.iconName ?: ""
                    val finalColor = selectedLabel?.color ?: 0
                    if (finalCategory.isNotBlank() && amountValue > 0) {
                        onSave(finalCategory, amountValue, finalIconName, finalColor)
                    }
                },
                enabled = run {
                    val finalCategory = budget?.category ?: selectedCategory
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    finalCategory.isNotBlank() && amountValue > 0
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (budget != null) {
                    TextButton(onClick = { showDeleteConfirmation = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Total Budget Dialog
@Composable
private fun TotalBudgetDialog(
    currentBudget: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Total Budget Limit") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set your overall monthly budget limit. This overrides the sum of category allocations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Total Budget") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Set to 0 to use sum of category allocations instead.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    onSave(amountValue)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}