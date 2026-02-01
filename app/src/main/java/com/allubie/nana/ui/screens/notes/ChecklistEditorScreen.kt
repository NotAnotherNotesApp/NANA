package com.allubie.nana.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.ChecklistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ChecklistEditorViewModel = viewModel(factory = ChecklistEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var newItemText by remember { mutableStateOf("") }
    LocalFocusManager.current
    val newItemFocusRequester = remember { FocusRequester() }
    
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            viewModel.loadChecklist(noteId)
        }
    }
    
    // Show loading indicator when loading checklist
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "Checklist Title",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveChecklist()
                            onNavigateBack()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            
            // Checklist Items + Input in single scrollable list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unchecked items first
                val uncheckedItems = uiState.items.filter { !it.isChecked }
                val checkedItems = uiState.items.filter { it.isChecked }
                
                itemsIndexed(
                    items = uncheckedItems,
                    key = { _, item -> "unchecked_${item.id}_${item.position}" }
                ) { _, item ->
                    ChecklistItemRow(
                        item = item,
                        onCheckedChange = { viewModel.toggleItemChecked(item) },
                        onTextChange = { newText -> viewModel.updateItemText(item, newText) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
                
                // New Item Input - inline with list items
                item(key = "new_item_input") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        ),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            BasicTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newItemText.isNotBlank()) {
                                            viewModel.addItem(newItemText)
                                            newItemText = ""
                                        }
                                    }
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        if (newItemText.isEmpty()) {
                                            Text(
                                                text = "Add a new item...",
                                                style = TextStyle(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(newItemFocusRequester)
                            )
                        }
                    }
                }
                
                // Checked items section
                if (checkedItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "${checkedItems.size} completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                    
                    itemsIndexed(
                        items = checkedItems,
                        key = { _, item -> "checked_${item.id}_${item.position}" }
                    ) { _, item ->
                        ChecklistItemRow(
                            item = item,
                            onCheckedChange = { viewModel.toggleItemChecked(item) },
                            onTextChange = { newText -> viewModel.updateItemText(item, newText) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var editingText by remember(item.id, item.position) { mutableStateOf(item.text) }
    val alpha by animateFloatAsState(
        targetValue = if (item.isChecked) 0.6f else 1f,
        label = "alpha"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Custom Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (item.isChecked) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (item.isChecked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onCheckedChange() },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = item.isChecked,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Text Input
            BasicTextField(
                value = editingText,
                onValueChange = { 
                    editingText = it
                    onTextChange(it)
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isChecked) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            
            // Delete button (shown on swipe or long press in future)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
