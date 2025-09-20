package com.allubie.nana.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.NanaApplication
import com.allubie.nana.ui.screens.expenses.ExpensesScreen
import com.allubie.nana.ui.screens.expenses.BudgetManagerScreen
import com.allubie.nana.ui.screens.expenses.CategoriesManagerScreen
import com.allubie.nana.ui.screens.expenses.CategoriesBreakdownScreen
import com.allubie.nana.ui.screens.notes.NotesScreen
import com.allubie.nana.ui.screens.notes.NoteEditorScreen
import com.allubie.nana.ui.screens.notes.NoteViewerScreen
import com.allubie.nana.ui.screens.notes.ArchivedNotesScreen
import com.allubie.nana.ui.screens.notes.RecycleBinScreen
import com.allubie.nana.ui.screens.routines.RoutinesScreen
import com.allubie.nana.ui.screens.routines.RoutineEditorScreen
import com.allubie.nana.ui.screens.routines.RoutineStatsScreen
import com.allubie.nana.ui.screens.schedules.SchedulesScreen
import com.allubie.nana.ui.screens.schedules.ScheduleEditorScreen
import com.allubie.nana.ui.screens.expenses.ExpenseEditorScreen
import com.allubie.nana.ui.screens.settings.SettingsScreen
import com.allubie.nana.ui.viewmodel.*
import com.allubie.nana.data.preferences.AppPreferences
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

// Screen types for animation control
enum class ScreenType {
    MAIN_TAB, SETTINGS, EDITOR, VIEWER, MANAGER, STATS
}

// Animation constants
private const val ANIMATION_DURATION = 300
private const val FADE_DURATION = 200

// Standard Android transition animations with fade and subtle scale
private fun standardEnterTransition() = fadeIn(
    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
) + scaleIn(
    initialScale = 0.95f,
    animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
)

private fun standardExitTransition() = fadeOut(
    animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing)
) + scaleOut(
    targetScale = 0.95f,
    animationSpec = tween(FADE_DURATION, easing = FastOutSlowInEasing)
)

private fun quickFadeTransition() = fadeIn(
    animationSpec = tween(FADE_DURATION, easing = LinearOutSlowInEasing)
) togetherWith fadeOut(
    animationSpec = tween(FADE_DURATION, easing = FastOutLinearInEasing)
)

@Composable
fun MainNavigation(appPreferences: AppPreferences) {
    val context = LocalContext.current
    val application = context.applicationContext as NanaApplication
    
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showArchivedNotes by remember { mutableStateOf(false) }
    var showRecycleBin by remember { mutableStateOf(false) }
    var showNoteViewer by remember { mutableStateOf(false) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var showScheduleEditor by remember { mutableStateOf(false) }
    var showExpenseEditor by remember { mutableStateOf(false) }
    var showRoutineEditor by remember { mutableStateOf(false) }
    var showRoutineStats by remember { mutableStateOf(false) }
    var showBudgetManager by remember { mutableStateOf(false) }
    var showCategoriesManager by remember { mutableStateOf(false) }
    var showCategoriesBreakdown by remember { mutableStateOf(false) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var editingNoteType by remember { mutableStateOf("text") }
    var viewingNoteId by remember { mutableStateOf<String?>(null) }
    var editingScheduleId by remember { mutableStateOf<String?>(null) }
    var editingExpenseId by remember { mutableStateOf<String?>(null) }
    var editingRoutineId by remember { mutableStateOf<String?>(null) }

    // ViewModels
    val expensesViewModel: ExpensesViewModel = viewModel(
        factory = ExpensesViewModelFactory(application.expenseRepository)
    )

    val navItems = listOf(
        BottomNavItem(
            title = "Notes",
            selectedIcon = Icons.AutoMirrored.Filled.Notes,
            unselectedIcon = Icons.AutoMirrored.Outlined.Notes,
            route = "notes"
        ),
        BottomNavItem(
            title = "Routines",
            selectedIcon = Icons.Filled.Schedule,
            unselectedIcon = Icons.Outlined.Schedule,
            route = "routines"
        ),
        BottomNavItem(
            title = "Schedules",
            selectedIcon = Icons.Filled.Event,
            unselectedIcon = Icons.Outlined.Event,
            route = "schedules"
        ),
        BottomNavItem(
            title = "Expenses",
            selectedIcon = Icons.Filled.AccountBalanceWallet,
            unselectedIcon = Icons.Outlined.AccountBalanceWallet,
            route = "expenses"
        )
    )

    // Determine current screen type for animation
    val currentScreenType = when {
        showSettings -> ScreenType.SETTINGS
        showNoteEditor || showScheduleEditor || showExpenseEditor || showRoutineEditor -> ScreenType.EDITOR
        showNoteViewer -> ScreenType.VIEWER
    showBudgetManager || showCategoriesManager || showCategoriesBreakdown -> ScreenType.MANAGER
        showRoutineStats -> ScreenType.STATS
        showArchivedNotes || showRecycleBin -> ScreenType.VIEWER
        else -> ScreenType.MAIN_TAB
    }

    AnimatedContent(
        targetState = currentScreenType,
        transitionSpec = {
            when {
                // Settings use standard enter/exit with scale
                targetState == ScreenType.SETTINGS -> standardEnterTransition() togetherWith standardExitTransition()
                initialState == ScreenType.SETTINGS -> standardEnterTransition() togetherWith standardExitTransition()
                // Editors use standard enter/exit with scale
                targetState == ScreenType.EDITOR -> standardEnterTransition() togetherWith standardExitTransition()
                initialState == ScreenType.EDITOR -> standardEnterTransition() togetherWith standardExitTransition()
                // Viewers use quick fade
                targetState == ScreenType.VIEWER || initialState == ScreenType.VIEWER -> quickFadeTransition()
                // Managers use standard enter/exit with scale
                targetState == ScreenType.MANAGER -> standardEnterTransition() togetherWith standardExitTransition()
                initialState == ScreenType.MANAGER -> standardEnterTransition() togetherWith standardExitTransition()
                // Stats use standard enter/exit with scale
                targetState == ScreenType.STATS -> standardEnterTransition() togetherWith standardExitTransition()
                initialState == ScreenType.STATS -> standardEnterTransition() togetherWith standardExitTransition()
                // Default quick fade for tab switches
                else -> quickFadeTransition()
            }
        },
        label = "screen_transition"
    ) { screenType ->
        when (screenType) {
            ScreenType.MAIN_TAB -> {
                MainTabContent(
                    selectedIndex = selectedIndex,
                    navItems = navItems,
                    onTabSelected = { selectedIndex = it },
                    application = application,
                    context = context,
                    expensesViewModel = expensesViewModel,
                    appPreferences = appPreferences,
                    onSettingsClick = { showSettings = true },
                    onNoteClick = { noteId ->
                        if (noteId == "-1") {
                            editingNoteId = null
                            editingNoteType = "text"
                            showNoteEditor = true
                        } else {
                            viewingNoteId = noteId
                            showNoteViewer = true
                        }
                    },
                    onCreateNote = { noteType ->
                        editingNoteId = null
                        editingNoteType = noteType
                        showNoteEditor = true
                    },
                    onArchivedNotesClick = { showArchivedNotes = true },
                    onRecycleBinClick = { showRecycleBin = true },
                    onAddRoutine = { 
                        editingRoutineId = null
                        showRoutineEditor = true
                    },
                    onEditRoutine = { routineId ->
                        editingRoutineId = routineId
                        showRoutineEditor = true
                    },
                    onStatsClick = { showRoutineStats = true },
                    onAddSchedule = { 
                        editingScheduleId = null
                        showScheduleEditor = true
                    },
                    onEditSchedule = { scheduleId ->
                        editingScheduleId = scheduleId
                        showScheduleEditor = true
                    },
                    onAddExpense = { 
                        editingExpenseId = null
                        showExpenseEditor = true
                    },
                    onEditExpense = { expenseId ->
                        editingExpenseId = expenseId
                        showExpenseEditor = true
                    },
                    onBudgetClick = { showBudgetManager = true },
                    onCategoriesClick = { showCategoriesBreakdown = true }
                )
            }
            else -> {
                // Handle all overlay screens with back handlers
                HandleOverlayScreens(
                    showSettings = showSettings,
                    showBudgetManager = showBudgetManager,
                    showCategoriesManager = showCategoriesManager,
                    showCategoriesBreakdown = showCategoriesBreakdown,
                    showArchivedNotes = showArchivedNotes,
                    showRecycleBin = showRecycleBin,
                    showNoteViewer = showNoteViewer,
                    showNoteEditor = showNoteEditor,
                    showScheduleEditor = showScheduleEditor,
                    showExpenseEditor = showExpenseEditor,
                    showRoutineEditor = showRoutineEditor,
                    showRoutineStats = showRoutineStats,
                    appPreferences = appPreferences,
                    application = application,
                    context = context,
                    expensesViewModel = expensesViewModel,
                    editingNoteId = editingNoteId,
                    editingNoteType = editingNoteType,
                    viewingNoteId = viewingNoteId,
                    editingScheduleId = editingScheduleId,
                    editingExpenseId = editingExpenseId,
                    editingRoutineId = editingRoutineId,
                    onDismissSettings = { showSettings = false },
                    onDismissBudgetManager = { showBudgetManager = false },
                    onDismissCategoriesManager = { showCategoriesManager = false },
                    onDismissCategoriesBreakdown = { showCategoriesBreakdown = false },
                    onDismissArchivedNotes = { showArchivedNotes = false },
                    onDismissRecycleBin = { showRecycleBin = false },
                    onDismissNoteViewer = { 
                        showNoteViewer = false
                        viewingNoteId = null
                    },
                    onDismissNoteEditor = { 
                        showNoteEditor = false
                        editingNoteId = null
                    },
                    onDismissScheduleEditor = { 
                        showScheduleEditor = false
                        editingScheduleId = null
                    },
                    onDismissExpenseEditor = { 
                        showExpenseEditor = false
                        editingExpenseId = null
                    },
                    onDismissRoutineEditor = { 
                        showRoutineEditor = false
                        editingRoutineId = null
                    },
                    onDismissRoutineStats = { showRoutineStats = false },
                    onNoteViewerEdit = {
                        editingNoteId = viewingNoteId
                        showNoteViewer = false
                        showNoteEditor = true
                    },
                    onArchivedNoteClick = { noteId ->
                        viewingNoteId = noteId
                        showNoteViewer = true
                        showArchivedNotes = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MainTabContent(
    selectedIndex: Int,
    navItems: List<BottomNavItem>,
    onTabSelected: (Int) -> Unit,
    application: NanaApplication,
    context: android.content.Context,
    expensesViewModel: ExpensesViewModel,
    appPreferences: AppPreferences,
    onSettingsClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onCreateNote: (String) -> Unit,
    onArchivedNotesClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onAddRoutine: () -> Unit,
    onEditRoutine: (String) -> Unit,
    onStatsClick: () -> Unit,
    onAddSchedule: () -> Unit,
    onEditSchedule: (String) -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onBudgetClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    Scaffold(
        // Remove default system bar padding; edge-to-edge handled at activity level
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0,0,0,0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (index == selectedIndex) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                                contentDescription = item.title
                            )
                        },
                        label = { 
                            if (index == selectedIndex) {
                                Text(item.title)
                            }
                        },
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(index) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { quickFadeTransition() },
                label = "tab_content"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        val notesViewModel: NotesViewModel = viewModel(
                            factory = NotesViewModelFactory(application.noteRepository)
                        )
                        NotesScreen(
                            viewModel = notesViewModel,
                            onNoteClick = onNoteClick,
                            onCreateNote = onCreateNote,
                            onArchivedNotesClick = onArchivedNotesClick,
                            onRecycleBinClick = onRecycleBinClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                    1 -> {
                        val routinesViewModel: RoutinesViewModel = viewModel(
                            factory = RoutinesViewModelFactory(application.routineRepository, context)
                        )
                        RoutinesScreen(
                            viewModel = routinesViewModel,
                            onAddRoutine = onAddRoutine,
                            onEditRoutine = onEditRoutine,
                            onStatsClick = onStatsClick,
                            onSettingsClick = onSettingsClick
                        )
                    }
                    2 -> {
                        val schedulesViewModel: SchedulesViewModel = viewModel(
                            factory = SchedulesViewModelFactory(application.scheduleRepository, context)
                        )
                        SchedulesScreen(
                            viewModel = schedulesViewModel,
                            onAddSchedule = onAddSchedule,
                            onEditSchedule = onEditSchedule,
                            onSettingsClick = onSettingsClick
                        )
                    }
                    3 -> {
                        ExpensesScreen(
                            viewModel = expensesViewModel,
                            appPreferences = appPreferences,
                            onAddExpense = onAddExpense,
                            onEditExpense = onEditExpense,
                            onSettingsClick = onSettingsClick,
                            onBudgetClick = onBudgetClick,
                            onCategoriesClick = onCategoriesClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleOverlayScreens(
    showSettings: Boolean,
    showBudgetManager: Boolean,
    showCategoriesManager: Boolean,
    showCategoriesBreakdown: Boolean,
    showArchivedNotes: Boolean,
    showRecycleBin: Boolean,
    showNoteViewer: Boolean,
    showNoteEditor: Boolean,
    showScheduleEditor: Boolean,
    showExpenseEditor: Boolean,
    showRoutineEditor: Boolean,
    showRoutineStats: Boolean,
    appPreferences: AppPreferences,
    application: NanaApplication,
    context: android.content.Context,
    expensesViewModel: ExpensesViewModel,
    editingNoteId: String?,
    editingNoteType: String,
    viewingNoteId: String?,
    editingScheduleId: String?,
    editingExpenseId: String?,
    editingRoutineId: String?,
    onDismissSettings: () -> Unit,
    onDismissBudgetManager: () -> Unit,
    onDismissCategoriesManager: () -> Unit,
    onDismissCategoriesBreakdown: () -> Unit,
    onDismissArchivedNotes: () -> Unit,
    onDismissRecycleBin: () -> Unit,
    onDismissNoteViewer: () -> Unit,
    onDismissNoteEditor: () -> Unit,
    onDismissScheduleEditor: () -> Unit,
    onDismissExpenseEditor: () -> Unit,
    onDismissRoutineEditor: () -> Unit,
    onDismissRoutineStats: () -> Unit,
    onNoteViewerEdit: () -> Unit,
    onArchivedNoteClick: (String) -> Unit
) {
    when {
        showSettings -> {
            BackHandler { onDismissSettings() }
            SettingsScreen(
                appPreferences = appPreferences,
                onBackPressed = onDismissSettings
            )
        }
        showBudgetManager -> {
            BackHandler { onDismissBudgetManager() }
            BudgetManagerScreen(
                viewModel = expensesViewModel,
                appPreferences = appPreferences,
                onBackPressed = onDismissBudgetManager
            )
        }
        showCategoriesManager -> {
            BackHandler { onDismissCategoriesManager() }
            CategoriesManagerScreen(
                viewModel = expensesViewModel,
                appPreferences = appPreferences,
                onBackPressed = onDismissCategoriesManager
            )
        }
        showCategoriesBreakdown -> {
            BackHandler { onDismissCategoriesBreakdown() }
            CategoriesBreakdownScreen(
                viewModel = expensesViewModel,
                appPreferences = appPreferences,
                onBackPressed = onDismissCategoriesBreakdown
            )
        }
        showArchivedNotes -> {
            BackHandler { onDismissArchivedNotes() }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory(application.noteRepository)
            )
            ArchivedNotesScreen(
                viewModel = notesViewModel,
                onBackPressed = onDismissArchivedNotes,
                onNoteClick = onArchivedNoteClick
            )
        }
        showRecycleBin -> {
            BackHandler { onDismissRecycleBin() }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory(application.noteRepository)
            )
            RecycleBinScreen(
                viewModel = notesViewModel,
                onBackPressed = onDismissRecycleBin
            )
        }
        showNoteViewer -> {
            BackHandler { onDismissNoteViewer() }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory(application.noteRepository)
            )
            NoteViewerScreen(
                noteId = viewingNoteId ?: "",
                notesViewModel = notesViewModel,
                onEdit = onNoteViewerEdit,
                onBack = onDismissNoteViewer
            )
        }
        showNoteEditor -> {
            BackHandler { onDismissNoteEditor() }
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory(application.noteRepository)
            )
            
            var initialTitle by remember { mutableStateOf("") }
            var initialContent by remember { mutableStateOf("") }
            var initialRich by remember { mutableStateOf("") }
            var initialHtml by remember { mutableStateOf("") }
            var initialCategory by remember { mutableStateOf("General") }
            var initialNoteType by remember { mutableStateOf(editingNoteType) }
            var isLoading by remember { mutableStateOf(editingNoteId != null) }
            
            // Load existing note data if editing
            LaunchedEffect(editingNoteId) {
                if (editingNoteId != null) {
                    notesViewModel.getNoteByStringId(editingNoteId) { note ->
                        if (note != null) {
                            initialTitle = note.title
                            initialContent = note.content
                            initialNoteType = note.noteType
                            initialRich = note.richContent
                            initialHtml = note.htmlContent
                            initialCategory = note.category ?: "General"
                        }
                        isLoading = false
                    }
                } else {
                    // For new notes, use the selected note type
                    initialNoteType = editingNoteType
                    initialRich = ""
                    initialHtml = ""
                    initialCategory = "General"
                    isLoading = false
                }
            }
            
            if (!isLoading) {
                NoteEditorScreen(
                    noteId = editingNoteId,
                    initialTitle = initialTitle,
                    initialContent = initialContent,
                    initialRichContent = initialRich,
                    initialNoteType = initialNoteType,
                    initialHtmlContent = initialHtml,
                    initialCategory = initialCategory,
                    onSave = { title, content, richContent, htmlContent, noteType, category ->
                        if (editingNoteId == null) {
                            notesViewModel.createNote(title, content, category ?: "General", richContent, htmlContent, noteType)
                        } else {
                            notesViewModel.updateNoteContent(editingNoteId, title, content, richContent, htmlContent, noteType)
                        }
                    },
                    onBack = onDismissNoteEditor
                )
            } else {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        showScheduleEditor -> {
            BackHandler { onDismissScheduleEditor() }
            val schedulesViewModel: SchedulesViewModel = viewModel(
                factory = SchedulesViewModelFactory(application.scheduleRepository, context)
            )
            ScheduleEditorScreen(
                scheduleId = editingScheduleId,
                schedulesViewModel = schedulesViewModel,
                onSave = { title, description, date, time, reminderMinutes ->
                    if (editingScheduleId == null) {
                        schedulesViewModel.createSchedule(
                            title = title,
                            description = description,
                            startTime = time,
                            endTime = LocalTime(
                                hour = if (time.hour < 23) time.hour + 1 else 23,
                                minute = time.minute
                            ),
                            date = date,
                            category = "General",
                            reminderMinutes = reminderMinutes
                        )
                    } else {
                        schedulesViewModel.getScheduleById(editingScheduleId) { schedule ->
                            if (schedule != null) {
                                schedulesViewModel.updateSchedule(
                                    schedule.copy(
                                        title = title,
                                        description = description,
                                        startTime = time,
                                        endTime = LocalTime(
                                            hour = if (time.hour < 23) time.hour + 1 else 23,
                                            minute = time.minute
                                        ),
                                        date = date,
                                        reminderMinutes = reminderMinutes
                                    )
                                )
                            }
                        }
                    }
                },
                onBack = onDismissScheduleEditor
            )
        }
        showExpenseEditor -> {
            BackHandler { onDismissExpenseEditor() }
            ExpenseEditorScreen(
                expenseId = editingExpenseId,
                expensesViewModel = expensesViewModel,
                appPreferences = appPreferences,
                onSave = { title, amount, category, date, _ ->
                    if (editingExpenseId == null) {
                        expensesViewModel.createExpense(title, amount, category, date)
                    } else {
                        expensesViewModel.getExpenseById(editingExpenseId) { expense ->
                            if (expense != null) {
                                expensesViewModel.updateExpense(
                                    expense.copy(
                                        title = title,
                                        amount = amount,
                                        category = category,
                                        date = date
                                    )
                                )
                            }
                        }
                    }
                },
                onBack = onDismissExpenseEditor
            )
        }
        showRoutineEditor -> {
            BackHandler { onDismissRoutineEditor() }
            val routinesViewModel: RoutinesViewModel = viewModel(
                factory = RoutinesViewModelFactory(application.routineRepository, context)
            )
            RoutineEditorScreen(
                routineId = editingRoutineId,
                routinesViewModel = routinesViewModel,
                onSave = { title, description, frequency, reminderTime ->
                    if (editingRoutineId == null) {
                        routinesViewModel.createRoutine(title, description, frequency, reminderTime)
                    } else {
                        routinesViewModel.getRoutineById(editingRoutineId) { routine ->
                            if (routine != null) {
                                routinesViewModel.updateRoutine(
                                    routine.copy(
                                        title = title,
                                        description = description,
                                        frequency = frequency,
                                        reminderTime = reminderTime
                                    )
                                )
                            }
                        }
                    }
                },
                onBack = onDismissRoutineEditor
            )
        }
        showRoutineStats -> {
            BackHandler { onDismissRoutineStats() }
            RoutineStatsScreen(onNavigateBack = onDismissRoutineStats)
        }
    }
}
