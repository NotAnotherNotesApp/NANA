package com.allubie.nana.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.ui.viewmodel.CategoryWithSpending
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import com.allubie.nana.utils.getCurrencySymbol
import com.allubie.nana.utils.parseLocalizedDouble
import com.allubie.nana.ui.screens.expenses.getIconFromName
import com.allubie.nana.ui.components.SwipeableItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagerScreen(
    viewModel: ExpensesViewModel,
    appPreferences: AppPreferences,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState(initial = emptyList())
    val currencySymbol = getCurrencySymbol(appPreferences.currency)

    var isSelecting by remember { mutableStateOf(false) }
    var totalBudgetText by remember { mutableStateOf(appPreferences.totalMonthlyBudget.toString()) }
    var totalBudgetError by remember { mutableStateOf(false) }
    var editorTarget by remember { mutableStateOf<ExpenseCategoryEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var hasShownSelectHint by remember { mutableStateOf(false) }

    fun enterSelectMode() {
        totalBudgetText = appPreferences.totalMonthlyBudget.toString()
        totalBudgetError = false
        isSelecting = true
    }

    fun exitSelectMode() {
        totalBudgetError = false
        isSelecting = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Add Category -> open full editor
                    IconButton(onClick = {
                        editorTarget = null
                        showEditor = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                    IconButton(onClick = {
                        val nowSelecting = !isSelecting
                        isSelecting = nowSelecting
                        if (nowSelecting) {
                            enterSelectMode()
                            if (!hasShownSelectHint) {
                                snackbarMessage = "Tap a category to edit"
                                hasShownSelectHint = true
                            }
                        } else {
                            exitSelectMode()
                        }
                    }) {
                        Icon(if (isSelecting) Icons.Default.Check else Icons.Default.Edit, contentDescription = if (isSelecting) "Done" else "Edit")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        LaunchedEffect(snackbarMessage) {
            snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                snackbarMessage = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                currencySymbol = currencySymbol,
                totalCap = appPreferences.totalMonthlyBudget,
                totalCapText = totalBudgetText,
                isEditing = true,
                isError = totalBudgetError,
                onTotalCapChange = { newText ->
                    totalBudgetText = newText
                    val parsed = parseLocalizedDouble(newText.trim())
                    totalBudgetError = parsed == null || parsed < 0.0
                    if (parsed != null && parsed >= 0.0) {
                        if (parsed != appPreferences.totalMonthlyBudget) {
                            appPreferences.updateTotalMonthlyBudget(parsed)
                            snackbarMessage = "Total cap updated"
                        }
                    }
                },
                totalSpent = uiState.totalSpent
            )

            Text(
                text = "Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // hint is now shown via snackbar on first entry to select mode

            if (allCategories.isEmpty()) {
                EmptyStateCard()
            } else {
                val withSpendingNames = uiState.categoriesWithSpending.map { it.category.name }.toSet()
                val extras = allCategories.filter { it.name !in withSpendingNames }
                val combined = uiState.categoriesWithSpending + extras.map {
                    CategoryWithSpending(category = it, spent = 0.0, progress = 0.0)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(combined, key = { it.category.name }) { cws ->
                        SwipeableItemCard(
                            showPin = false,
                            showArchive = false,
                            onDelete = {
                                val deleted = cws.category
                                scope.launch {
                                    viewModel.deleteCategory(deleted)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Category deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.createCategory(
                                            name = deleted.name,
                                            iconName = deleted.iconName,
                                            colorHex = deleted.colorHex,
                                            monthlyBudget = deleted.monthlyBudget
                                        )
                                    }
                                }
                            }
                        ) {
                            CategoryBudgetRow(
                                item = cws,
                                currencySymbol = currencySymbol,
                                enableClick = isSelecting,
                                onOpenEditor = {
                                    editorTarget = cws.category
                                    showEditor = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full-screen editor for create/edit
    if (showEditor) {
        val existingNames = remember(allCategories, editorTarget) {
            val base = allCategories.map { it.name }.toMutableSet()
            editorTarget?.let { base.remove(it.name) }
            base.toSet()
        }
        CategoryBudgetEditorScreen(
            viewModel = viewModel,
            appPreferences = appPreferences,
            editing = editorTarget,
            existingNames = existingNames,
            onClose = { showEditor = false },
            onSaved = { msg -> snackbarMessage = msg }
        )
    }
}

@Composable
private fun SummaryCard(
    currencySymbol: String,
    totalCap: Double,
    totalCapText: String,
    isEditing: Boolean,
    isError: Boolean,
    onTotalCapChange: (String) -> Unit,
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Monthly Budget Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    if (isEditing) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = totalCapText,
                            onValueChange = onTotalCapChange,
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = isError,
                            supportingText = {
                                if (isError) {
                                    Text(
                                        text = "Enter a valid amount (>= 0)",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "$currencySymbol${String.format("%.2f", totalCap)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.2f", totalSpent)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (totalCap > 0 && totalSpent > totalCap) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { if (totalCap > 0) (totalSpent / totalCap).toFloat().coerceAtMost(1f) else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = if (totalCap > 0 && totalSpent / totalCap > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (totalCap > 0) "${((totalSpent / totalCap).coerceAtMost(1.0) * 100).toInt()}% of budget used" else "No cap set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                text = "Create a category first to set budgets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    item: CategoryWithSpending,
    currencySymbol: String,
    enableClick: Boolean,
    onOpenEditor: () -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .then(if (enableClick) Modifier.clickable { onOpenEditor() } else Modifier)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(android.graphics.Color.parseColor(item.category.colorHex)),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = getIconFromName(item.category.iconName),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.category.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Spent: $currencySymbol${String.format("%.2f", item.spent)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.2f", item.category.monthlyBudget)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (item.category.monthlyBudget > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress.toFloat().coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        item.progress > 1.0 -> MaterialTheme.colorScheme.error
                        item.progress > 0.8 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    text = "${(item.progress * 100).toInt()}% of budget used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// icon mapping moved to CategoryUi.kt

// Removed inline add dialog in favor of full-screen editor
