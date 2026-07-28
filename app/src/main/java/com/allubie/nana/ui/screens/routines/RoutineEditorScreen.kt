package com.allubie.nana.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.res.stringResource
import com.allubie.nana.R
import com.allubie.nana.data.model.RoutineType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    routineId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: RoutineEditorViewModel = viewModel(factory = RoutineEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reminderEnabled by remember { mutableStateOf(true) }
    var selectedHour by remember { mutableIntStateOf(7) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var isAm by remember { mutableStateOf(true) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collectLatest {
            onNavigateBack()
        }
    }
    
    // Icon mapping for display
    val iconOptions = listOf(
        "reading" to (Icons.Outlined.AutoStories to stringResource(R.string.cd_icon_reading)),
        "fitness" to (Icons.Outlined.FitnessCenter to stringResource(R.string.cd_icon_fitness)),
        "running" to (Icons.AutoMirrored.Outlined.DirectionsRun to stringResource(R.string.cd_icon_running)),
        "meditation" to (Icons.Outlined.SelfImprovement to stringResource(R.string.cd_icon_meditation)),
        "water" to (Icons.Outlined.WaterDrop to stringResource(R.string.cd_icon_water)),
        "sleep" to (Icons.Outlined.Bedtime to stringResource(R.string.cd_icon_sleep)),
        "study" to (Icons.Outlined.School to stringResource(R.string.cd_icon_study)),
        "coding" to (Icons.Outlined.Code to stringResource(R.string.cd_icon_coding)),
        "writing" to (Icons.Outlined.Create to stringResource(R.string.cd_icon_writing)),
        "music" to (Icons.Outlined.MusicNote to stringResource(R.string.cd_icon_music)),
        "art" to (Icons.Outlined.Brush to stringResource(R.string.cd_icon_art)),
        "nutrition" to (Icons.Outlined.Restaurant to stringResource(R.string.cd_icon_nutrition))
    )
    
    // Get current icon from uiState.iconName
    val currentIcon = iconOptions.find { it.first == uiState.iconName }?.second?.first 
        ?: Icons.Outlined.CheckCircle
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour + if (!isAm && selectedHour != 12) 12 else if (isAm && selectedHour == 12) 0 else 0,
        initialMinute = selectedMinute
    )
    
    // Initialize selected hour/minute from ViewModel when routine is loaded
    LaunchedEffect(uiState.reminderTime) {
        uiState.reminderTime?.let { timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 0
                val hour12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                selectedHour = if (hour12 == 0) 12 else hour12
                selectedMinute = m
                isAm = h < 12
            }
        }
    }

    // When opening time picker, set the TimePickerState to current reminder time
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            val timeStr = uiState.reminderTime
            if (timeStr != null) {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull() ?: 0
                    val m = parts[1].toIntOrNull() ?: 0
                    timePickerState.hour = h
                    timePickerState.minute = m
                }
            } else {
                // default to current selectedHour/Minute
                val h24 = if (isAm) (if (selectedHour == 12) 0 else selectedHour) else (if (selectedHour == 12) 12 else selectedHour + 12)
                timePickerState.hour = h24
                timePickerState.minute = selectedMinute
            }
        }
    }
    
    // Sync AM/PM toggle and time changes to ViewModel (fixes AM always being saved)
    LaunchedEffect(isAm, selectedHour, selectedMinute) {
        if (reminderEnabled) {
            val h24 = if (isAm) {
                if (selectedHour == 12) 0 else selectedHour
            } else {
                if (selectedHour == 12) 12 else selectedHour + 12
            }
            val formatted = String.format("%02d:%02d", h24, selectedMinute)
            viewModel.updateReminderTime(formatted)
        }
    }
    
    LaunchedEffect(routineId) {
        if (routineId != null) {
            viewModel.loadRoutine(routineId)
        }
    }
    
    // Show loading indicator when loading routine
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Days: S M T W T F S (starting with Sunday = 0)
    val daysOfWeek = listOf(
        stringResource(R.string.day_initial_sun),
        stringResource(R.string.day_initial_mon),
        stringResource(R.string.day_initial_tue),
        stringResource(R.string.day_initial_wed),
        stringResource(R.string.day_initial_thu),
        stringResource(R.string.day_initial_fri),
        stringResource(R.string.day_initial_sat)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (routineId == null) stringResource(R.string.title_new_routine) else stringResource(R.string.title_edit_routine),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_arrow_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveRoutine() }) {
                        Text(
                            text = stringResource(R.string.action_save),
                            fontWeight = FontWeight.Bold
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Routine Name Section with Icon Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon picker button
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showIconPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = currentIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Name input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.section_routine_name),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.hint_routine_name),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Routine Type Section
            Text(
                text = stringResource(R.string.label_routine_type),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Type selection chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoutineType.entries.forEach { type ->
                            val isSelected = uiState.routineType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateRoutineType(type) },
                                label = {
                                    Text(
                                        text = when (type) {
                                            RoutineType.SIMPLE -> stringResource(R.string.type_simple)
                                            RoutineType.COUNTER -> stringResource(R.string.type_counter)
                                            RoutineType.TIMER -> stringResource(R.string.type_timer)
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            RoutineType.SIMPLE -> Icons.Outlined.Check
                                            RoutineType.COUNTER -> Icons.Outlined.Add
                                            RoutineType.TIMER -> Icons.Outlined.Timer
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    // Type-specific settings
                    when (uiState.routineType) {
                        RoutineType.COUNTER -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = stringResource(R.string.label_target_count),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateTargetCount(uiState.targetCount - 1) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                ) {
                                    Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.cd_decrease))
                                }
                                Text(
                                    text = "${uiState.targetCount}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.updateTargetCount(uiState.targetCount + 1) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_increase))
                                }
                            }
                        }
                        RoutineType.TIMER -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = stringResource(R.string.label_duration_minutes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateDurationMinutes(uiState.durationMinutes - 1) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                ) {
                                    Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.cd_decrease))
                                }
                                Text(
                                    text = "${uiState.durationMinutes} ${stringResource(R.string.unit_minutes)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.updateDurationMinutes(uiState.durationMinutes + 1) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_increase))
                                }
                            }
                        }
                        else -> { }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Frequency Section
            Text(
                text = stringResource(R.string.label_frequency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Day toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            // Map: S=0 is Sunday, etc. Convert to our model (1=Mon...7=Sun)
                            val dayNumber = if (index == 0) 7 else index
                            val isSelected = uiState.selectedDays.contains(dayNumber)
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { viewModel.toggleDay(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Repeat row with presets dropdown
                    var showRepeatMenu by remember { mutableStateOf(false) }
                    
                    // Determine current selection label
                    val repeatLabel = when {
                        uiState.selectedDays == setOf(1, 2, 3, 4, 5, 6, 7) -> stringResource(R.string.repeat_every_day)
                        uiState.selectedDays == setOf(1, 2, 3, 4, 5) -> stringResource(R.string.repeat_weekdays)
                        uiState.selectedDays == setOf(6, 7) -> stringResource(R.string.repeat_weekends)
                        else -> stringResource(R.string.repeat_custom)
                    }
                    
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRepeatMenu = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_repeat),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = repeatLabel,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showRepeatMenu,
                            onDismissRequest = { showRepeatMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.repeat_every_day)) },
                                onClick = {
                                    listOf(1, 2, 3, 4, 5, 6, 7).forEach { day ->
                                        if (!uiState.selectedDays.contains(day)) viewModel.toggleDay(day)
                                    }
                                    showRepeatMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.repeat_weekdays)) },
                                onClick = {
                                    listOf(6, 7).forEach { if (uiState.selectedDays.contains(it)) viewModel.toggleDay(it) }
                                    listOf(1, 2, 3, 4, 5).forEach { if (!uiState.selectedDays.contains(it)) viewModel.toggleDay(it) }
                                    showRepeatMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.repeat_weekends)) },
                                onClick = {
                                    listOf(1, 2, 3, 4, 5).forEach { if (uiState.selectedDays.contains(it)) viewModel.toggleDay(it) }
                                    listOf(6, 7).forEach { if (!uiState.selectedDays.contains(it)) viewModel.toggleDay(it) }
                                    showRepeatMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.repeat_custom)) },
                                onClick = { showRepeatMenu = false }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Reminder Section
            Text(
                text = stringResource(R.string.label_reminder),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.label_enable_reminders),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.desc_enable_reminders),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Time picker visual
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Hour
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = String.format("%02d", selectedHour),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            
                            Text(
                                text = ":",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                            
                            // Minute
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = String.format("%02d", selectedMinute),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // AM/PM toggle
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isAm) MaterialTheme.colorScheme.surfaceVariant 
                                           else Color.Transparent,
                                    modifier = Modifier.clickable { isAm = true }
                                ) {
                                    Text(
                                        text = stringResource(R.string.time_am),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAm) MaterialTheme.colorScheme.onSurface 
                                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (!isAm) MaterialTheme.colorScheme.surfaceVariant 
                                           else Color.Transparent,
                                    modifier = Modifier.clickable { isAm = false }
                                ) {
                                    Text(
                                        text = stringResource(R.string.time_pm),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAm) MaterialTheme.colorScheme.onSurface 
                                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Motivation Section
            Text(
                text = stringResource(R.string.label_motivation),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                TextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.hint_description_optional),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    minLines = 3
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
    
    // Icon Picker Dialog
    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text(stringResource(R.string.dialog_choose_icon), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (iconName, iconPair) ->
                                val (icon, name) = iconPair
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.updateIconName(iconName)
                                            showIconPicker = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (uiState.iconName == iconName)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = name,
                                            tint = if (uiState.iconName == iconName)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            // Fill remaining slots if row is incomplete
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.dialog_select_reminder_time), fontWeight = FontWeight.Bold) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour24 = timePickerState.hour
                        selectedHour = if (hour24 == 0) 12 else if (hour24 > 12) hour24 - 12 else hour24
                        selectedMinute = timePickerState.minute
                        isAm = hour24 < 12
                        // Persist to ViewModel
                        val formatted = String.format("%02d:%02d", hour24, timePickerState.minute)
                        viewModel.updateReminderTime(formatted)
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
