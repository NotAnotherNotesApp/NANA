package com.allubie.nana.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.border
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import com.allubie.nana.ui.viewmodel.NotesViewModel
import com.allubie.nana.ui.viewmodel.RoutinesViewModel
import com.allubie.nana.ui.viewmodel.SchedulesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.allubie.nana.data.backup.BackupRestoreManager
import com.allubie.nana.data.backup.AppBackupData

// AppBackupData moved to BackupRestoreManager.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    onBackPressed: () -> Unit,
    notesViewModel: NotesViewModel? = null,
    schedulesViewModel: SchedulesViewModel? = null,
    routinesViewModel: RoutinesViewModel? = null,
    expensesViewModel: ExpensesViewModel? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val currencies = listOf(
        "USD" to "USD ($)",
        "EUR" to "EUR (€)",
        "GBP" to "GBP (£)",
        "JPY" to "JPY (¥)",
        "CAD" to "CAD ($)",
        "AUD" to "AUD ($)",
        "CHF" to "CHF",
        "CNY" to "CNY (¥)",
        "INR" to "INR (₹)",
        "BDT" to "BDT (৳)"
    )
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    // File picker launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                BackupRestoreManager.exportData(context, it, appPreferences, notesViewModel, schedulesViewModel, routinesViewModel, expensesViewModel) { _, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                        isExporting = false
                    }
                }
            }
        } ?: run {
            isExporting = false
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                BackupRestoreManager.importData(context, it, appPreferences, notesViewModel, schedulesViewModel, routinesViewModel, expensesViewModel) { _, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                        isImporting = false
                    }
                }
            }
        } ?: run {
            isImporting = false
        }
    }
    
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = appPreferences.currency,
            currencies = currencies,
            onCurrencySelected = { currency ->
                appPreferences.updateCurrency(currency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { 
                Column {
                    Text("Export all your app data including notes, schedules, routines, and expenses to a backup file.")
                    if (isExporting) {
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text("Exporting...")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        isExporting = true
                        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                        exportLauncher.launch("nana_backup_$timestamp.json")
                        showExportDialog = false
                    },
                    enabled = !isExporting
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = { 
                Column {
                    Text("Select a backup file to restore your data. This will replace all current data with the data from the backup file.")
                    if (isImporting) {
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text("Importing...")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        isImporting = true
                        importLauncher.launch("application/json")
                        showImportDialog = false
                    },
                    enabled = !isImporting
                ) {
                    Text("Select File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all app data? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        // Clear all data logic would go here
                        showClearDataDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showReminderTimeDialog) {
        ReminderTimeSelectionDialog(
            currentMinutes = appPreferences.defaultReminderMinutes,
            onTimeSelected = { minutes ->
                appPreferences.updateDefaultReminderMinutes(minutes)
                showReminderTimeDialog = false
            },
            onDismiss = { showReminderTimeDialog = false }
        )
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(Spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSection(title = "Appearance") {
                    SettingsToggleItem(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = appPreferences.isDarkTheme,
                        onCheckedChange = { appPreferences.updateDarkTheme(it) }
                    )
                    
                    SettingsToggleItem(
                        title = "AMOLED Theme",
                        subtitle = "Monochrome with true blacks",
                        checked = appPreferences.isAmoledTheme,
                        onCheckedChange = { appPreferences.updateAmoledTheme(it) }
                    )
                }
            }
            
            item {
                SettingsSection(title = "Format") {
                    CurrencyDropdownItem(
                        title = "Currency",
                        selectedCurrency = currencies.find { it.first == appPreferences.currency }?.second ?: appPreferences.currency,
                        onClick = { showCurrencyDialog = true }
                    )
                    
                    TimeFormatRadioGroup(
                        selectedFormat = appPreferences.is24HourFormat,
                        onFormatSelected = { appPreferences.updateTimeFormat(it) }
                    )
                }
            }
            
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        title = "Enable Notifications",
                        subtitle = "Receive reminders and alerts",
                        checked = appPreferences.notificationsEnabled,
                        onCheckedChange = { appPreferences.updateNotifications(it) }
                    )
                    
                    SettingsToggleItem(
                        title = "Routine Reminders",
                        subtitle = "Get notified about routine activities",
                        checked = appPreferences.routineRemindersEnabled,
                        onCheckedChange = { appPreferences.updateRoutineReminders(it) }
                    )
                    
                    SettingsToggleItem(
                        title = "Schedule Reminders",
                        subtitle = "Get notified about upcoming schedules",
                        checked = appPreferences.scheduleRemindersEnabled,
                        onCheckedChange = { appPreferences.updateScheduleReminders(it) }
                    )
                    
                    SettingsClickableItem(
                        title = "Default Reminder Time",
                        subtitle = "${appPreferences.defaultReminderMinutes} minutes before"
                    ) {
                        showReminderTimeDialog = true
                    }
                }
            }
            
            item {
                SettingsSection(title = "Data & Backup") {
                    SettingsToggleItem(
                        title = "Auto Backup",
                        subtitle = "Automatically backup data daily",
                        checked = false, // You can add this to preferences later
                        onCheckedChange = { /* Add auto backup logic */ }
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Spacing.large,
                                vertical = Spacing.medium
                            ),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        SettingsActionButton(
                            text = "Export Data",
                            modifier = Modifier.weight(1f)
                        ) {
                            showExportDialog = true
                        }
                        
                        SettingsActionButton(
                            text = "Import Data",
                            modifier = Modifier.weight(1f)
                        ) {
                            showImportDialog = true
                        }
                    }
                }
            }
            
            item {
                SettingsSection(title = "About") {
                    var showLicenseDialog by remember { mutableStateOf(false) }
                    AboutClickableItem(
                        title = "About NANA",
                        subtitle = "Version 1.1b - Your Productivity Companion"
                    ) {
                        // Show about dialog with app info
                    }
                    
                    AboutClickableItem(
                        title = "Open Source License",
                        subtitle = "MIT License - View source code and contributions"
                    ) {
                        showLicenseDialog = true
                    }

                    if (showLicenseDialog) {
                        AlertDialog(
                            onDismissRequest = { showLicenseDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = { showLicenseDialog = false },
                                    modifier = Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text("Close")
                                }
                            },
                            title = { Text("MIT License") },
                            text = {
                                val licenseText = """
MIT License

Copyright (c) ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} NANA contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
""".trimIndent()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 420.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = licenseText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = Spacing.small)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(
                bottom = Spacing.medium,
                start = Spacing.medium,
                end = Spacing.medium
            )
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(Spacing.cornerMedium)
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.small)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CurrencyDropdownItem(
    title: String,
    selectedCurrency: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.medium
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        @OptIn(ExperimentalMaterial3Api::class)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.medium,
                        vertical = Spacing.medium
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCurrency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimeFormatRadioGroup(
    selectedFormat: Boolean,
    onFormatSelected: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.medium
            )
    ) {
        Text(
            text = "Time Format",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        TimeFormatRadioOption(
            text = "12-hour (AM/PM)",
            selected = !selectedFormat,
            onClick = { onFormatSelected(false) }
        )
        
        Spacer(modifier = Modifier.height(Spacing.small))
        
        TimeFormatRadioOption(
            text = "24-hour",
            selected = selectedFormat,
            onClick = { onFormatSelected(true) }
        )
    }
}

@Composable
fun TimeFormatRadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(Spacing.small))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                horizontal = Spacing.medium,
                vertical = Spacing.large
            )
        )
    }
}

@Composable
fun AboutClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    currencies: List<Pair<String, String>>,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            LazyColumn {
                items(currencies) { (code, display) ->
                    TextButton(
                        onClick = { onCurrencySelected(code) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = Spacing.medium,
                            vertical = Spacing.small
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(display)
                            if (code == currentCurrency) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReminderTimeSelectionDialog(
    currentMinutes: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val reminderOptions = listOf(
        5 to "5 minutes",
        10 to "10 minutes", 
        15 to "15 minutes",
        30 to "30 minutes",
        60 to "1 hour",
        120 to "2 hours",
        1440 to "1 day"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default Reminder Time") },
        text = {
            LazyColumn {
                items(reminderOptions) { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTimeSelected(minutes) }
                            .padding(
                                horizontal = Spacing.medium,
                                vertical = Spacing.medium
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label)
                        if (minutes == currentMinutes) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private suspend fun exportData(
    context: Context,
    uri: Uri,
    appPreferences: AppPreferences,
    notesViewModel: NotesViewModel?,
    schedulesViewModel: SchedulesViewModel?,
    routinesViewModel: RoutinesViewModel?,
    expensesViewModel: ExpensesViewModel?,
    onResult: (Boolean, String) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Collect data from all sources using first() to get current values
            val notesData = try {
                notesViewModel?.notes?.first()?.map { note ->
                    JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("category", note.category)
                        put("isPinned", note.isPinned)
                        put("isArchived", note.isArchived)
                        put("isDeleted", note.isDeleted)
                        put("createdAt", note.createdAt.toString())
                        put("updatedAt", note.updatedAt.toString())
                    }.toString()
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList<String>()
            }
            
            val schedulesData = try {
                schedulesViewModel?.allSchedules?.first()?.map { schedule ->
                    JSONObject().apply {
                        put("id", schedule.id)
                        put("title", schedule.title)
                        put("description", schedule.description)
                        put("date", schedule.date.toString())
                        put("startTime", schedule.startTime.toString())
                        put("endTime", schedule.endTime.toString())
                        put("category", schedule.category)
                        put("isCompleted", schedule.isCompleted)
                        put("reminderMinutes", schedule.reminderMinutes)
                        put("createdAt", schedule.createdAt)
                    }.toString()
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList<String>()
            }
            
            val routinesData = try {
                routinesViewModel?.routines?.first()?.map { routine ->
                    JSONObject().apply {
                        put("id", routine.id)
                        put("title", routine.title)
                        put("description", routine.description)
                        put("frequency", routine.frequency)
                        put("reminderTime", routine.reminderTime)
                        put("isActive", routine.isActive)
                        put("isPinned", routine.isPinned)
                        put("createdAt", routine.createdAt.toString())
                    }.toString()
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList<String>()
            }
            
            val expensesData = try {
                expensesViewModel?.allExpenses?.first()?.map { expense ->
                    JSONObject().apply {
                        put("id", expense.id)
                        put("title", expense.title)
                        put("amount", expense.amount)
                        put("category", expense.category)
                        put("date", expense.date.toString())
                        put("createdAt", expense.createdAt)
                    }.toString()
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList<String>()
            }
            
            val backupData = AppBackupData(
                exportDate = exportDate,
                notes = notesData,
                schedules = schedulesData,
                routines = routinesData,
                expenses = expensesData,
                preferences = mapOf(
                    "currency" to appPreferences.currency,
                    "darkTheme" to appPreferences.isDarkTheme.toString(),
                    "amoledTheme" to appPreferences.isAmoledTheme.toString(),
                    "notificationsEnabled" to appPreferences.notificationsEnabled.toString(),
                    "routineRemindersEnabled" to appPreferences.routineRemindersEnabled.toString(),
                    "scheduleRemindersEnabled" to appPreferences.scheduleRemindersEnabled.toString(),
                    "defaultReminderMinutes" to appPreferences.defaultReminderMinutes.toString(),
                    "is24HourFormat" to appPreferences.is24HourFormat.toString(),
                    "appVersion" to "1.0.0"
                )
            )
            
            val json = JSONObject().apply {
                put("exportDate", backupData.exportDate)
                put("notes", backupData.notes)
                put("schedules", backupData.schedules)
                put("routines", backupData.routines)
                put("expenses", backupData.expenses)
                put("preferences", JSONObject(backupData.preferences))
            }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toString(2).toByteArray())
                outputStream.flush()
            }
        }
        onResult(true, "Data exported successfully!")
    } catch (e: Exception) {
        onResult(false, "Failed to export data: ${e.message}")
    }
}

private suspend fun importData(
    context: Context,
    uri: Uri,
    appPreferences: AppPreferences,
    notesViewModel: NotesViewModel?,
    schedulesViewModel: SchedulesViewModel?,
    routinesViewModel: RoutinesViewModel?,
    expensesViewModel: ExpensesViewModel?,
    onResult: (Boolean, String) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw Exception("Failed to read file")
            
            val json = JSONObject(jsonString)
            val preferences = json.optJSONObject("preferences")
            
            // Restore preferences
            preferences?.optString("currency")?.let { 
                if (it.isNotEmpty()) appPreferences.updateCurrency(it) 
            }
            preferences?.optString("darkTheme")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateDarkTheme(value) }
            }
            preferences?.optString("amoledTheme")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateAmoledTheme(value) }
            }
            preferences?.optString("notificationsEnabled")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateNotifications(value) }
            }
            preferences?.optString("routineRemindersEnabled")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateRoutineReminders(value) }
            }
            preferences?.optString("scheduleRemindersEnabled")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateScheduleReminders(value) }
            }
            preferences?.optString("defaultReminderMinutes")?.let { 
                it.toIntOrNull()?.let { value -> appPreferences.updateDefaultReminderMinutes(value) }
            }
            preferences?.optString("is24HourFormat")?.let { 
                it.toBooleanStrictOrNull()?.let { value -> appPreferences.updateTimeFormat(value) }
            }
            
            // Restore data from backup
            // Restore notes data
            val notesArray = json.optJSONArray("notes")
            if (notesArray != null && notesViewModel != null) {
                for (i in 0 until notesArray.length()) {
                    try {
                        val noteJson = JSONObject(notesArray.getString(i))
                        val title = noteJson.optString("title", "")
                        val content = noteJson.optString("content", "")
                        val category = noteJson.optString("category", "")
                        
                        if (title.isNotEmpty()) {
                            notesViewModel.createNote(title, content, category)
                        }
                    } catch (e: Exception) {
                        // Skip invalid note entries
                    }
                }
            }
            
            // Restore schedules data
            val schedulesArray = json.optJSONArray("schedules")
            if (schedulesArray != null && schedulesViewModel != null) {
                for (i in 0 until schedulesArray.length()) {
                    try {
                        val scheduleJson = JSONObject(schedulesArray.getString(i))
                        val title = scheduleJson.optString("title", "")
                        @Suppress("UNUSED_VARIABLE")
                        val description = scheduleJson.optString("description", "")
                        
                        if (title.isNotEmpty()) {
                            // Parse date/time fields (expecting ISO strings) with safe fallbacks
                            val dateStr = scheduleJson.optString("date", "")
                            val startStr = scheduleJson.optString("startTime", "")
                            val endStr = scheduleJson.optString("endTime", "")
                            val category = scheduleJson.optString("category", "General")
                            val location = scheduleJson.optString("location", null)
                            val isRecurring = scheduleJson.optBoolean("isRecurring", false)
                            val recurringPattern = scheduleJson.optString("recurringPattern", null)
                            val reminderMinutes = scheduleJson.optInt("reminderMinutes", 15)

                            try {
                                val date = if (dateStr.isNotBlank()) {
                                    kotlinx.datetime.LocalDate.parse(dateStr)
                                } else {
                                    // Approximate current date via epoch day using system zone offset not available: fallback to today in UTC adjusted minimally
                                    val instant = kotlinx.datetime.Clock.System.now()
                                    val epochSeconds = instant.epochSeconds
                                    val epochDays = (epochSeconds / 86400L).toInt()
                                    kotlinx.datetime.LocalDate.fromEpochDays(epochDays)
                                }
                                val startTime = if (startStr.isNotBlank()) kotlinx.datetime.LocalTime.parse(startStr) else kotlinx.datetime.LocalTime(9,0)
                                // Fallback endTime = startTime + 1 hour (manual)
                                val endTime = if (endStr.isNotBlank()) {
                                    kotlinx.datetime.LocalTime.parse(endStr)
                                } else {
                                    val hour = (startTime.hour + 1).coerceAtMost(23)
                                    kotlinx.datetime.LocalTime(hour, startTime.minute)
                                }
                                schedulesViewModel.createSchedule(
                                    title = title,
                                    description = description,
                                    startTime = startTime,
                                    endTime = endTime,
                                    date = date,
                                    location = location,
                                    category = category.ifBlank { "General" },
                                    isRecurring = isRecurring,
                                    recurringPattern = recurringPattern,
                                    reminderMinutes = reminderMinutes
                                )
                            } catch (_: Exception) {
                                // Skip invalid schedule entry silently
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid schedule entries
                    }
                }
            }
            
            // Restore routines data
            val routinesArray = json.optJSONArray("routines")
            if (routinesArray != null && routinesViewModel != null) {
                for (i in 0 until routinesArray.length()) {
                    try {
                        val routineJson = JSONObject(routinesArray.getString(i))
                        val title = routineJson.optString("title", "")
                        val description = routineJson.optString("description", "")
                        val frequency = routineJson.optString("frequency", "Daily")
                        
                        if (title.isNotEmpty()) {
                            routinesViewModel.createRoutine(title, description, frequency, null)
                        }
                    } catch (e: Exception) {
                        // Skip invalid routine entries
                    }
                }
            }
            
            // Restore expenses data
            val expensesArray = json.optJSONArray("expenses")
            if (expensesArray != null && expensesViewModel != null) {
                for (i in 0 until expensesArray.length()) {
                    try {
                        val expenseJson = JSONObject(expensesArray.getString(i))
                        val title = expenseJson.optString("title", "")
                        val amount = expenseJson.optDouble("amount", 0.0)
                        @Suppress("UNUSED_VARIABLE")
                        val category = expenseJson.optString("category", "General")
                        
                        if (title.isNotEmpty() && amount > 0) {
                            expensesViewModel.createExpense(title, amount, category)
                        }
                    } catch (e: Exception) {
                        // Skip invalid expense entries
                    }
                }
            }
        }
        onResult(true, "Data imported successfully!")
    } catch (e: Exception) {
        onResult(false, "Failed to import data: ${e.message}")
    }
}
