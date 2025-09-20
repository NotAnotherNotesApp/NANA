package com.allubie.nana.ui.screens.schedules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.theme.Spacing
import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    scheduleId: String? = null,
    schedulesViewModel: com.allubie.nana.ui.viewmodel.SchedulesViewModel? = null,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: LocalDate = Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()),
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).time,
    onSave: (title: String, description: String, date: LocalDate, time: LocalTime, reminderMinutes: Int) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedStartTime by remember { mutableStateOf(initialTime) }
    var selectedEndTime by remember { mutableStateOf(initialTime.let { 
        LocalTime(
            hour = if (it.hour < 23) it.hour + 1 else 23,
            minute = it.minute
        )
    }) }
    var category by remember { mutableStateOf("General") }
    var location by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringType by remember { mutableStateOf("Weekly") }
    var reminderMinutes by remember { mutableStateOf(15) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showRecurringDropdown by remember { mutableStateOf(false) }
    
    // Load existing schedule data if editing
    LaunchedEffect(scheduleId) {
        if (scheduleId != null && schedulesViewModel != null) {
            schedulesViewModel.getScheduleById(scheduleId) { schedule ->
                if (schedule != null) {
                    title = schedule.title
                    description = schedule.description
                    selectedDate = schedule.date
                    selectedStartTime = schedule.startTime
                    selectedEndTime = schedule.endTime
                    category = schedule.category
                    location = schedule.location ?: ""
                    isRecurring = schedule.isRecurring
                    reminderMinutes = schedule.reminderMinutes
                }
            }
        }
    }
    var showReminderDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val categories = listOf("General", "Work", "Personal", "Health", "Education", "Social", "Family", "Travel")
    val recurringTypes = listOf("Daily", "Weekly", "Monthly", "Yearly")
    val reminderOptions = listOf(
        0 to "No reminder",
        5 to "5 minutes before",
        15 to "15 minutes before", 
        30 to "30 minutes before",
        60 to "1 hour before",
        1440 to "1 day before"
    )

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L
    )
    val startTimePickerState = rememberTimePickerState(
        initialHour = selectedStartTime.hour,
        initialMinute = selectedStartTime.minute
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = selectedEndTime.hour,
        initialMinute = selectedEndTime.minute
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (scheduleId == null) "New Schedule" else "Edit Schedule",
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            if (title.isNotBlank()) {
                                onSave(title, description, selectedDate, selectedStartTime, reminderMinutes)
                                onBack()
                            }
                        },
                        enabled = title.isNotBlank()
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
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                minLines = 3
            )

            // Category and Location
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                            // Pending: migrate to new menuAnchor API once constant name confirmed.
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth(),
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
                            categories.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            // Date and Time
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.cardPadding)
                ) {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.screenPadding))
                    
                    // Date Field
                    OutlinedTextField(
                        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                            Date(selectedDate.toEpochDays() * 24 * 60 * 60 * 1000L)
                        ),
                        onValueChange = {},
                        label = { Text("Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Start Time and End Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Time
                        OutlinedTextField(
                            value = "${selectedStartTime.hour.toString().padStart(2, '0')}:${selectedStartTime.minute.toString().padStart(2, '0')}",
                            onValueChange = {},
                            label = { Text("Start Time") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showStartTimePicker = true },
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select Start Time",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        // End Time
                        OutlinedTextField(
                            value = "${selectedEndTime.hour.toString().padStart(2, '0')}:${selectedEndTime.minute.toString().padStart(2, '0')}",
                            onValueChange = {},
                            label = { Text("End Time") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showEndTimePicker = true },
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select End Time",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Recurring Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurring",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recurring Event",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                    }
                    
                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = showRecurringDropdown,
                            onExpandedChange = { showRecurringDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = recurringType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Repeat") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRecurringDropdown) },
                                // Pending: migrate to new menuAnchor API once constant name confirmed.
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = showRecurringDropdown,
                                onDismissRequest = { showRecurringDropdown = false },
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                recurringTypes.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            recurringType = option
                                            showRecurringDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reminder Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = showReminderDropdown,
                        onExpandedChange = { showReminderDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = reminderOptions.find { it.first == reminderMinutes }?.second ?: "Custom",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reminder Time") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showReminderDropdown) },
                            // Pending: migrate to new menuAnchor API once constant name confirmed.
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showReminderDropdown,
                            onDismissRequest = { showReminderDropdown = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            reminderOptions.forEach { (minutes, text) ->
                                DropdownMenuItem(
                                    text = { Text(text) },
                                    onClick = {
                                        reminderMinutes = minutes
                                        showReminderDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let {
                    selectedDate = Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        .date
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                selectedStartTime = LocalTime(hour, minute)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            initialHour = selectedStartTime.hour,
            initialMinute = selectedStartTime.minute
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                selectedEndTime = LocalTime(hour, minute)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            initialHour = selectedEndTime.hour,
            initialMinute = selectedEndTime.minute
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
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
        title = { Text("Select Time") },
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
