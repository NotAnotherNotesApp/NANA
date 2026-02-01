package com.allubie.nana.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleViewerScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit,
    viewModel: ScheduleEditorViewModel = viewModel(factory = ScheduleEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val timeFormat = remember(use24HourFormat) {
        SimpleDateFormat(if (use24HourFormat) "HH:mm" else "hh:mm a", Locale.getDefault())
    }
    
    // Get category color
    val categoryColor = when (uiState.category.lowercase()) {
        "class", "lecture" -> ScheduleLecture
        "social" -> ScheduleSocial
        "lab" -> ScheduleLab
        "gym" -> ScheduleWork
        "exam" -> ScheduleWork
        "study" -> ScheduleStudy
        else -> ScheduleOther
    }
    
    // Parse recurrence rule for display
    fun parseRecurrenceDisplay(): Pair<String, String?>? {
        val rule = uiState.recurrenceRule ?: return null
        if (rule.isEmpty()) return null
        
        val frequency = when {
            rule.contains("FREQ=DAILY") -> "Daily"
            rule.contains("FREQ=WEEKLY") -> {
                val dayMatch = Regex("BYDAY=([A-Z,]+)").find(rule)
                val days = dayMatch?.groupValues?.get(1) ?: ""
                val dayNames = days.split(",").mapNotNull { day ->
                    when (day) {
                        "MO" -> "Monday"
                        "TU" -> "Tuesday"
                        "WE" -> "Wednesday"
                        "TH" -> "Thursday"
                        "FR" -> "Friday"
                        "SA" -> "Saturday"
                        "SU" -> "Sunday"
                        else -> null
                    }
                }
                if (dayNames.size == 1) {
                    "Weekly on ${dayNames.first()}s"
                } else {
                    "Weekly on ${dayNames.joinToString(", ")}"
                }
            }
            rule.contains("FREQ=MONTHLY") -> "Monthly"
            rule.contains("FREQ=YEARLY") -> "Yearly"
            else -> return null
        }
        
        val untilMatch = Regex("UNTIL=(\\d{8})").find(rule)
        val untilDate = untilMatch?.groupValues?.get(1)?.let { dateStr ->
            try {
                val year = dateStr.substring(0, 4).toInt()
                val month = dateStr.substring(4, 6).toInt() - 1
                val day = dateStr.substring(6, 8).toInt()
                val cal = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
            } catch (e: Exception) {
                null
            }
        }
        
        return Pair(frequency, untilDate?.let { "Until $it" })
    }
    
    // Parse reminder minutes for display
    fun parseReminderDisplay(): String? {
        if (uiState.reminderMinutes.isEmpty()) return null
        val minutes = uiState.reminderMinutes.firstOrNull() ?: return null
        return when {
            minutes == 0 -> "At time of event"
            minutes < 60 -> "$minutes minutes before"
            minutes == 60 -> "1 hour before"
            minutes < 1440 -> "${minutes / 60} hours before"
            minutes == 1440 -> "1 day before"
            else -> "${minutes / 1440} days before"
        }
    }
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditor) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit"
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
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 48.dp)
        ) {
            // Category badge and pin
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = uiState.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                // Show pin icon if event is pinned
                if (uiState.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Event title
            Text(
                text = uiState.title.ifEmpty { "Untitled Event" },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Time section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    val startTime = Date(uiState.startTime)
                    val endTime = uiState.endTime?.let { Date(it) }
                    
                    Text(
                        text = if (uiState.isAllDay) {
                            "All Day"
                        } else if (endTime != null) {
                            "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
                        } else {
                            timeFormat.format(startTime)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = dateFormat.format(startTime),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Location section (if available)
            if (uiState.location.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        // Split location by comma to get room and building
                        val locationParts = uiState.location.split(",").map { it.trim() }
                        
                        Text(
                            text = locationParts.firstOrNull() ?: uiState.location,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        if (locationParts.size > 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = locationParts.drop(1).joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Divider
            val recurrenceInfo = parseRecurrenceDisplay()
            val reminderText = parseReminderDisplay()
            
            if (recurrenceInfo != null || reminderText != null) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 68.dp, bottom = 24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            // Repeat section (if available)
            if (recurrenceInfo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = recurrenceInfo.first,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        if (recurrenceInfo.second != null) {
                            Text(
                                text = recurrenceInfo.second!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Reminder section (if available)
            if (reminderText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        text = reminderText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            // Divider before notes
            if (uiState.description.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 68.dp, bottom = 24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            // Notes section (if available)
            if (uiState.description.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = uiState.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
