package com.allubie.nana.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.res.stringResource
import com.allubie.nana.R
import com.allubie.nana.data.model.RoutineType

data class RoutineCategory(
    val key: String,
    val name: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    routineId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: RoutineEditorViewModel = viewModel(factory = RoutineEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    
    val reminderTime = uiState.reminderTime
    val reminderEnabled = reminderTime != null
    val (selectedHour, selectedMinute, isAm) = remember(reminderTime) {
        if (reminderTime != null) {
            val parts = reminderTime.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
            val hour12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            Triple(hour12, m, h < 12)
        } else {
            Triple(7, 30, true)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collectLatest {
            onNavigateBack()
        }
    }
    
    val readingLabel = stringResource(R.string.cd_icon_reading)
    val fitnessLabel = stringResource(R.string.cd_icon_fitness)
    val runningLabel = stringResource(R.string.cd_icon_running)
    val meditationLabel = stringResource(R.string.cd_icon_meditation)
    val waterLabel = stringResource(R.string.cd_icon_water)
    val sleepLabel = stringResource(R.string.cd_icon_sleep)
    val studyLabel = stringResource(R.string.cd_icon_study)
    val codingLabel = stringResource(R.string.cd_icon_coding)
    val writingLabel = stringResource(R.string.cd_icon_writing)
    val musicLabel = stringResource(R.string.cd_icon_music)
    val artLabel = stringResource(R.string.cd_icon_art)
    val nutritionLabel = stringResource(R.string.cd_icon_nutrition)
    
    // Category mapping for prominent label picker
    val categories = remember(
        readingLabel, fitnessLabel, runningLabel, meditationLabel,
        waterLabel, sleepLabel, studyLabel, codingLabel,
        writingLabel, musicLabel, artLabel, nutritionLabel
    ) {
        listOf(
            RoutineCategory("reading", readingLabel, Icons.Outlined.AutoStories),
            RoutineCategory("fitness", fitnessLabel, Icons.Outlined.FitnessCenter),
            RoutineCategory("running", runningLabel, Icons.AutoMirrored.Outlined.DirectionsRun),
            RoutineCategory("meditation", meditationLabel, Icons.Outlined.SelfImprovement),
            RoutineCategory("water", waterLabel, Icons.Outlined.WaterDrop),
            RoutineCategory("sleep", sleepLabel, Icons.Outlined.Bedtime),
            RoutineCategory("study", studyLabel, Icons.Outlined.School),
            RoutineCategory("coding", codingLabel, Icons.Outlined.Code),
            RoutineCategory("writing", writingLabel, Icons.Outlined.Create),
            RoutineCategory("music", musicLabel, Icons.Outlined.MusicNote),
            RoutineCategory("art", artLabel, Icons.Outlined.Brush),
            RoutineCategory("nutrition", nutritionLabel, Icons.Outlined.Restaurant)
        )
    }
    
    // Get current icon from uiState.iconName
    val currentIcon = categories.find { it.key == uiState.iconName }?.icon 
        ?: Icons.Outlined.CheckCircle
    
    val timePickerState = rememberTimePickerState(
        initialHour = if (isAm) (if (selectedHour == 12) 0 else selectedHour) else (if (selectedHour == 12) 12 else selectedHour + 12),
        initialMinute = selectedMinute
    )

    // When opening time picker, set the TimePickerState to current reminder time
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            val parts = (uiState.reminderTime ?: "07:30").split(":")
            timePickerState.hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            timePickerState.minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
        }
    }
    
    LaunchedEffect(routineId) {
        if (routineId != null) {
            viewModel.loadRoutine(routineId)
        }
    }
    
    // Days: S M T W T F S (starting with Sunday = 0)
    val sunLabel = stringResource(R.string.day_initial_sun)
    val monLabel = stringResource(R.string.day_initial_mon)
    val tueLabel = stringResource(R.string.day_initial_tue)
    val wedLabel = stringResource(R.string.day_initial_wed)
    val thuLabel = stringResource(R.string.day_initial_thu)
    val friLabel = stringResource(R.string.day_initial_fri)
    val satLabel = stringResource(R.string.day_initial_sat)
    val daysOfWeek = remember(sunLabel, monLabel, tueLabel, wedLabel, thuLabel, friLabel, satLabel) {
        listOf(sunLabel, monLabel, tueLabel, wedLabel, thuLabel, friLabel, satLabel)
    }
    
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
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
            
            // Header Card (Routine Name, Category / Label picker)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Routine Name input
                    TextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.hint_routine_name),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    // Category / Label picker (Same style as Schedule editor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = uiState.iconName == category.key
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateIconName(category.key)
                                },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
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
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    viewModel.updateReminderTime("07:30")
                                } else {
                                    viewModel.updateReminderTime(null)
                                }
                            }
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
                                    modifier = Modifier.clickable {
                                        val h24 = if (selectedHour == 12) 0 else selectedHour
                                        viewModel.updateReminderTime(String.format("%02d:%02d", h24, selectedMinute))
                                    }
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
                                    modifier = Modifier.clickable {
                                        val h24 = if (selectedHour == 12) 12 else selectedHour + 12
                                        viewModel.updateReminderTime(String.format("%02d:%02d", h24, selectedMinute))
                                    }
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
                        val formatted = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
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
