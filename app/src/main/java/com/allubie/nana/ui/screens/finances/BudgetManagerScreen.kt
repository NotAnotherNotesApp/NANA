package com.allubie.nana.ui.screens.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.Budget
import com.allubie.nana.data.model.BudgetPeriod
import com.allubie.nana.data.model.ExpenseCategories
import java.text.NumberFormat
import java.util.*
import com.allubie.nana.ui.theme.*

// Category colors - using theme colors
private val CategoryColors = mapOf(
    "Food" to CategoryFood,
    "Transport" to CategoryTransport,
    "Entertainment" to CategoryEntertainment,
    "Shopping" to CategoryShopping,
    "Education" to CategoryEducation,
    "Health" to CategoryHealth,
    "Bills" to CategoryBills,
    "Other" to CategoryOther
)

// Category icons
private val CategoryIcons = mapOf(
    "Food" to Icons.Outlined.Restaurant,
    "Transport" to Icons.Outlined.DirectionsCar,
    "Entertainment" to Icons.Outlined.Movie,
    "Shopping" to Icons.Outlined.ShoppingBag,
    "Education" to Icons.Outlined.School,
    "Health" to Icons.Outlined.LocalHospital,
    "Bills" to Icons.Outlined.Receipt,
    "Other" to Icons.Outlined.MoreHoriz
)

// Available icons for custom categories
private val AvailableCategoryIcons = listOf(
    "restaurant" to Icons.Outlined.Restaurant,
    "car" to Icons.Outlined.DirectionsCar,
    "movie" to Icons.Outlined.Movie,
    "shopping" to Icons.Outlined.ShoppingBag,
    "school" to Icons.Outlined.School,
    "hospital" to Icons.Outlined.LocalHospital,
    "receipt" to Icons.Outlined.Receipt,
    "home" to Icons.Outlined.Home,
    "flight" to Icons.Outlined.Flight,
    "fitness" to Icons.Outlined.FitnessCenter,
    "phone" to Icons.Outlined.Phone,
    "wifi" to Icons.Outlined.Wifi,
    "pets" to Icons.Outlined.Pets,
    "child" to Icons.Outlined.ChildCare,
    "coffee" to Icons.Outlined.Coffee,
    "sports" to Icons.Outlined.SportsBasketball,
    "music" to Icons.Outlined.MusicNote,
    "games" to Icons.Outlined.SportsEsports,
    "book" to Icons.Outlined.Book,
    "gift" to Icons.Outlined.CardGiftcard,
    "savings" to Icons.Outlined.Savings,
    "credit" to Icons.Outlined.CreditCard,
    "work" to Icons.Outlined.Work,
    "tools" to Icons.Outlined.Build,
    "smoking" to Icons.Outlined.SmokingRooms,
    "liquor" to Icons.Outlined.Liquor,
    "local_bar" to Icons.Outlined.LocalBar,
    "cake" to Icons.Outlined.Cake,
    "beach" to Icons.Outlined.BeachAccess,
    "spa" to Icons.Outlined.Spa,
    "laundry" to Icons.Outlined.LocalLaundryService,
    "gas" to Icons.Outlined.LocalGasStation,
    "parking" to Icons.Outlined.LocalParking,
    "train" to Icons.Outlined.Train,
    "bike" to Icons.Outlined.PedalBike,
    "brush" to Icons.Outlined.Brush
)

// Get icon from name
private fun getIconFromName(iconName: String): ImageVector {
    return AvailableCategoryIcons.find { it.first == iconName }?.second ?: Icons.Outlined.Category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: BudgetManagerViewModel = viewModel(factory = BudgetManagerViewModel.Factory)
) {
    val budgets by viewModel.budgets.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val totalBudgetLimit by viewModel.totalBudgetLimit.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val categorySpending by viewModel.categorySpending.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var showTotalBudgetDialog by remember { mutableStateOf(false) }
    
    val remainingBudget = totalBudget - totalSpent
    val remainingPercentage = if (totalBudget > 0) ((remainingBudget / totalBudget) * 100).toInt().coerceAtLeast(0) else 0
    
    // Format currency with symbol from settings
    fun formatCurrency(amount: Double): String {
        val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(kotlin.math.abs(amount))
        return "$currencySymbol$formatted"
    }
    
    // Generate months starting from current month
    val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
    val monthNames = listOf("January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December")
    // Reorder months to start from current month
    val months = (0..11).map { offset ->
        val monthIndex = (currentMonthIndex + offset) % 12
        Pair(monthIndex, monthNames[monthIndex])
    }
    
    if (showAddBudgetDialog || editingBudget != null) {
        BudgetDialog(
            budget = editingBudget,
            existingCategories = budgets.map { it.category },
            currencySymbol = currencySymbol,
            onDismiss = { 
                showAddBudgetDialog = false
                editingBudget = null
            },
            onSave = { category, amount, iconName ->
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
            }
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
        val selectedMonthListIndex = remember(selectedMonth) {
            months.indexOfFirst { it.first == selectedMonth }.coerceAtLeast(0)
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
                    items(months.size, key = { index -> "month_${months[index].first}" }) { index ->
                        val (monthValue, monthName) = months[index]
                        val isSelected = monthValue == selectedMonth
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = if (isSelected) 8.dp else 0.dp,
                            modifier = Modifier.clickable { viewModel.selectMonth(monthValue) }
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
                                progress = { (remainingPercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 16.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = MaterialTheme.colorScheme.primary,
                                strokeCap = StrokeCap.Round
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$remainingPercentage%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Remaining Budget",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(remainingBudget.coerceAtLeast(0.0)),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "of ${formatCurrency(totalBudget)} Total Limit",
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
                val categoryColor = CategoryColors[budget.category] ?: MaterialTheme.colorScheme.primary
                
                BudgetCategoryItem(
                    category = budget.category,
                    budgeted = budget.amount,
                    spent = spent,
                    progress = progress,
                    color = categoryColor,
                    iconName = budget.iconName,
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
    color: Color,
    iconName: String,
    currencySymbol: String,
    onClick: () -> Unit
) {
    // Use custom icon if set, otherwise use predefined category icon
    val icon = if (iconName.isNotEmpty()) {
        getIconFromName(iconName)
    } else {
        CategoryIcons[category] ?: Icons.Outlined.Category
    }
    val usagePercent = (progress * 100).toInt()
    
    // Format currency with symbol
    fun formatCurrency(amount: Double): String {
        val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
        return "$currencySymbol$formatted"
    }
    
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
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
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
                        text = formatCurrency(spent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of ${formatCurrency(budgeted)}",
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
                    else -> color
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
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit,
    onDelete: (Budget) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(budget?.category ?: "") }
    var customCategory by remember { mutableStateOf(if (budget != null && budget.category !in ExpenseCategories.list) budget.category else "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var useCustomCategory by remember { mutableStateOf(budget != null && budget.category !in ExpenseCategories.list) }
    var selectedIconName by remember { mutableStateOf(budget?.iconName ?: "category") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val availableCategories = ExpenseCategories.list.filter { 
        it !in existingCategories || it == budget?.category 
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 450.dp)
            ) {
                // Category selector
                if (budget == null) {
                    // Toggle between predefined and custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !useCustomCategory,
                            onClick = { useCustomCategory = false },
                            label = { Text("Predefined") }
                        )
                        FilterChip(
                            selected = useCustomCategory,
                            onClick = { useCustomCategory = true },
                            label = { Text("Custom") }
                        )
                    }
                    
                    if (useCustomCategory) {
                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Icon selector for custom category
                        Text(
                            text = "Choose Icon",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(AvailableCategoryIcons, key = { it.first }) { (iconName, icon) ->
                                val isSelected = selectedIconName == iconName
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedIconName = iconName },
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = iconName,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
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
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                availableCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = CategoryIcons[category] ?: Icons.Outlined.Category,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(category)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Editing existing budget
                    OutlinedTextField(
                        value = if (useCustomCategory) customCategory else selectedCategory,
                        onValueChange = { 
                            if (useCustomCategory) customCategory = it 
                            else selectedCategory = it 
                        },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Icon selector for existing custom category
                    if (budget.category !in ExpenseCategories.list || budget.iconName.isNotEmpty()) {
                        Text(
                            text = "Choose Icon",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(AvailableCategoryIcons, key = { it.first }) { (iconName, icon) ->
                                val isSelected = selectedIconName == iconName
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedIconName = iconName },
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = iconName,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Amount input (optional)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Budget Amount") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("0.00") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val finalCategory = if (budget != null) {
                        if (useCustomCategory) customCategory else selectedCategory
                    } else {
                        if (useCustomCategory) customCategory else selectedCategory
                    }
                    val finalIconName = if (useCustomCategory || (budget != null && budget.category !in ExpenseCategories.list)) {
                        selectedIconName
                    } else ""
                    if (finalCategory.isNotBlank()) {
                        onSave(finalCategory, amountValue, finalIconName)
                    }
                },
                enabled = run {
                    val finalCategory = if (budget != null) {
                        if (useCustomCategory) customCategory else selectedCategory
                    } else {
                        if (useCustomCategory) customCategory else selectedCategory
                    }
                    finalCategory.isNotBlank()
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