package com.allubie.nana.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.ui.viewmodel.RoutinesViewModel
import com.allubie.nana.ui.viewmodel.RoutinesViewModelFactory
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.NanaApplication
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.Month
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineStatsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as NanaApplication
    val viewModel: RoutinesViewModel = viewModel(
        factory = RoutinesViewModelFactory(application.routineRepository, context)
    )
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    
    // Map to display routines from the ViewModel
    val displayRoutines = uiState.routinesWithProgress.map { routineWithProgress ->
        RoutineDisplayData(
            id = routineWithProgress.routine.id,
            title = routineWithProgress.routine.title,
            description = routineWithProgress.routine.description,
            frequency = routineWithProgress.routine.frequency,
            completedToday = routineWithProgress.isCompletedToday,
            progress = routineWithProgress.progress,
            streak = routineWithProgress.streak,
            reminderTime = routineWithProgress.routine.reminderTime
        )
    }
    
    // Calculate statistics
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startOfWeek = today.minus(DatePeriod(days = today.dayOfWeek.isoDayNumber - 1))
    
    val totalRoutines = uiState.totalRoutines
    val completedToday = uiState.completedToday
    
    val dailyRoutines = displayRoutines.filter { it.frequency == "Daily" }
    val weeklyRoutines = displayRoutines.filter { it.frequency == "Weekly" }
    
    // Calculate completion rates
    val todayCompletions = dailyRoutines.count { it.completedToday }
    val dailyCompletionRate = if (dailyRoutines.isNotEmpty()) {
        (todayCompletions.toFloat() / dailyRoutines.size * 100).roundToInt()
    } else 0
    
    val weeklyCompletions = weeklyRoutines.count { it.completedToday }
    val weeklyCompletionRate = if (weeklyRoutines.isNotEmpty()) {
        (weeklyCompletions.toFloat() / weeklyRoutines.size * 100).roundToInt()
    } else 0
    
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Routine Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reset All Progress") },
                                onClick = {
                                    showOverflowMenu = false
                                    // Reset progress logic could go here
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(Spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.cardSpacing),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Daily and Weekly Progress Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.cardSpacing)
                ) {
                    // Daily Progress Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Daily Routines",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$todayCompletions/${dailyRoutines.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$dailyCompletionRate% Complete",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { dailyCompletionRate / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Weekly Progress Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Weekly Routines",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$weeklyCompletions/${weeklyRoutines.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "$weeklyCompletionRate% Complete",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { weeklyCompletionRate / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            // Habit Tracking Section
            item {
                Text(
                    text = "Active Routines",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                HabitTrackingCard(
                    routines = displayRoutines.filter { !it.completedToday }
                )
            }
            
            // Weekly Calendar View
            item {
                Text(
                    text = "This Week's Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                WeeklyCalendarCard(
                    startOfWeek = startOfWeek,
                    completedRoutines = displayRoutines.filter { it.completedToday }
                )
            }
            
            // Statistics Summary
            item {
                StatisticsSummaryCard(
                    routines = displayRoutines,
                    totalRoutines = totalRoutines,
                    completedToday = completedToday
                )
            }
        }
    }
}

// Data class to match reference pattern
data class RoutineDisplayData(
    val id: String,
    val title: String,
    val description: String,
    val frequency: String,
    val completedToday: Boolean = false,
    val progress: Float,
    val streak: Int,
    val reminderTime: String?
)

@Composable
fun HabitTrackingCard(
    routines: List<RoutineDisplayData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding)
        ) {
            if (routines.isEmpty()) {
                Text(
                    text = "All routines completed for today!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                routines.forEach { routine ->
                    HabitTrackingItem(
                        routine = routine
                    )
                    if (routine != routines.last()) {
                        Spacer(modifier = Modifier.height(Spacing.small))
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarCard(
    startOfWeek: LocalDate,
    completedRoutines: List<RoutineDisplayData>
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding)
        ) {
            Text(
                text = "Week of ${startOfWeek.month.name.take(3)} ${startOfWeek.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { dayIndex ->
                    val dayDate = startOfWeek.plus(DatePeriod(days = dayIndex))
                    val isToday = dayDate == today
                    val dayName = dayDate.dayOfWeek.name.take(3)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dayDate.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = if (isToday && completedRoutines.isNotEmpty()) 
                                       MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.surfaceVariant
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsSummaryCard(
    routines: List<RoutineDisplayData>,
    totalRoutines: Int,
    completedToday: Int
) {
    val averageProgress = if (routines.isNotEmpty()) routines.map { it.progress }.average().toFloat() else 0.0f
    val maxStreak = routines.maxOfOrNull { it.streak } ?: 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding)
        ) {
            Text(
                text = "Statistics Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            StatsStatisticItem("Total Routines", totalRoutines.toString())
            StatsStatisticItem("Completed Today", "$completedToday / $totalRoutines")
            StatsStatisticItem("Average Progress", "${(averageProgress * 100).toInt()}%")
            StatsStatisticItem("Best Streak", maxStreak.toString())
        }
    }
}

@Composable
fun StatsStatisticItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HabitTrackingItem(
    routine: RoutineDisplayData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${routine.frequency.lowercase().replaceFirstChar { it.uppercase() }} routine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Enhanced streak indicator with proper calculation
            if (routine.streak > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
                ) {
                    Text(
                        text = " ${routine.streak}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                // Removed checkbox; keep clean trailing space only when streak exists
                Spacer(modifier = Modifier.width(4.dp))
            }
            // Checkbox removed per request; trailing area intentionally left minimal
        }
    }
}
