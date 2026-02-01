package com.allubie.nana.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLabels: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val timezone by viewModel.timezone.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    LocalContext.current
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    
    // File picker for restore
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreData(it) }
    }
    
    // Permission launcher for backup
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.backupData()
        }
    }
    
    // Show snackbar for backup messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(backupState.message) {
        backupState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearBackupMessage()
        }
    }
    
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = when (mode) {
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                    ThemeMode.AMOLED -> "AMOLED"
                                    ThemeMode.SYSTEM -> "System default"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showCurrencyDialog) {
        val currencies = listOf(
            Triple("USD", "$", "US Dollar"),
            Triple("EUR", "€", "Euro"),
            Triple("GBP", "£", "British Pound"),
            Triple("JPY", "¥", "Japanese Yen"),
            Triple("INR", "₹", "Indian Rupee"),
            Triple("BDT", "৳", "Bangladeshi Taka"),
            Triple("CNY", "¥", "Chinese Yuan"),
            Triple("KRW", "₩", "Korean Won"),
            Triple("AUD", "A$", "Australian Dollar"),
            Triple("CAD", "C$", "Canadian Dollar")
        )
        
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Choose Currency") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    currencies.forEach { (code, symbol, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(code, symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currencyCode == code,
                                onClick = {
                                    viewModel.setCurrency(code, symbol)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$code ($symbol) - $name")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Timezone Dialog
    if (showTimezoneDialog) {
        val commonTimezones = listOf(
            "America/New_York" to "Eastern Time (US)",
            "America/Chicago" to "Central Time (US)",
            "America/Denver" to "Mountain Time (US)",
            "America/Los_Angeles" to "Pacific Time (US)",
            "Europe/London" to "London (GMT)",
            "Europe/Paris" to "Paris (CET)",
            "Asia/Tokyo" to "Tokyo (JST)",
            "Asia/Shanghai" to "Shanghai (CST)",
            "Asia/Singapore" to "Singapore (SGT)",
            "Asia/Dhaka" to "Dhaka (BST)",
            "Asia/Kolkata" to "India (IST)",
            "Australia/Sydney" to "Sydney (AEST)"
        )
        AlertDialog(
            onDismissRequest = { showTimezoneDialog = false },
            title = { Text("Select Timezone") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    commonTimezones.forEach { (id, name) ->
                        val isSelected = timezone == id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTimezone(id)
                                    showTimezoneDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setTimezone(id)
                                    showTimezoneDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimezoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "This app uses the following open source libraries:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    listOf(
                        "Jetpack Compose" to "Apache 2.0",
                        "Material Design 3" to "Apache 2.0",
                        "Room Database" to "Apache 2.0",
                        "Kotlin Coroutines" to "Apache 2.0",
                        "AndroidX Libraries" to "Apache 2.0"
                    ).forEach { (lib, license) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = lib, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = license,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash") },
            text = { Text("This will permanently delete all notes in trash. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // General Section
            item {
                Text(
                    text = "GENERAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.AttachMoney,
                        title = "Currency",
                        subtitle = "$currencyCode - $currencySymbol",
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Schedule,
                        title = "Timezone",
                        subtitle = java.util.TimeZone.getTimeZone(timezone).displayName,
                        onClick = { showTimezoneDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.AccessTime,
                        title = "24-hour format",
                        subtitle = if (use24HourFormat) "Using 24-hour time (14:00)" else "Using 12-hour time (2:00 PM)",
                        checked = use24HourFormat,
                        onCheckedChange = { viewModel.setUse24HourFormat(it) }
                    )
                }
            }
            
            // Labels & Categories Section
            item {
                Text(
                    text = "LABELS & CATEGORIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Label,
                        title = "Manage Labels",
                        subtitle = "Note labels, expense & income categories",
                        onClick = onNavigateToLabels
                    )
                }
            }
            
            // Appearance Section
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        subtitle = when (themeMode) {
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                            ThemeMode.AMOLED -> "AMOLED"
                            ThemeMode.SYSTEM -> "System default"
                        },
                        onClick = { showThemeDialog = true }
                    )
                }
            }
            
            // Data Management Section
            item {
                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Backup,
                        title = "Backup Data",
                        subtitle = "Export your data",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                viewModel.backupData()
                            } else {
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Restore,
                        title = "Restore Data",
                        subtitle = "Import from backup",
                        onClick = { filePicker.launch(arrayOf("application/json")) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Empty Trash",
                        subtitle = "Permanently delete trashed notes",
                        onClick = { showEmptyTrashDialog = true },
                        isDestructive = true
                    )
                }
            }
            
            // About Section
            item {
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        subtitle = "0.8.1",
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Person,
                        title = "Developer",
                        subtitle = "allubie",
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Description,
                        title = "Licenses",
                        subtitle = "Open source licenses",
                        onClick = { showLicensesDialog = true }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = if (isDestructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
}

@Composable
private fun SettingsItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    )
}
