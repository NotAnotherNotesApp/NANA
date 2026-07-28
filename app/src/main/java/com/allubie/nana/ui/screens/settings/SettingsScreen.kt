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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allubie.nana.BuildConfig
import com.allubie.nana.ui.components.NanaConfirmationDialog
import com.allubie.nana.ui.components.NanaSearchableListDialog
import com.allubie.nana.ui.components.NanaSelectionDialog
import com.allubie.nana.ui.components.SectionHeader
import com.allubie.nana.ui.components.SettingsCard
import com.allubie.nana.ui.components.SettingsItem
import androidx.compose.ui.res.stringResource
import com.allubie.nana.R
import com.allubie.nana.ui.components.SettingsItemWithSwitch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.allubie.nana.ui.theme.ThemeMode
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLabels: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val timezone by viewModel.timezone.collectAsStateWithLifecycle()
    val use24HourFormat by viewModel.use24HourFormat.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = BuildConfig.VERSION_NAME
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var currencySearch by remember { mutableStateOf("") }
    
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
    
    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf(
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.AMOLED to stringResource(R.string.theme_amoled)
        )
        NanaSelectionDialog(
            onDismiss = { showThemeDialog = false },
            title = stringResource(R.string.dialog_choose_theme),
            options = themes,
            selectedOption = themes.find { it.first == themeMode } ?: themes[0],
            optionLabel = { it.second },
            onSelect = { (mode, _) ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            }
        )
    }
    
    // Currency Selector Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            Triple("USD", "$", stringResource(R.string.currency_usd)),
            Triple("EUR", "€", stringResource(R.string.currency_eur)),
            Triple("GBP", "£", stringResource(R.string.currency_gbp)),
            Triple("JPY", "¥", stringResource(R.string.currency_jpy)),
            Triple("INR", "₹", stringResource(R.string.currency_inr)),
            Triple("BDT", "৳", stringResource(R.string.currency_bdt)),
            Triple("CNY", "¥", stringResource(R.string.currency_cny)),
            Triple("KRW", "₩", stringResource(R.string.currency_krw)),
            Triple("AUD", "A$", stringResource(R.string.currency_aud)),
            Triple("CAD", "C$", stringResource(R.string.currency_cad)),
            Triple("CHF", "Fr", stringResource(R.string.currency_chf)),
            Triple("SEK", "kr", stringResource(R.string.currency_sek)),
            Triple("NOK", "kr", stringResource(R.string.currency_nok)),
            Triple("DKK", "kr", stringResource(R.string.currency_dkk)),
            Triple("PLN", "zł", stringResource(R.string.currency_pln)),
            Triple("CZK", "Kč", stringResource(R.string.currency_czk)),
            Triple("HUF", "Ft", stringResource(R.string.currency_huf)),
            Triple("TRY", "₺", stringResource(R.string.currency_try)),
            Triple("RUB", "₽", stringResource(R.string.currency_rub)),
            Triple("BRL", "R$", stringResource(R.string.currency_brl)),
            Triple("MXN", "$", stringResource(R.string.currency_mxn)),
            Triple("ARS", "$", stringResource(R.string.currency_ars)),
            Triple("COP", "$", stringResource(R.string.currency_cop)),
            Triple("CLP", "$", stringResource(R.string.currency_clp)),
            Triple("ZAR", "R", stringResource(R.string.currency_zar)),
            Triple("NGN", "₦", stringResource(R.string.currency_ngn)),
            Triple("EGP", "E£", stringResource(R.string.currency_egp)),
            Triple("KES", "KSh", stringResource(R.string.currency_kes)),
            Triple("GHS", "₵", stringResource(R.string.currency_ghs)),
            Triple("AED", "د.إ", stringResource(R.string.currency_aed)),
            Triple("SAR", "﷼", stringResource(R.string.currency_sar)),
            Triple("QAR", "﷼", stringResource(R.string.currency_qar)),
            Triple("KWD", "د.ك", stringResource(R.string.currency_kwd)),
            Triple("THB", "฿", stringResource(R.string.currency_thb)),
            Triple("MYR", "RM", stringResource(R.string.currency_myr)),
            Triple("SGD", "S$", stringResource(R.string.currency_sgd)),
            Triple("IDR", "Rp", stringResource(R.string.currency_idr)),
            Triple("PHP", "₱", stringResource(R.string.currency_php)),
            Triple("VND", "₫", stringResource(R.string.currency_vnd)),
            Triple("PKR", "₨", stringResource(R.string.currency_pkr)),
            Triple("LKR", "₨", stringResource(R.string.currency_lkr)),
            Triple("TWD", "NT$", stringResource(R.string.currency_twd)),
            Triple("HKD", "HK$", stringResource(R.string.currency_hkd)),
            Triple("NZD", "NZ$", stringResource(R.string.currency_nzd))
        )
        val filteredCurrencies = if (currencySearch.isBlank()) currencies
            else currencies.filter {
                it.first.contains(currencySearch, ignoreCase = true) ||
                it.third.contains(currencySearch, ignoreCase = true)
            }

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false; currencySearch = "" },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = stringResource(R.string.dialog_choose_currency),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currencySearch,
                        onValueChange = { currencySearch = it },
                        placeholder = { Text(stringResource(R.string.hint_search_currencies)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredCurrencies.forEach { (code, symbol, name) ->
                            val isSelected = code == currencyCode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setCurrency(code, symbol)
                                        showCurrencyDialog = false
                                        currencySearch = ""
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) 
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
                                    else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = symbol,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$code • $symbol",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { showCurrencyDialog = false; currencySearch = "" },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
    
    // Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text(stringResource(R.string.dialog_open_source_licenses)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.dialog_msg_open_source),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
                SectionHeader(title = stringResource(R.string.section_general), isFirst = true)
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.AttachMoney,
                        title = stringResource(R.string.label_currency),
                        subtitle = "$currencyCode ($currencySymbol)",
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.AccessTime,
                        title = stringResource(R.string.label_24_hour_format),
                        subtitle = if (use24HourFormat) stringResource(R.string.template_using_24_hour) else stringResource(R.string.template_using_12_hour),
                        checked = use24HourFormat,
                        onCheckedChange = { viewModel.setUse24HourFormat(it) }
                    )
                }
            }
            
            // Appearance Section
            item {
                SectionHeader(title = stringResource(R.string.section_appearance))
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.label_theme),
                        subtitle = when (themeMode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                            ThemeMode.AMOLED -> stringResource(R.string.theme_amoled)
                        },
                        onClick = { showThemeDialog = true }
                    )
                }
            }
            
            // Labels & Categories Section
            item {
                SectionHeader(title = stringResource(R.string.section_labels_categories))
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Label,
                        title = stringResource(R.string.settings_manage_labels),
                        subtitle = stringResource(R.string.settings_manage_labels_desc),
                        onClick = onNavigateToLabels
                    )
                }
            }
            
            // Data Management Section
            item {
                SectionHeader(title = stringResource(R.string.section_data_management))
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Backup,
                        title = stringResource(R.string.label_backup_data),
                        subtitle = stringResource(R.string.settings_export_data),
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
                        title = stringResource(R.string.label_restore_data),
                        subtitle = stringResource(R.string.settings_import_backup),
                        onClick = { filePicker.launch(arrayOf("application/json")) }
                    )
                }
            }
            
            // About Section
            item {
                SectionHeader(title = stringResource(R.string.section_about))
            }
            
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.label_version),
                        subtitle = appVersion,
                        onClick = { }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Public,
                        title = stringResource(R.string.settings_about_app),
                        subtitle = stringResource(R.string.settings_about_app_desc),
                        onClick = { uriHandler.openUri("https://github.com/allubie/NANA") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.NewReleases,
                        title = stringResource(R.string.settings_latest_release),
                        subtitle = stringResource(R.string.settings_latest_release_desc),
                        onClick = { uriHandler.openUri("https://github.com/allubie/NANA/releases") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.BugReport,
                        title = stringResource(R.string.settings_report_bug),
                        subtitle = stringResource(R.string.settings_report_bug_desc),
                        onClick = {
                            com.allubie.nana.util.BugReportUtils.sendBugReportWithLogs(context, appVersion)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Description,
                        title = stringResource(R.string.label_licenses),
                        subtitle = stringResource(R.string.settings_open_source),
                        onClick = { showLicensesDialog = true }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}


