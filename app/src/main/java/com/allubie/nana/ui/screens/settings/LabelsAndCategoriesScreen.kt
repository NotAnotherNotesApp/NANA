package com.allubie.nana.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelColors
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.repository.LabelRepository
import com.allubie.nana.util.CategoryIcons
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ViewModel
class LabelsViewModel(private val repository: LabelRepository) : ViewModel() {
    
    // Loading state - true until first data arrives
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Single query, filter in memory - Eagerly starts flow immediately
    private val allLabels: StateFlow<List<Label>> = repository.getAllLabels()
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val noteLabels: StateFlow<List<Label>> = allLabels
        .map { labels -> labels.filter { it.type == LabelType.NOTE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val eventLabels: StateFlow<List<Label>> = allLabels
        .map { labels -> labels.filter { it.type == LabelType.EVENT } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val expenseCategories: StateFlow<List<Label>> = allLabels
        .map { labels -> labels.filter { it.type == LabelType.EXPENSE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val incomeCategories: StateFlow<List<Label>> = allLabels
        .map { labels -> labels.filter { it.type == LabelType.INCOME } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    fun createLabel(name: String, type: LabelType, iconName: String?, color: Int) {
        viewModelScope.launch {
            repository.createLabel(name, type, iconName, color)
        }
    }
    
    fun updateLabel(label: Label) {
        viewModelScope.launch {
            repository.updateLabel(label)
        }
    }
    
    fun deleteLabel(label: Label) {
        viewModelScope.launch {
            if (label.isPreset) {
                repository.hideLabel(label.id)
            } else {
                repository.deleteLabel(label.id)
            }
        }
    }
    
    class Factory(private val database: NanaDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LabelsViewModel(LabelRepository(database.labelDao())) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsAndCategoriesScreen(
    database: NanaDatabase,
    onNavigateBack: () -> Unit,
    initialLabelType: String? = null
) {
    val viewModel: LabelsViewModel = viewModel(
        factory = LabelsViewModel.Factory(database)
    )
    
    val isLoading by viewModel.isLoading.collectAsState()
    val noteLabels by viewModel.noteLabels.collectAsState()
    val eventLabels by viewModel.eventLabels.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }
    var addingType by remember { mutableStateOf<LabelType?>(null) }
    
    // Handle initial type from navigation (for quick add mode)
    LaunchedEffect(initialLabelType) {
        when (initialLabelType) {
            "expense" -> {
                addingType = LabelType.EXPENSE
                showAddDialog = true
            }
            "income" -> {
                addingType = LabelType.INCOME
                showAddDialog = true
            }
            "note" -> {
                addingType = LabelType.NOTE
                showAddDialog = true
            }
            "event" -> {
                addingType = LabelType.EVENT
                showAddDialog = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Labels & Categories",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
            // Note Labels
            item {
                LabelSection(
                    title = "Note Labels",
                    labels = noteLabels,
                    showIcon = false,
                    onAddClick = {
                        addingType = LabelType.NOTE
                        showAddDialog = true
                    },
                    onLabelClick = { label -> editingLabel = label }
                )
            }
            
            // Schedule Labels
            item {
                LabelSection(
                    title = "Schedule Labels",
                    labels = eventLabels,
                    showIcon = true,
                    onAddClick = {
                        addingType = LabelType.EVENT
                        showAddDialog = true
                    },
                    onLabelClick = { label -> editingLabel = label }
                )
            }
            
            // Expense Categories
            item {
                LabelSection(
                    title = "Expense Categories",
                    labels = expenseCategories,
                    showIcon = true,
                    onAddClick = {
                        addingType = LabelType.EXPENSE
                        showAddDialog = true
                    },
                    onLabelClick = { label -> editingLabel = label }
                )
            }
            
            // Income Categories
            item {
                LabelSection(
                    title = "Income Categories",
                    labels = incomeCategories,
                    showIcon = true,
                    onAddClick = {
                        addingType = LabelType.INCOME
                        showAddDialog = true
                    },
                    onLabelClick = { label -> editingLabel = label }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        }
    }
    
    // Add Label Dialog
    if (showAddDialog && addingType != null) {
        // Get existing colors for the label type to auto-assign unique color
        val existingColors = remember(addingType, noteLabels, expenseCategories, incomeCategories, eventLabels) {
            when (addingType) {
                LabelType.NOTE -> noteLabels.map { it.color }.toSet()
                LabelType.EXPENSE -> expenseCategories.map { it.color }.toSet()
                LabelType.INCOME -> incomeCategories.map { it.color }.toSet()
                LabelType.EVENT -> eventLabels.map { it.color }.toSet()
                else -> emptySet()
            }
        }
        
        LabelEditorDialog(
            labelType = addingType!!,
            existingLabel = null,
            existingColorsForType = existingColors,
            onDismiss = { 
                showAddDialog = false
                // If opened via quick-add mode, navigate back
                if (initialLabelType != null) {
                    onNavigateBack()
                }
                addingType = null
            },
            onSave = { name, iconName, color ->
                viewModel.createLabel(name, addingType!!, iconName, color)
                showAddDialog = false
                // If opened via quick-add mode, navigate back after save
                if (initialLabelType != null) {
                    onNavigateBack()
                }
                addingType = null
            },
            onDelete = null
        )
    }
    
    // Edit Label Dialog
    if (editingLabel != null) {
        LabelEditorDialog(
            labelType = editingLabel!!.type,
            existingLabel = editingLabel,
            existingColorsForType = emptySet(), // Not needed for editing - label already has color
            onDismiss = { editingLabel = null },
            onSave = { name, iconName, color ->
                viewModel.updateLabel(
                    editingLabel!!.copy(name = name, iconName = iconName, color = color)
                )
                editingLabel = null
            },
            onDelete = { 
                viewModel.deleteLabel(editingLabel!!)
                editingLabel = null
            }
        )
    }
}

@Composable
private fun LabelSection(
    title: String,
    labels: List<Label>,
    showIcon: Boolean,
    onAddClick: () -> Unit,
    onLabelClick: (Label) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                labels.forEach { label ->
                    LabelChip(
                        label = label,
                        showIcon = showIcon,
                        onClick = { onLabelClick(label) }
                    )
                }
                
                // Add button
                AddLabelChip(
                    text = if (showIcon) "Add Category" else "Add Label",
                    onClick = onAddClick
                )
            }
        }
    }
}

@Composable
private fun LabelChip(
    label: Label,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showIcon && label.iconName != null) {
                Icon(
                    imageVector = CategoryIcons.getIcon(label.iconName),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = label.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AddLabelChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelEditorDialog(
    labelType: LabelType,
    existingLabel: Label?,
    existingColorsForType: Set<Int>,
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String?, color: Int) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(existingLabel?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(existingLabel?.iconName) }
    // Auto-assign a unique color for new labels
    val selectedColor = existingLabel?.color 
        ?: LabelColors.getNextAvailableColor(existingColorsForType, labelType)
    
    val showIconPicker = labelType != LabelType.NOTE
    val isEditing = existingLabel != null
    val title = when {
        isEditing -> "Edit ${if (labelType == LabelType.NOTE) "Label" else "Category"}"
        else -> "Add ${if (labelType == LabelType.NOTE) "Label" else "Category"}"
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Icon Picker (for non-note labels)
                if (showIconPicker) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val availableIcons = listOf(
                        "restaurant", "local_cafe", "fastfood", "local_bar",
                        "directions_bus", "directions_car", "flight", "train",
                        "shopping_bag", "shopping_cart", "store", "local_mall",
                        "movie", "sports_esports", "music_note", "sports_basketball",
                        "receipt_long", "receipt", "payments", "credit_card",
                        "savings", "wallet", "favorite", "local_hospital",
                        "fitness_center", "spa", "school", "auto_stories",
                        "work", "laptop", "phone_android", "home",
                        "local_laundry_service", "pets", "child_care", "person",
                        "beach_access", "hotel", "event", "cake",
                        "wifi", "water_drop", "bolt", "local_gas_station",
                        "smoking_rooms", "local_drink", "coffee", "bedtime",
                        "alarm", "directions_run", "thermostat", "power",
                        "card_giftcard", "category", "star", "more_horiz"
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableIcons) { iconName ->
                            val isSelected = selectedIcon == iconName
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { selectedIcon = iconName },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CategoryIcons.getIcon(iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            if (name.isNotBlank()) {
                                onSave(name.trim(), selectedIcon, selectedColor)
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
