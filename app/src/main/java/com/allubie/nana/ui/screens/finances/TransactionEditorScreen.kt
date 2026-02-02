package com.allubie.nana.ui.screens.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.TransactionType
import com.allubie.nana.ui.theme.*
import com.allubie.nana.util.CategoryIcons
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    transactionId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: (String) -> Unit = {},
    viewModel: TransactionEditorViewModel = viewModel(factory = TransactionEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date
    )
    
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        }
    }
    
    // Show loading indicator when loading transaction
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    
    // Labels from database
    val expenseLabels by viewModel.expenseLabels.collectAsState()
    val incomeLabels by viewModel.incomeLabels.collectAsState()
    
    // Get current labels based on transaction type
    val currentLabels = if (uiState.type == TransactionType.EXPENSE) expenseLabels else incomeLabels
    val currentLabel = currentLabels.find { it.name == uiState.category } ?: currentLabels.firstOrNull()
    
    // Set initial category from available labels when they load (only for new transactions)
    LaunchedEffect(currentLabels, transactionId) {
        if (transactionId == null && uiState.category.isEmpty() && currentLabels.isNotEmpty()) {
            viewModel.updateCategory(currentLabels.first().name)
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (transactionId == null) "NEW TRANSACTION" else "EDIT TRANSACTION",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (viewModel.saveTransaction()) {
                                    onNavigateBack()
                                }
                            },
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
                
                // Toggle Segment: Expense / Income
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        // Expense tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.updateType(TransactionType.EXPENSE) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.type == TransactionType.EXPENSE) 
                                MaterialTheme.colorScheme.primary else Color.Transparent,
                            shadowElevation = if (uiState.type == TransactionType.EXPENSE) 2.dp else 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Expense",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.type == TransactionType.EXPENSE) 
                                        Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Income tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.updateType(TransactionType.INCOME) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.type == TransactionType.INCOME) 
                                Income else Color.Transparent,
                            shadowElevation = if (uiState.type == TransactionType.INCOME) 2.dp else 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Income",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.type == TransactionType.INCOME) 
                                        Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Hero Amount Input with device numpad keyboard
                val amountFocusRequester = remember { FocusRequester() }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { amountFocusRequester.requestFocus() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Currency symbol on left
                    Text(
                        text = currencySymbol,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Amount input
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder when empty
                        if (uiState.amount.isEmpty()) {
                            Text(
                                text = "0",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                        BasicTextField(
                            value = uiState.amount,
                            onValueChange = { newValue ->
                                // Filter to only allow valid decimal input
                                val filtered = newValue.filter { it.isDigit() || it == '.' }
                                // Only allow one decimal point
                                val parts = filtered.split(".")
                                val sanitized = when {
                                    parts.size > 2 -> parts[0] + "." + parts[1]
                                    parts.size == 2 && parts[1].length > 2 -> parts[0] + "." + parts[1].take(2)
                                    else -> filtered
                                }
                                // Remove leading zeros except for "0." case
                                val final = if (sanitized.length > 1 && sanitized.startsWith("0") && !sanitized.startsWith("0.")) {
                                    sanitized.trimStart('0').ifEmpty { "0" }
                                } else {
                                    sanitized
                                }
                                if (final.length <= 10) {
                                    viewModel.updateAmount(if (final == "0") "" else final)
                                }
                            },
                            modifier = Modifier
                                .focusRequester(amountFocusRequester)
                                .widthIn(min = 50.dp),
                            textStyle = TextStyle(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Currency code on right
                    Text(
                        text = currencyCode,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Property List Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column {
                        // Category row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryPicker = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIcons.getIcon(currentLabel?.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Category",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = uiState.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        // Date row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Info.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Info,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Date",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    val isToday = remember(uiState.date) {
                                        val today = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        val selectedDate = Calendar.getInstance().apply {
                                            timeInMillis = uiState.date
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        today == selectedDate
                                    }
                                    Text(
                                        text = if (isToday) "Today, ${dateFormat.format(Date(uiState.date))}" 
                                               else dateFormat.format(Date(uiState.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        // Note row with input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            TextField(
                                value = uiState.note,
                                onValueChange = { viewModel.updateNote(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = "Add a note...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = {
                Text(
                    text = "Select Category",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentLabels.forEach { label ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateCategory(label.name)
                                    showCategoryPicker = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.category == label.name)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = CategoryIcons.getIcon(label.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label.name,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    // Add new category option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showCategoryPicker = false
                                val categoryType = if (uiState.type == TransactionType.EXPENSE) "expense" else "income"
                                onNavigateToAddCategory(categoryType)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Add New Category",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
