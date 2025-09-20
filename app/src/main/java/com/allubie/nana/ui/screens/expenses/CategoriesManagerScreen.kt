package com.allubie.nana.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.utils.getCurrencySymbol
import com.allubie.nana.utils.parseLocalizedDouble
import com.allubie.nana.ui.screens.expenses.CATEGORY_AVAILABLE_ICONS
import com.allubie.nana.ui.screens.expenses.CATEGORY_DEFAULT_COLORS
import com.allubie.nana.ui.screens.expenses.getIconFromName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesManagerScreen(
    viewModel: ExpensesViewModel,
    appPreferences: AppPreferences,
    onBackPressed: () -> Unit
) {
    val allCategories by viewModel.allCategories.collectAsState(initial = emptyList())
    val currencySymbol = getCurrencySymbol(appPreferences.currency)
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Expense Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (allCategories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                                text = "No categories yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Create your first expense category to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(allCategories) { category ->
                    CategoryCard(
                        category = category,
                        currencySymbol = currencySymbol,
                        onEdit = { editingCategory = category },
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        CategoryDialog(
            category = null,
            currencySymbol = currencySymbol,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, iconName, colorHex, budget ->
                viewModel.createCategory(name, iconName, colorHex, budget)
                showAddCategoryDialog = false
            },
            existingNames = allCategories.map { it.name }.toSet()
        )
    }
    
    // Edit Category Dialog
    editingCategory?.let { category ->
        CategoryDialog(
            category = category,
            currencySymbol = currencySymbol,
            onDismiss = { editingCategory = null },
            onSave = { name, iconName, colorHex, budget ->
                if (name != category.name) {
                    // Perform safe rename that updates dependent expenses
                    viewModel.renameCategory(
                        oldName = category.name,
                        newName = name,
                        iconName = iconName,
                        colorHex = colorHex,
                        monthlyBudget = budget
                    )
                } else {
                    viewModel.updateCategory(
                        category.copy(
                            iconName = iconName,
                            colorHex = colorHex,
                            monthlyBudget = budget
                        )
                    )
                }
                editingCategory = null
            },
            existingNames = allCategories.map { it.name }.filter { it != category.name }.toSet()
        )
    }
}

@Composable
private fun CategoryCard(
    category: ExpenseCategoryEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(android.graphics.Color.parseColor(category.colorHex)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconFromName(category.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Category Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Budget: $currencySymbol${String.format("%.2f", category.monthlyBudget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action Buttons
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            
            IconButton(onClick = { showDeleteConfirmation = true }) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Category") },
            text = { 
                Text("Are you sure you want to delete \"${category.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
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
}

@Composable
private fun CategoryDialog(
    category: ExpenseCategoryEntity?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double) -> Unit,
    existingNames: Set<String> = emptySet()
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.iconName ?: "ShoppingCart") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: "#FF6B6B") }
    var budget by remember { mutableStateOf(category?.monthlyBudget?.toString() ?: "0.0") }
    
    val isEditing = category != null
    val isDuplicate = remember(name, existingNames, category) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) false else existingNames.contains(trimmed)
    }
    val canSave = name.isNotBlank() && !isDuplicate && parseLocalizedDouble(budget) != null
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Category" else "Add Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Category Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank() || isDuplicate,
                    supportingText = {
                        when {
                            name.isBlank() -> Text(
                                text = "Title is required",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            isDuplicate -> Text(
                                text = "Duplicate title",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
                
                // Icon Selection
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORY_AVAILABLE_ICONS) { iconData ->
                        IconSelectionItem(
                            icon = iconData.icon,
                            iconName = iconData.name,
                            isSelected = selectedIcon == iconData.name,
                            color = Color(android.graphics.Color.parseColor(selectedColor)),
                            onClick = { selectedIcon = iconData.name }
                        )
                    }
                }
                
                // Color Selection
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORY_DEFAULT_COLORS) { colorHex ->
                        ColorSelectionItem(
                            color = Color(android.graphics.Color.parseColor(colorHex)),
                            isSelected = selectedColor == colorHex,
                            onClick = { selectedColor = colorHex }
                        )
                    }
                }
                
                // Budget Input
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Monthly Budget") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val budgetValue = parseLocalizedDouble(budget) ?: 0.0
                            onSave(name, selectedIcon, selectedColor, budgetValue)
                        },
                        enabled = canSave
                    ) {
                        Text(if (isEditing) "Update" else "Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun IconSelectionItem(
    icon: ImageVector,
    iconName: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconName,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ColorSelectionItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = color, shape = CircleShape)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
            )
        }
    }
}

// icons/colors and getIconFromName are centralized in CategoryUi.kt
