package com.allubie.nana.ui.screens.routines

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import com.allubie.nana.ui.theme.NanaIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.Routine
import com.allubie.nana.data.model.RoutineCompletion
import com.allubie.nana.data.model.RoutineType
import com.allubie.nana.util.DateUtils.isSameDay
import java.text.SimpleDateFormat
import java.util.*

// Routine icon mapping from iconName to ImageVector
private fun getRoutineIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "meditation", "self_improvement" -> Icons.Outlined.SelfImprovement
        "water", "water_drop" -> Icons.Outlined.WaterDrop
        "book", "auto_stories", "reading" -> Icons.AutoMirrored.Outlined.MenuBook
        "exercise", "fitness", "fitness_center" -> Icons.Outlined.FitnessCenter
        "run", "running", "directions_run" -> Icons.AutoMirrored.Outlined.DirectionsRun
        "coffee" -> Icons.Outlined.Coffee
        "sleep", "bedtime" -> Icons.Outlined.Bedtime
        "study", "school" -> Icons.Outlined.School
        "code", "coding" -> Icons.Outlined.Code
        "write", "create", "writing" -> Icons.Outlined.Create
        "music", "music_note" -> Icons.Outlined.MusicNote
        "art", "brush" -> Icons.Outlined.Brush
        "nutrition", "restaurant" -> Icons.Outlined.Restaurant
        else -> Icons.Outlined.CheckCircle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    onNavigateToEditor: (Long?) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: RoutinesViewModel = viewModel(factory = RoutinesViewModel.Factory)
) {
    val routines by viewModel.routines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val completedToday by viewModel.completedTodayIds.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dayNumberFormat = SimpleDateFormat("d", Locale.getDefault())
    val dateStringFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Helper to check if timer should show for current selected date
    val selectedDateString = remember(selectedDate) { dateStringFormat.format(selectedDate) }
    
    // Helper function to format time for display
    fun formatTimeForDisplay(reminderTime: String?): String? {
        if (reminderTime == null) return null
        return try {
            val parts = reminderTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (use24HourFormat) {
                String.format("%02d:%02d", hour, minute)
            } else {
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val amPm = if (hour < 12) "AM" else "PM"
                String.format("%d:%02d %s", displayHour, minute, amPm)
            }
        } catch (e: Exception) {
            reminderTime
        }
    }
    
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    
    val weekDays = remember(selectedDate) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        (0..6).map {
            val date = cal.time
            cal.add(Calendar.DAY_OF_MONTH, 1)
            date
        }
    }
    
    val completionRate = if (routines.isEmpty()) 0f else completedToday.size.toFloat() / routines.size
    var showMenu by remember { mutableStateOf(false) }
    
    // Helper function to parse reminder time to hour (24h format)
    fun getHourFromReminderTime(reminderTime: String?): Int? {
        if (reminderTime == null) return null
        return try {
            // Parse formats like "7:30 AM", "14:30", etc.
            val cleanTime = reminderTime.trim().uppercase()
            val isPm = cleanTime.contains("PM")
            val isAm = cleanTime.contains("AM")
            val timePart = cleanTime.replace("AM", "").replace("PM", "").trim()
            val parts = timePart.split(":")
            var hour = parts[0].toInt()
            
            if (isAm && hour == 12) hour = 0
            else if (isPm && hour != 12) hour += 12
            
            hour
        } catch (e: Exception) {
            null
        }
    }
    
    // Group routines by time of day: Morning (5-11), Afternoon (12-16), Evening (17-4)
    val morningRoutines = remember(routines) {
        routines.filter { routine ->
            val hour = getHourFromReminderTime(routine.reminderTime)
            hour == null || (hour in 5..11) // Default to morning if no time set
        }
    }
    val afternoonRoutines = remember(routines) {
        routines.filter { routine ->
            val hour = getHourFromReminderTime(routine.reminderTime)
            hour != null && hour in 12..16
        }
    }
    val eveningRoutines = remember(routines) {
        routines.filter { routine ->
            val hour = getHourFromReminderTime(routine.reminderTime)
            hour != null && (hour >= 17 || hour < 5)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Routines",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Today button
                    TextButton(onClick = { viewModel.selectDate(Date()) }) {
                        Text(
                            text = "Today",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Statistics") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToStatistics()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Analytics,
                                        contentDescription = null
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add Routine",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Week day selector
                item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    weekDays.forEach { date ->
                        val isSelected = isSameDay(date, selectedDate)
                        val isToday = isSameDay(date, Date())
                        DayChip(
                            dayName = dayFormat.format(date).take(3).uppercase(),
                            dayNumber = dayNumberFormat.format(date),
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }
            
            // Progress card
            item {
                ProgressCard(
                    completionRate = completionRate.coerceAtMost(1f),
                    completedCount = completedToday.size.coerceAtMost(routines.size),
                    totalCount = routines.size,
                    selectedDate = selectedDate,
                    onViewStats = onNavigateToStatistics
                )
            }
            
            // Morning section
            if (morningRoutines.isNotEmpty()) {
                item {
                    Text(
                        text = "MORNING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                items(morningRoutines, key = { "morning_${it.id}" }) { routine ->
                    RoutineCard(
                        routine = routine,
                        completion = todayCompletions[routine.id],
                        isCompleted = completedToday.contains(routine.id),
                        timerState = if (timerState.routineId == routine.id && 
                            (timerState.startedOnDate.isEmpty() || timerState.startedOnDate == selectedDateString)) timerState else null,
                        formattedReminderTime = formatTimeForDisplay(routine.reminderTime),
                        onToggle = { viewModel.toggleCompletion(routine) },
                        onIncrement = { viewModel.incrementCounter(routine) },
                        onDecrement = { viewModel.decrementCounter(routine) },
                        onStartTimer = { viewModel.startTimer(routine) },
                        onPauseTimer = { viewModel.pauseTimer() },
                        onResetTimer = { viewModel.resetTimer(routine) },
                        onClick = { onNavigateToEditor(routine.id) },
                        onTogglePin = { viewModel.togglePin(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }
            }
            
            // Afternoon section
            if (afternoonRoutines.isNotEmpty()) {
                item {
                    Text(
                        text = "AFTERNOON",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                items(afternoonRoutines, key = { "afternoon_${it.id}" }) { routine ->
                    RoutineCard(
                        routine = routine,
                        completion = todayCompletions[routine.id],
                        isCompleted = completedToday.contains(routine.id),
                        timerState = if (timerState.routineId == routine.id && 
                            (timerState.startedOnDate.isEmpty() || timerState.startedOnDate == selectedDateString)) timerState else null,
                        formattedReminderTime = formatTimeForDisplay(routine.reminderTime),
                        onToggle = { viewModel.toggleCompletion(routine) },
                        onIncrement = { viewModel.incrementCounter(routine) },
                        onDecrement = { viewModel.decrementCounter(routine) },
                        onStartTimer = { viewModel.startTimer(routine) },
                        onPauseTimer = { viewModel.pauseTimer() },
                        onResetTimer = { viewModel.resetTimer(routine) },
                        onClick = { onNavigateToEditor(routine.id) },
                        onTogglePin = { viewModel.togglePin(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }
            }
            
            // Evening section
            if (eveningRoutines.isNotEmpty()) {
                item {
                    Text(
                        text = "EVENING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                items(eveningRoutines, key = { "evening_${it.id}" }) { routine ->
                    RoutineCard(
                        routine = routine,
                        completion = todayCompletions[routine.id],
                        isCompleted = completedToday.contains(routine.id),
                        timerState = if (timerState.routineId == routine.id && 
                            (timerState.startedOnDate.isEmpty() || timerState.startedOnDate == selectedDateString)) timerState else null,
                        formattedReminderTime = formatTimeForDisplay(routine.reminderTime),
                        onToggle = { viewModel.toggleCompletion(routine) },
                        onIncrement = { viewModel.incrementCounter(routine) },
                        onDecrement = { viewModel.decrementCounter(routine) },
                        onStartTimer = { viewModel.startTimer(routine) },
                        onPauseTimer = { viewModel.pauseTimer() },
                        onResetTimer = { viewModel.resetTimer(routine) },
                        onClick = { onNavigateToEditor(routine.id) },
                        onTogglePin = { viewModel.togglePin(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }
            }
            
            // Empty state
            if (routines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SelfImprovement,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No routines yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to create your first habit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun DayChip(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    .background(MaterialTheme.colorScheme.primary)
                else if (isToday) Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                else Modifier.background(Color.Transparent)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayNumber,
            style = if (isSelected) MaterialTheme.typography.headlineMedium 
                    else MaterialTheme.typography.titleLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        } else if (isToday) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun ProgressCard(
    completionRate: Float,
    completedCount: Int,
    totalCount: Int,
    selectedDate: Date,
    onViewStats: () -> Unit
) {
    val isToday = isSameDay(selectedDate, Date())
    val dateLabel = if (isToday) {
        "Today's Progress"
    } else {
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate)
        "$dayName's Progress"
    }
    val habitsSuffix = if (isToday) "today" else "on this day"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Box {
            // Gradient overlay on right side
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append("You've completed ")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        Text(
                            text = "${(completionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " of your habits $habitsSuffix.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // View Stats button with arrow
                    Surface(
                        modifier = Modifier.clickable(onClick = onViewStats),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Stats",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Circular progress with star icon
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background track
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    // Progress with glow effect
                    CircularProgressIndicator(
                        progress = { completionRate },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineCard(
    routine: Routine,
    completion: RoutineCompletion?,
    isCompleted: Boolean,
    timerState: TimerState?,
    formattedReminderTime: String?,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Get icon from routine's iconName
    val icon = getRoutineIcon(routine.iconName)
    
    val routineType = try { RoutineType.valueOf(routine.routineType) } catch (e: Exception) { RoutineType.SIMPLE }
    val currentCount = completion?.currentCount ?: 0
    val elapsedSeconds = timerState?.elapsedSeconds ?: (completion?.elapsedSeconds ?: 0)
    val targetSeconds = routine.durationMinutes * 60
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete this routine?") },
            text = { Text("\"${routine.title}\" will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            color = if (routine.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                    else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isCompleted || routine.isPinned) 0.dp else 1.dp,
            border = if (routine.isPinned) 
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                else if (isCompleted) 
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin indicator
                if (routine.isPinned) {
                    Icon(
                        imageVector = NanaIcons.KeepFilled,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
                // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                when (routineType) {
                    RoutineType.COUNTER -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { currentCount.toFloat() / routine.targetCount.coerceAtLeast(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$currentCount/${routine.targetCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    RoutineType.TIMER -> {
                        val displayMinutes = elapsedSeconds / 60
                        val displaySeconds = elapsedSeconds % 60
                        val targetMinutes = targetSeconds / 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { elapsedSeconds.toFloat() / targetSeconds.coerceAtLeast(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = String.format("%d:%02d / %d:00", displayMinutes, displaySeconds, targetMinutes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    RoutineType.SIMPLE -> {
                        if (formattedReminderTime != null) {
                            Text(
                                text = formattedReminderTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Controls based on type
            when (routineType) {
                RoutineType.COUNTER -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onDecrement() },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onIncrement() },
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                RoutineType.TIMER -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Reset button
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onResetTimer() },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Play/Pause button
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { 
                                    if (timerState?.isRunning == true) onPauseTimer()
                                    else onStartTimer()
                                },
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (timerState?.isRunning == true) 
                                        Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (timerState?.isRunning == true) "Pause" else "Start",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                RoutineType.SIMPLE -> {
                    Switch(
                        checked = isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
    
        // Context menu dropdown
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 8.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(if (routine.isPinned) "Unpin Routine" else "Pin Routine") },
                onClick = {
                    showMenu = false
                    onTogglePin()
                },
                leadingIcon = {
                    Icon(
                        imageVector = NanaIcons.Keep,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null
                    )
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    showDeleteConfirmation = true
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}


