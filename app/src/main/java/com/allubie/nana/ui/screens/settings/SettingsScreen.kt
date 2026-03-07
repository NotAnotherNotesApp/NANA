package com.allubie.nana.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.BuildConfig
import com.allubie.nana.ui.components.NanaConfirmationDialog
import com.allubie.nana.ui.components.NanaSearchableListDialog
import com.allubie.nana.ui.components.NanaSelectionDialog
import com.allubie.nana.ui.components.SectionHeader
import com.allubie.nana.ui.components.SettingsCard
import com.allubie.nana.ui.components.SettingsItem
import com.allubie.nana.ui.components.SettingsItemWithSwitch
import com.allubie.nana.ui.theme.ThemeMode
import java.util.TimeZone

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
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = BuildConfig.VERSION_NAME
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var currencySearch by remember { mutableStateOf("") }
    var timezoneSearch by remember { mutableStateOf("") }
    
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
        NanaSelectionDialog(
            onDismiss = { showThemeDialog = false },
            title = "Choose Theme",
            options = ThemeMode.entries.toList(),
            selectedOption = themeMode,
            optionLabel = { mode ->
                when (mode) {
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.AMOLED -> "AMOLED"
                    ThemeMode.SYSTEM -> "System default"
                }
            },
            onSelect = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
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
            Triple("CAD", "C$", "Canadian Dollar"),
            Triple("CHF", "Fr", "Swiss Franc"),
            Triple("SEK", "kr", "Swedish Krona"),
            Triple("NOK", "kr", "Norwegian Krone"),
            Triple("DKK", "kr", "Danish Krone"),
            Triple("PLN", "zł", "Polish Zloty"),
            Triple("CZK", "Kč", "Czech Koruna"),
            Triple("HUF", "Ft", "Hungarian Forint"),
            Triple("TRY", "₺", "Turkish Lira"),
            Triple("RUB", "₽", "Russian Ruble"),
            Triple("BRL", "R$", "Brazilian Real"),
            Triple("MXN", "$", "Mexican Peso"),
            Triple("ARS", "$", "Argentine Peso"),
            Triple("COP", "$", "Colombian Peso"),
            Triple("CLP", "$", "Chilean Peso"),
            Triple("ZAR", "R", "South African Rand"),
            Triple("NGN", "₦", "Nigerian Naira"),
            Triple("EGP", "E£", "Egyptian Pound"),
            Triple("KES", "KSh", "Kenyan Shilling"),
            Triple("GHS", "₵", "Ghanaian Cedi"),
            Triple("AED", "د.إ", "UAE Dirham"),
            Triple("SAR", "﷼", "Saudi Riyal"),
            Triple("QAR", "﷼", "Qatari Riyal"),
            Triple("KWD", "د.ك", "Kuwaiti Dinar"),
            Triple("THB", "฿", "Thai Baht"),
            Triple("MYR", "RM", "Malaysian Ringgit"),
            Triple("SGD", "S$", "Singapore Dollar"),
            Triple("IDR", "Rp", "Indonesian Rupiah"),
            Triple("PHP", "₱", "Philippine Peso"),
            Triple("VND", "₫", "Vietnamese Dong"),
            Triple("PKR", "₨", "Pakistani Rupee"),
            Triple("LKR", "₨", "Sri Lankan Rupee"),
            Triple("TWD", "NT$", "Taiwan Dollar"),
            Triple("HKD", "HK$", "Hong Kong Dollar"),
            Triple("NZD", "NZ$", "New Zealand Dollar")
        )
        val filteredCurrencies = if (currencySearch.isBlank()) currencies
            else currencies.filter {
                it.first.contains(currencySearch, ignoreCase = true) ||
                it.third.contains(currencySearch, ignoreCase = true)
            }

        NanaSearchableListDialog(
            onDismiss = { showCurrencyDialog = false; currencySearch = "" },
            title = "Choose Currency",
            searchQuery = currencySearch,
            onSearchQueryChange = { currencySearch = it },
            searchPlaceholder = "Search currencies...",
            items = filteredCurrencies,
            isSelected = { it.first == currencyCode },
            itemLabel = { (code, symbol, name) -> "$code ($symbol) - $name" },
            onSelect = { (code, symbol, _) ->
                viewModel.setCurrency(code, symbol)
                showCurrencyDialog = false
                currencySearch = ""
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
            "America/Anchorage" to "Alaska Time (US)",
            "Pacific/Honolulu" to "Hawaii Time (US)",
            "America/Toronto" to "Toronto (EST)",
            "America/Vancouver" to "Vancouver (PST)",
            "America/Mexico_City" to "Mexico City (CST)",
            "America/Sao_Paulo" to "São Paulo (BRT)",
            "America/Argentina/Buenos_Aires" to "Buenos Aires (ART)",
            "America/Bogota" to "Bogotá (COT)",
            "America/Santiago" to "Santiago (CLT)",
            "Europe/London" to "London (GMT)",
            "Europe/Paris" to "Paris (CET)",
            "Europe/Berlin" to "Berlin (CET)",
            "Europe/Madrid" to "Madrid (CET)",
            "Europe/Rome" to "Rome (CET)",
            "Europe/Amsterdam" to "Amsterdam (CET)",
            "Europe/Stockholm" to "Stockholm (CET)",
            "Europe/Warsaw" to "Warsaw (CET)",
            "Europe/Moscow" to "Moscow (MSK)",
            "Europe/Istanbul" to "Istanbul (TRT)",
            "Europe/Athens" to "Athens (EET)",
            "Africa/Cairo" to "Cairo (EET)",
            "Africa/Johannesburg" to "Johannesburg (SAST)",
            "Africa/Lagos" to "Lagos (WAT)",
            "Africa/Nairobi" to "Nairobi (EAT)",
            "Asia/Dubai" to "Dubai (GST)",
            "Asia/Riyadh" to "Riyadh (AST)",
            "Asia/Karachi" to "Karachi (PKT)",
            "Asia/Kolkata" to "India (IST)",
            "Asia/Dhaka" to "Dhaka (BST)",
            "Asia/Bangkok" to "Bangkok (ICT)",
            "Asia/Jakarta" to "Jakarta (WIB)",
            "Asia/Singapore" to "Singapore (SGT)",
            "Asia/Kuala_Lumpur" to "Kuala Lumpur (MYT)",
            "Asia/Manila" to "Manila (PHT)",
            "Asia/Ho_Chi_Minh" to "Ho Chi Minh (ICT)",
            "Asia/Shanghai" to "Shanghai (CST)",
            "Asia/Hong_Kong" to "Hong Kong (HKT)",
            "Asia/Taipei" to "Taipei (CST)",
            "Asia/Tokyo" to "Tokyo (JST)",
            "Asia/Seoul" to "Seoul (KST)",
            "Australia/Sydney" to "Sydney (AEST)",
            "Australia/Melbourne" to "Melbourne (AEST)",
            "Australia/Perth" to "Perth (AWST)",
            "Pacific/Auckland" to "Auckland (NZST)"
        )
        fun formatUtcOffset(timezoneId: String): String {
            val tz = TimeZone.getTimeZone(timezoneId)
            val offsetMs = tz.getOffset(System.currentTimeMillis())
            val hours = offsetMs / 3600000
            val minutes = Math.abs(offsetMs % 3600000) / 60000
            return if (minutes == 0) "UTC${if (hours >= 0) "+" else ""}$hours"
                   else "UTC${if (hours >= 0) "+" else ""}$hours:${String.format("%02d", minutes)}"
        }
        val filteredTimezones = if (timezoneSearch.isBlank()) commonTimezones
            else commonTimezones.filter {
                val offset = formatUtcOffset(it.first)
                it.first.contains(timezoneSearch, ignoreCase = true) ||
                it.second.contains(timezoneSearch, ignoreCase = true) ||
                offset.contains(timezoneSearch, ignoreCase = true)
            }

        NanaSearchableListDialog(
            onDismiss = { showTimezoneDialog = false; timezoneSearch = "" },
            title = "Select Timezone",
            searchQuery = timezoneSearch,
            onSearchQueryChange = { timezoneSearch = it },
            searchPlaceholder = "Search timezones...",
            items = filteredTimezones,
            isSelected = { it.first == timezone },
            itemLabel = { (id, name) -> "$name  ${formatUtcOffset(id)}" },
            onSelect = { (id, _) ->
                viewModel.setTimezone(id)
                showTimezoneDialog = false
                timezoneSearch = ""
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
                        "AndroidX Core KTX" to "Apache 2.0",
                        "AndroidX Lifecycle" to "Apache 2.0",
                        "AndroidX Navigation" to "Apache 2.0",
                        "AndroidX DataStore" to "Apache 2.0",
                        "AndroidX Glance" to "Apache 2.0",
                        "Vico Charts" to "Apache 2.0",
                        "RichEditor Compose" to "Apache 2.0",
                        "Coil Image Loading" to "Apache 2.0",
                        "Gson" to "Apache 2.0",
                        "Google Fonts" to "Apache 2.0"
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
        NanaConfirmationDialog(
            onDismiss = { showEmptyTrashDialog = false },
            onConfirm = {
                viewModel.emptyTrash()
                showEmptyTrashDialog = false
            },
            title = "Empty Trash",
            message = "This will permanently delete all notes in trash. This action cannot be undone.",
            confirmText = "Delete All",
            isDestructive = true
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
                SectionHeader(title = "GENERAL", isFirst = true)
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
                SectionHeader(title = "LABELS & CATEGORIES")
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
                SectionHeader(title = "APPEARANCE")
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
                SectionHeader(title = "DATA MANAGEMENT")
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
                SectionHeader(title = "ABOUT")
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        subtitle = appVersion,
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Person,
                        title = "About the app",
                        subtitle = "Visit the github page",
                        onClick = { uriHandler.openUri("https://github.com/allubie/NANA") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.NewReleases,
                        title = "Latest Release",
                        subtitle = "Check for updates",
                        onClick = { uriHandler.openUri("https://github.com/allubie/NANA/releases") }
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


