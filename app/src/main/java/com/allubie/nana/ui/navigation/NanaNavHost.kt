package com.allubie.nana.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.allubie.nana.ui.screens.finances.BudgetManagerScreen
import com.allubie.nana.ui.screens.finances.FinancesOverviewScreen
import com.allubie.nana.ui.screens.finances.FinancesScreen
import com.allubie.nana.ui.screens.finances.TransactionEditorScreen
import com.allubie.nana.ui.screens.notes.ChecklistEditorScreen
import com.allubie.nana.ui.screens.notes.NoteEditorScreen
import com.allubie.nana.ui.screens.notes.NoteViewerScreen
import com.allubie.nana.ui.screens.notes.NotesArchiveScreen
import com.allubie.nana.ui.screens.notes.NotesScreen
import com.allubie.nana.ui.screens.notes.NotesTrashScreen
import com.allubie.nana.ui.screens.routines.RoutineEditorScreen
import com.allubie.nana.ui.screens.routines.RoutineStatisticsScreen
import com.allubie.nana.ui.screens.routines.RoutinesScreen
import com.allubie.nana.ui.screens.schedule.ScheduleEditorScreen
import com.allubie.nana.ui.screens.schedule.ScheduleScreen
import com.allubie.nana.ui.screens.schedule.ScheduleViewerScreen
import com.allubie.nana.ui.screens.settings.SettingsScreen

// Shared transition animations for navigation destinations
private const val TRANSITION_DURATION_MS = 300

private val subScreenEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION_MS))
}

private val subScreenPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION_MS))
}

private val subScreenPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION_MS))
}

private val exitSlideLeft: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION_MS))
}

private val exitFade: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
}

// Main tabs slide when navigating to/from child sub-screens, and fade when switching between sibling tabs
private fun mainTabEnter(
    childRoutes: Set<String>
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (initialState.destination.route in childRoutes)
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION_MS))
    else
        fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
}

private fun mainTabExit(
    childRoutes: Set<String>
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (targetState.destination.route in childRoutes)
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(TRANSITION_DURATION_MS))
    else
        fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
}

private fun mainTabPopEnter(
    childRoutes: Set<String>
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (initialState.destination.route in childRoutes)
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(TRANSITION_DURATION_MS))
    else
        fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
}

@Composable
fun NanaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val notesChildRoutes = setOf(
        Screen.NoteViewer.route, Screen.NoteEditor.route,
        Screen.ChecklistEditor.route, Screen.NotesArchive.route,
        Screen.NotesTrash.route, Screen.Settings.route
    )
    val scheduleChildRoutes = setOf(
        Screen.ScheduleViewer.route, Screen.ScheduleEditor.route, Screen.Settings.route
    )
    val routinesChildRoutes = setOf(
        Screen.RoutineEditor.route, Screen.RoutineStatistics.route, Screen.Settings.route
    )
    val financesChildRoutes = setOf(
        Screen.TransactionEditor.route, Screen.FinancesOverview.route,
        Screen.BudgetManager.route, Screen.Settings.route
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route,
        modifier = modifier
    ) {

        composable(
            route = Screen.Notes.route,
            enterTransition = mainTabEnter(notesChildRoutes),
            exitTransition = mainTabExit(notesChildRoutes),
            popEnterTransition = mainTabPopEnter(notesChildRoutes),
            popExitTransition = exitFade
        ) {
            NotesScreen(
                onNavigateToViewer = { noteId ->
                    navController.navigate(Screen.NoteViewer.createRoute(noteId))
                },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onNavigateToChecklist = { noteId ->
                    navController.navigate(Screen.ChecklistEditor.createRoute(noteId))
                },
                onNavigateToArchive = {
                    navController.navigate(Screen.NotesArchive.route)
                },
                onNavigateToTrash = {
                    navController.navigate(Screen.NotesTrash.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Schedule.route,
            enterTransition = mainTabEnter(scheduleChildRoutes),
            exitTransition = mainTabExit(scheduleChildRoutes),
            popEnterTransition = mainTabPopEnter(scheduleChildRoutes),
            popExitTransition = exitFade
        ) {
            ScheduleScreen(
                onNavigateToViewer = { eventId ->
                    navController.navigate(Screen.ScheduleViewer.createRoute(eventId))
                },
                onNavigateToEditor = { eventId ->
                    navController.navigate(Screen.ScheduleEditor.createRoute(eventId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Routines.route,
            enterTransition = mainTabEnter(routinesChildRoutes),
            exitTransition = mainTabExit(routinesChildRoutes),
            popEnterTransition = mainTabPopEnter(routinesChildRoutes),
            popExitTransition = exitFade
        ) {
            RoutinesScreen(
                onNavigateToEditor = { routineId ->
                    navController.navigate(Screen.RoutineEditor.createRoute(routineId))
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.RoutineStatistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Finances.route,
            enterTransition = mainTabEnter(financesChildRoutes),
            exitTransition = mainTabExit(financesChildRoutes),
            popEnterTransition = mainTabPopEnter(financesChildRoutes),
            popExitTransition = exitFade
        ) {
            FinancesScreen(
                onNavigateToEditor = { transactionId ->
                    navController.navigate(Screen.TransactionEditor.createRoute(transactionId))
                },
                onNavigateToOverview = { month, year ->
                    navController.navigate(Screen.FinancesOverview.createRoute(month, year))
                },
                onNavigateToBudgetManager = {
                    navController.navigate(Screen.BudgetManager.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ── Viewer sub-screens (slide-out-left exit: can push deeper) ────

        composable(
            route = Screen.NoteViewer.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitSlideLeft,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1
            NoteViewerScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { 
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(
            route = Screen.NotesArchive.route,
            enterTransition = subScreenEnter,
            exitTransition = exitSlideLeft,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            NotesArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(
            route = Screen.ScheduleViewer.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitSlideLeft,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1
            ScheduleViewerScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = {
                    navController.navigate(Screen.ScheduleEditor.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.BudgetManager.route,
            enterTransition = subScreenEnter,
            exitTransition = exitSlideLeft,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            BudgetManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = subScreenEnter,
            exitTransition = exitSlideLeft,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLabels = { navController.navigate(Screen.LabelsAndCategories.route) }
            )
        }

        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1
            NoteEditorScreen(
                noteId = if (noteId == -1L) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ChecklistEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1
            ChecklistEditorScreen(
                noteId = if (noteId == -1L) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NotesTrash.route,
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            NotesTrashScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ScheduleEditor.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1
            ScheduleEditorScreen(
                eventId = if (eventId == -1L) null else eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RoutineEditor.route,
            arguments = listOf(navArgument("routineId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: -1
            RoutineEditorScreen(
                routineId = if (routineId == -1L) null else routineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RoutineStatistics.route,
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            RoutineStatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FinancesOverview.route,
            arguments = listOf(
                navArgument("month") { type = NavType.IntType },
                navArgument("year") { type = NavType.IntType }
            ),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getInt("month") ?: java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
            val year = backStackEntry.arguments?.getInt("year") ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            FinancesOverviewScreen(
                selectedMonth = month,
                selectedYear = year,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TransactionEditor.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
            enterTransition = subScreenEnter,
            exitTransition = exitFade,
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1
            TransactionEditorScreen(
                transactionId = if (transactionId == -1L) null else transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LabelsAndCategories.route,
            enterTransition = subScreenEnter,
            exitTransition = exitFade,  // Fixed: was incorrectly slideOutRight
            popEnterTransition = subScreenPopEnter,
            popExitTransition = subScreenPopExit
        ) {
            com.allubie.nana.ui.screens.settings.LabelsAndCategoriesScreen(
                database = com.allubie.nana.data.NanaDatabase.getDatabase(
                    androidx.compose.ui.platform.LocalContext.current
                ),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
