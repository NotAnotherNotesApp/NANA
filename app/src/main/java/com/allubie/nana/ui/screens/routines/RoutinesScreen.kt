package com.allubie.nana.ui.screens.routines

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allubie.nana.data.entity.RoutineEntity
import com.allubie.nana.ui.viewmodel.RoutinesViewModel
import com.allubie.nana.ui.viewmodel.RoutinesUiState
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.ui.components.SwipeableItemCard

data class Routine(
    val id: String,
    val title: String,
    val description: String,
    val frequency: String, // "Daily", "Weekly", etc.
    val completedToday: Boolean = false,
    val isPinned: Boolean = false,
    val progress: Float, // 0.0 to 1.0
    val streak: Int,
    val reminderTime: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel,
    onAddRoutine: () -> Unit = {},
    onEditRoutine: (String) -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    
    // Use actual data from ViewModel instead of mock data
    val displayRoutines = uiState.routinesWithProgress.map { routineWithProgress ->
        Routine(
            id = routineWithProgress.routine.id,
            title = routineWithProgress.routine.title,
            description = routineWithProgress.routine.description,
            frequency = routineWithProgress.routine.frequency,
            completedToday = routineWithProgress.isCompletedToday,
            isPinned = routineWithProgress.routine.isPinned,
            progress = routineWithProgress.progress,
            streak = routineWithProgress.streak,
            reminderTime = routineWithProgress.routine.reminderTime
        )
    }
    
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }

    if (showStatisticsDialog) {
        RoutineStatisticsDialog(
            uiState = uiState,
            routines = displayRoutines,
            onDismiss = { showStatisticsDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Routines") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.Analytics, contentDescription = "Statistics")
                    }
                    
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        val menuShape = RoundedCornerShape(12.dp)
                        DropdownMenu(
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
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showOverflowMenu = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
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
                    onClick = onAddRoutine,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Routine")
                }
            }
        }
    ) { paddingValues ->
        
        if (displayRoutines.isEmpty()) {
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "No routines",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Text(
                        text = "No routines yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "Tap the + button to create your first routine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    ProgressSummaryCard(
                        completedToday = uiState.completedToday,
                        totalRoutines = uiState.totalRoutines,
                        overallProgress = uiState.overallProgress
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "Your Routines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                }
                
                items(displayRoutines) { routine ->
                    SwipeableItemCard(
                        isPinned = routine.isPinned,
                        showArchive = false,
                        onPin = { 
                            viewModel.togglePin(routine.id, routine.isPinned)
                        },
                        onArchive = { /* No archive for routines */ },
                        onDelete = {
                            viewModel.getRoutineById(routine.id) { routineEntity ->
                                routineEntity?.let { viewModel.deleteRoutine(it) }
                            }
                        }
                    ) {
                        RoutineCard(
                            routine = routine,
                            onToggleComplete = { 
                                viewModel.toggleCompletion(routine.id)
                            },
                            onClick = { onEditRoutine(routine.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(
    completedToday: Int,
    totalRoutines: Int,
    overallProgress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$completedToday of $totalRoutines completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(overallProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun RoutineCard(
    routine: Routine,
    onToggleComplete: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    
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
            containerColor = if (routine.completedToday) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { 
                            onToggleComplete()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (routine.completedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (routine.completedToday) "Completed" else "Not completed",
                            tint = if (routine.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = routine.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (routine.isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    modifier = Modifier.size(Spacing.iconSmall),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (!routine.reminderTime.isNullOrEmpty()) {
                                if (routine.isPinned) {
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
                        
                        if (routine.description.isNotBlank()) {
                            Text(
                                text = routine.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = routine.frequency,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!routine.completedToday && routine.streak > 0) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "${routine.streak} day streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
            
            if (routine.progress > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(routine.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { routine.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun RoutineStatisticsDialog(
    uiState: RoutinesUiState,
    routines: List<Routine>,
    onDismiss: () -> Unit
) {
    val totalRoutines = uiState.totalRoutines
    val completedToday = uiState.completedToday
    val averageProgress = if (routines.isNotEmpty()) routines.map { it.progress }.average().toFloat() else 0.0f
    val totalStreak = routines.maxOfOrNull { it.streak } ?: 0
    val pinnedRoutines = routines.count { it.isPinned }
    val activeRoutines = totalRoutines // All routines are considered active for now
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Routine Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                StatisticItem("Total Routines", totalRoutines.toString())
                StatisticItem("Active Routines", activeRoutines.toString())
                StatisticItem("Completed Today", "$completedToday / $totalRoutines")
                StatisticItem("Average Progress", "${(averageProgress * 100).toInt()}%")
                StatisticItem("Best Streak", totalStreak.toString())
                StatisticItem("Pinned Routines", pinnedRoutines.toString())
                
                if (routines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Keep up the great work!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(Spacing.medium)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun StatisticItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
