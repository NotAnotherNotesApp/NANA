package com.allubie.nana.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import com.allubie.nana.ui.screens.expenses.CATEGORY_AVAILABLE_ICONS
import com.allubie.nana.ui.screens.expenses.CATEGORY_DEFAULT_COLORS
import com.allubie.nana.utils.getCurrencySymbol
import com.allubie.nana.utils.parseLocalizedDouble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBudgetEditorScreen(
    viewModel: ExpensesViewModel,
    appPreferences: AppPreferences,
    editing: ExpenseCategoryEntity?,
    existingNames: Set<String>,
    onClose: () -> Unit,
    onSaved: (String) -> Unit
) {
    val currencySymbol = getCurrencySymbol(appPreferences.currency)
    val isEditingExisting = editing != null

    var title by remember { mutableStateOf(editing?.name ?: "") }
    var budgetText by remember { mutableStateOf(editing?.monthlyBudget?.toString() ?: "0.0") }
    var selectedIcon by remember { mutableStateOf(editing?.iconName ?: "ShoppingCart") }
    var selectedColor by remember { mutableStateOf(editing?.colorHex ?: CATEGORY_DEFAULT_COLORS.first()) }

    val titleTrim = title.trim()
    val duplicate = remember(titleTrim, existingNames, editing) {
        if (titleTrim.isEmpty()) false else existingNames.contains(titleTrim)
    }
    val budgetValue = parseLocalizedDouble(budgetText)
    val budgetError = budgetValue == null || (budgetValue ?: -1.0) < 0.0
    val canSave = titleTrim.isNotEmpty() && !duplicate && !budgetError

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditingExisting) "Edit Category" else "New Category", fontWeight = FontWeight.Medium)
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val budget = budgetValue ?: 0.0
                        if (isEditingExisting) {
                            val old = editing!!
                            if (titleTrim != old.name) {
                                viewModel.renameCategory(
                                    oldName = old.name,
                                    newName = titleTrim,
                                    iconName = selectedIcon,
                                    colorHex = selectedColor,
                                    monthlyBudget = budget
                                )
                            } else {
                                viewModel.updateCategory(
                                    old.copy(
                                        iconName = selectedIcon,
                                        colorHex = selectedColor,
                                        monthlyBudget = budget
                                    )
                                )
                            }
                            onSaved("Category updated")
                            onClose()
                        } else {
                            viewModel.createCategory(
                                name = titleTrim,
                                iconName = selectedIcon,
                                colorHex = selectedColor,
                                monthlyBudget = budget
                            )
                            onSaved("Category created")
                            onClose()
                        }
                    }, enabled = canSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                isError = titleTrim.isEmpty() || duplicate,
                supportingText = {
                    when {
                        titleTrim.isEmpty() -> Text("Title is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        duplicate -> Text("Duplicate title", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Icon selection
            Text(text = "Icon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORY_AVAILABLE_ICONS) { data ->
                    IconChoice(
                        icon = data.icon,
                        isSelected = selectedIcon == data.name,
                        color = Color(android.graphics.Color.parseColor(selectedColor)),
                        onClick = { selectedIcon = data.name }
                    )
                }
            }

            // Color selection (optional but helpful)
            Text(text = "Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORY_DEFAULT_COLORS) { colorHex ->
                    ColorChoice(
                        color = Color(android.graphics.Color.parseColor(colorHex)),
                        isSelected = selectedColor == colorHex,
                        onClick = { selectedColor = colorHex }
                    )
                }
            }

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Monthly Budget") },
                prefix = { Text(getCurrencySymbol(appPreferences.currency)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = budgetError,
                supportingText = {
                    if (budgetError) Text("Enter a valid amount (>= 0)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// constants moved to CategoryUi.kt

@Composable
private fun IconChoice(icon: ImageVector, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ColorChoice(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = color, shape = CircleShape)
            .border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
    }
}
