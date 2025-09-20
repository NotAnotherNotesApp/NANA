package com.allubie.nana.ui.screens.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.viewmodel.SchedulesViewModel
import com.allubie.nana.ui.components.SwipeableItemCard
import com.allubie.nana.ui.theme.Spacing
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.Instant

data class ScheduleItem(
    val id: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val location: String? = null,
    val isPinned: Boolean = false,
    val isCompleted: Boolean = false,
    val category: String,
    val isRecurring: Boolean = false,
    val reminderMinutes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: SchedulesViewModel,
    onAddSchedule: () -> Unit = {},
    onEditSchedule: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    var showDatePicker by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
    )
    
    val displaySchedules = allSchedules.map { schedule: com.allubie.nana.data.entity.ScheduleEntity ->
        ScheduleItem(
            id = schedule.id,
            title = schedule.title,
            description = schedule.description,
            startTime = formatTime(schedule.startTime),
            endTime = formatTime(schedule.endTime),
            date = formatDate(schedule.date),
            location = schedule.location,
            isPinned = schedule.isPinned,
            isCompleted = schedule.isCompleted,
            category = schedule.category,
            isRecurring = schedule.isRecurring,
            reminderMinutes = schedule.reminderMinutes
        )
    }
    
    // Filter schedules by selected date
    val filteredSchedules = if (selectedDate != null) {
        allSchedules.filter { it.date == selectedDate }
    } else {
        allSchedules
    }.map { schedule ->
        ScheduleItem(
            id = schedule.id,
            title = schedule.title,
            description = schedule.description,
            startTime = formatTime(schedule.startTime),
            endTime = formatTime(schedule.endTime),
            date = formatDate(schedule.date),
            location = schedule.location,
            isPinned = schedule.isPinned,
            isCompleted = schedule.isCompleted,
            category = schedule.category,
            isRecurring = schedule.isRecurring,
            reminderMinutes = schedule.reminderMinutes
        )
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
                        selectedDate = if (selectedDate == picked) null else picked
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todaySchedules = filteredSchedules.filter { schedule ->
        allSchedules.find { it.id == schedule.id }?.date == today
    }
    val upcomingSchedules = filteredSchedules.filter { schedule ->
        allSchedules.find { it.id == schedule.id }?.date != today
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Schedules") },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        val menuShape = RoundedCornerShape(12.dp)
                        androidx.compose.material3.DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = menuShape,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .clip(menuShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), menuShape)
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showOverflowMenu = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = tween(180, easing = LinearOutSlowInEasing), initialScale = 0.9f) +
                        fadeIn(animationSpec = tween(160, easing = LinearOutSlowInEasing)),
                exit = scaleOut(animationSpec = tween(140, easing = FastOutLinearInEasing)) +
                       fadeOut(animationSpec = tween(120, easing = FastOutLinearInEasing))
            ) {
                FloatingActionButton(
                    onClick = onAddSchedule,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                }
            }
        }
    ) { paddingValues ->
        if (displaySchedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "No schedules",
                        modifier = Modifier.size(Spacing.iconMassive),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.screenPadding))
                    Text(
                        text = "No schedules yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to create your first schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardSpacing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    TodayScheduleSummary(todaySchedules = todaySchedules)
                }
                
                if (todaySchedules.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Today's Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(todaySchedules) { schedule ->
                        val originalSchedule = allSchedules.find { it.id == schedule.id }
                        SwipeableItemCard(
                            isPinned = schedule.isPinned,
                            showArchive = false,
                            onPin = { viewModel.togglePin(schedule.id, schedule.isPinned) },
                            onArchive = { /* No archive for schedules */ },
                            onDelete = { originalSchedule?.let { viewModel.deleteSchedule(it) } }
                        ) {
                            ScheduleCard(
                                schedule = schedule,
                                onComplete = { viewModel.toggleCompletion(schedule.id) },
                                onClick = { onEditSchedule(schedule.id) }
                            )
                        }
                    }
                }
                
                if (upcomingSchedules.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Upcoming",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(upcomingSchedules) { schedule ->
                        val originalSchedule = allSchedules.find { it.id == schedule.id }
                        SwipeableItemCard(
                            isPinned = schedule.isPinned,
                            showArchive = false,
                            onPin = { viewModel.togglePin(schedule.id, schedule.isPinned) },
                            onArchive = { /* No archive for schedules */ },
                            onDelete = { originalSchedule?.let { viewModel.deleteSchedule(it) } }
                        ) {
                            ScheduleCard(
                                schedule = schedule,
                                onComplete = { viewModel.toggleCompletion(schedule.id) },
                                onClick = { onEditSchedule(schedule.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayScheduleSummary(todaySchedules: List<ScheduleItem>) {
    val completedToday = todaySchedules.count { it.isCompleted }
    val totalToday = todaySchedules.size
    val nextSchedule = todaySchedules.firstOrNull { !it.isCompleted }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(Spacing.cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$totalToday events planned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(Spacing.iconSizeLarge)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$completedToday/$totalToday",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            nextSchedule?.let { schedule ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.cardSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(Spacing.cardSpacing))
                        Column {
                            Text(
                                text = "Next: ${schedule.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${schedule.startTime} - ${schedule.endTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleItem,
    onComplete: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var isCompleted by remember { mutableStateOf(schedule.isCompleted) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { 
                            isCompleted = !isCompleted
                            onComplete()
                        },
                        modifier = Modifier.size(Spacing.iconSize)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isCompleted) "Completed" else "Not completed",
                            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = schedule.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (schedule.isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    modifier = Modifier.size(Spacing.iconSmall),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (!schedule.isCompleted && schedule.date != "Past" && schedule.reminderMinutes > 0) {
                                if (schedule.isPinned) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Has notification reminder",
                                    modifier = Modifier.size(Spacing.iconSmall),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        if (schedule.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${schedule.startTime} - ${schedule.endTime}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            schedule.location?.let { location ->
                                if (location.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = schedule.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    if (schedule.isRecurring) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "R",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(time: LocalTime): String {
    val hour = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
    val period = if (time.hour < 12) "AM" else "PM"
    val minute = if (time.minute < 10) "0${time.minute}" else time.minute.toString()
    return "$hour:$minute $period"
}

private fun formatDate(date: LocalDate): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when {
        date == now -> "Today"
        date.toEpochDays() == now.toEpochDays() + 1 -> "Tomorrow"
        date.toEpochDays() == now.toEpochDays() - 1 -> "Yesterday"
        else -> {
            val daysDiff = date.toEpochDays() - now.toEpochDays()
            when {
                daysDiff > 0 && daysDiff < 7 -> "In $daysDiff day${if (daysDiff > 1) "s" else ""}"
                daysDiff < 0 && daysDiff > -7 -> "${-daysDiff} day${if (-daysDiff > 1) "s" else ""} ago"
                else -> "${date.monthNumber}/${date.dayOfMonth}/${date.year}"
            }
        }
    }
}

// Removed custom calendar; using Material3 DatePicker instead.
