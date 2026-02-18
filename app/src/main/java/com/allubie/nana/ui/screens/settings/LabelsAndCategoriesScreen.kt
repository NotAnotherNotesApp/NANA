package com.allubie.nana.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.repository.LabelRepository
import com.allubie.nana.util.CategoryIcons
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ViewModel
class LabelsViewModel(private val repository: LabelRepository) : ViewModel() {
    
    val noteLabels: StateFlow<List<Label>> = repository.getLabelsByType(LabelType.NOTE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val eventLabels: StateFlow<List<Label>> = repository.getLabelsByType(LabelType.EVENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val expenseCategories: StateFlow<List<Label>> = repository.getLabelsByType(LabelType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val incomeCategories: StateFlow<List<Label>> = repository.getLabelsByType(LabelType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        viewModelScope.launch {
            repository.seedPresetsIfNeeded()
        }
    }
    
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
    onNavigateBack: () -> Unit
) {
    val viewModel: LabelsViewModel = viewModel(
        factory = LabelsViewModel.Factory(database)
    )
    
    val noteLabels by viewModel.noteLabels.collectAsState()
    val eventLabels by viewModel.eventLabels.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }
    var addingType by remember { mutableStateOf<LabelType?>(null) }
    
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
    
    // Add Label Dialog
    if (showAddDialog && addingType != null) {
        LabelEditorDialog(
            labelType = addingType!!,
            existingLabel = null,
            onDismiss = { 
                showAddDialog = false
                addingType = null
            },
            onSave = { name, iconName, color ->
                viewModel.createLabel(name, addingType!!, iconName, color)
                showAddDialog = false
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
            onDismiss = { editingLabel = null },
            onSave = { name, iconName, color ->
                viewModel.updateLabel(
                    editingLabel!!.copy(name = name, iconName = iconName, color = color)
                )
                editingLabel = null
            },
            onDelete = if (!editingLabel!!.isPreset) {
                { 
                    viewModel.deleteLabel(editingLabel!!)
                    editingLabel = null
                }
            } else null
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
                    tint = Color(label.color)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(label.color))
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
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String?, color: Int) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(existingLabel?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(existingLabel?.iconName) }
    // Use a default color based on label type
    val defaultColor = when (labelType) {
        LabelType.NOTE -> 0xFF3B82F6.toInt()
        LabelType.EXPENSE -> 0xFFF97316.toInt()
        LabelType.INCOME -> 0xFF22C55E.toInt()
        LabelType.EVENT -> 0xFF6366F1.toInt()
    }
    val selectedColor = existingLabel?.color ?: defaultColor
    
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
                    
                    val popularIcons = listOf(
                        "restaurant", "directions_bus", "shopping_bag", "movie",
                        "receipt_long", "favorite", "school", "work",
                        "home", "person", "event", "card_giftcard",
                        "payments", "wallet", "savings", "more_horiz"
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularIcons) { iconName ->
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
