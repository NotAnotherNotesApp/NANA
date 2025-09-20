package com.allubie.nana.ui.screens.routines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import com.allubie.nana.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    routineId: String? = null,
    routinesViewModel: com.allubie.nana.ui.viewmodel.RoutinesViewModel? = null,
    initialTitle: String = "",
    initialDescription: String = "",
    onSave: (title: String, description: String, frequency: String, reminderTime: String?) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedCategory by remember { mutableStateOf("Health") }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val categories = listOf("Health", "Work", "Personal", "Fitness", "Learning", "Social", "Creative")
    val frequencies = listOf("Daily", "Weekly", "Monthly", "Custom")
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    // Load existing routine data if editing
    LaunchedEffect(routineId) {
        if (routineId != null && routinesViewModel != null) {
            routinesViewModel.getRoutineById(routineId) { routine ->
                if (routine != null) {
                    title = routine.title
                    description = routine.description
                    selectedFrequency = routine.frequency
                    // Parse reminder time from string format "HH:mm"
                    routine.reminderTime?.let { timeString ->
                        try {
                            val parts = timeString.split(":")
                            if (parts.size == 2) {
                                reminderTime = LocalTime(parts[0].toInt(), parts[1].toInt())
                            }
                        } catch (e: Exception) {
                            // Handle parsing error gracefully
                            reminderTime = null
                        }
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (routineId == null) "New Routine" else "Edit Routine",
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isFormValid = title.isNotBlank() && 
                                    (selectedFrequency != "Weekly" || selectedDays.isNotEmpty())
                    
                    TextButton(
                        onClick = { 
                            if (isFormValid) {
                                val reminderTimeString = reminderTime?.let { time ->
                                    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
                                }
                                onSave(title, description, selectedFrequency, reminderTimeString)
                                onBack()
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(Spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.screenPadding)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Routine Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                isError = title.isEmpty(),
                supportingText = if (title.isEmpty()) {
                    { Text("Routine name is required", color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                minLines = 3
            )

            // Category Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.cardPadding)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.itemSpacing))
                    
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                            // Use new menuAnchor overload per Material3 API.
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                            categories.forEach { category ->
                                    DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Frequency Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.cardPadding)
                ) {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.itemSpacing))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing)
                    ) {
                        items(frequencies) { frequency ->
                            val isSelected = selectedFrequency == frequency
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFrequency = frequency
                                    if (frequency != "Weekly") {
                                        selectedDays = emptySet()
                                    }
                                },
                                label = { Text(frequency) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outline,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                }
            }

            // Day Selection (only for Weekly frequency)
            AnimatedVisibility(visible = selectedFrequency == "Weekly") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.cardPadding)
                    ) {
                        Text(
                            text = "Select Days",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (selectedFrequency == "Weekly" && selectedDays.isEmpty()) {
                            Text(
                                text = "Please select at least one day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.itemSpacing))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing)
                        ) {
                            items(daysOfWeek) { day ->
                                val isSelected = selectedDays.contains(day)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                    },
                                    label = { Text(day.take(3)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outline,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Reminder Time
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.cardPadding)
                ) {
                    Text(
                        text = "Reminder Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.itemSpacing))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = reminderTime?.let { time ->
                                "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
                            } ?: "No reminder set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(
                            onClick = { 
                                showTimePicker = true
                            }
                        ) {
                            Text(if (reminderTime == null) "Set Time" else "Change")
                        }
                        
                        if (reminderTime != null) {
                            IconButton(
                                onClick = { reminderTime = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear time"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                reminderTime = LocalTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialHour = reminderTime?.hour ?: 9,
            initialMinute = reminderTime?.minute ?: 0
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = { 
                onTimeSelected(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
