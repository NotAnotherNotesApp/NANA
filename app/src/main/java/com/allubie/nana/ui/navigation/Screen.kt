package com.allubie.nana.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Notes : Screen(
        route = "notes",
        title = "Notes",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    )
    
    data object Schedule : Screen(
        route = "schedule",
        title = "Schedule",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
    
    data object Routines : Screen(
        route = "routines",
        title = "Routines",
        selectedIcon = Icons.Filled.Loop,
        unselectedIcon = Icons.Outlined.Loop
    )
    
    data object Finances : Screen(
        route = "finances",
        title = "Finances",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney
    )
    
    // Sub-screens
    data object NoteViewer : Screen(
        route = "notes/view/{noteId}",
        title = "View Note",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    ) {
        fun createRoute(noteId: Long) = "notes/view/$noteId"
    }
    
    data object NoteEditor : Screen(
        route = "notes/editor/{noteId}",
        title = "Edit Note",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    ) {
        fun createRoute(noteId: Long? = null) = "notes/editor/${noteId ?: -1}"
    }
    
    data object ChecklistEditor : Screen(
        route = "notes/checklist/{noteId}",
        title = "Checklist",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    ) {
        fun createRoute(noteId: Long? = null) = "notes/checklist/${noteId ?: -1}"
    }
    
    data object NotesArchive : Screen(
        route = "notes/archive",
        title = "Archive",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    )
    
    data object NotesTrash : Screen(
        route = "notes/trash",
        title = "Trash",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    )
    
    data object ScheduleViewer : Screen(
        route = "schedule/view/{eventId}",
        title = "View Event",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ) {
        fun createRoute(eventId: Long) = "schedule/view/$eventId"
    }
    
    data object ScheduleEditor : Screen(
        route = "schedule/editor/{eventId}",
        title = "Edit Event",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ) {
        fun createRoute(eventId: Long? = null) = "schedule/editor/${eventId ?: -1}"
    }
    
    data object RoutineEditor : Screen(
        route = "routines/editor/{routineId}",
        title = "Edit Routine",
        selectedIcon = Icons.Filled.Loop,
        unselectedIcon = Icons.Outlined.Loop
    ) {
        fun createRoute(routineId: Long? = null) = "routines/editor/${routineId ?: -1}"
    }
    
    data object RoutineStatistics : Screen(
        route = "routines/statistics",
        title = "Statistics",
        selectedIcon = Icons.Filled.Loop,
        unselectedIcon = Icons.Outlined.Loop
    )
    
    data object FinancesOverview : Screen(
        route = "finances/overview",
        title = "Overview",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney
    )
    
    data object BudgetManager : Screen(
        route = "finances/budget",
        title = "Budget Manager",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney
    )
    
    data object TransactionEditor : Screen(
        route = "finances/editor/{transactionId}",
        title = "Edit Transaction",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney
    ) {
        fun createRoute(transactionId: Long? = null) = "finances/editor/${transactionId ?: -1}"
    }
    
    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    )
    
    data object LabelsAndCategories : Screen(
        route = "settings/labels/{labelType}",
        title = "Labels & Categories",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    ) {
        fun createRoute(labelType: String? = null) = "settings/labels/${labelType ?: "all"}"
    }
}

val bottomNavItems = listOf(
    Screen.Notes,
    Screen.Schedule,
    Screen.Routines,
    Screen.Finances
)
