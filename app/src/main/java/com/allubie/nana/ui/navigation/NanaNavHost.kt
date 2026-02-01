package com.allubie.nana.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun NanaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route,
        modifier = modifier
    ) {
        // Main screens - with slide transitions for navigating to/from sub-screens
        composable(
            route = Screen.Notes.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.NoteViewer.route, Screen.NoteEditor.route, 
                    Screen.ChecklistEditor.route, Screen.NotesArchive.route,
                    Screen.NotesTrash.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.NoteViewer.route, Screen.NoteEditor.route,
                    Screen.ChecklistEditor.route, Screen.NotesArchive.route,
                    Screen.NotesTrash.route, Screen.Settings.route ->
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else -> fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.NoteViewer.route, Screen.NoteEditor.route, 
                    Screen.ChecklistEditor.route, Screen.NotesArchive.route,
                    Screen.NotesTrash.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
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
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.ScheduleViewer.route, Screen.ScheduleEditor.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.ScheduleViewer.route, Screen.ScheduleEditor.route, Screen.Settings.route ->
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else -> fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.ScheduleViewer.route, Screen.ScheduleEditor.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
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
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.RoutineEditor.route, Screen.RoutineStatistics.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.RoutineEditor.route, Screen.RoutineStatistics.route, Screen.Settings.route ->
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else -> fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.RoutineEditor.route, Screen.RoutineStatistics.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
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
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.TransactionEditor.route, Screen.FinancesOverview.route,
                    Screen.BudgetManager.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.TransactionEditor.route, Screen.FinancesOverview.route,
                    Screen.BudgetManager.route, Screen.Settings.route ->
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else -> fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.TransactionEditor.route, Screen.FinancesOverview.route,
                    Screen.BudgetManager.route, Screen.Settings.route ->
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    else -> fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            FinancesScreen(
                onNavigateToEditor = { transactionId ->
                    navController.navigate(Screen.TransactionEditor.createRoute(transactionId))
                },
                onNavigateToOverview = {
                    navController.navigate(Screen.FinancesOverview.route)
                },
                onNavigateToBudgetManager = {
                    navController.navigate(Screen.BudgetManager.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Sub-screens with slide animation
        composable(
            route = Screen.NoteViewer.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
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
            route = Screen.NoteEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
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
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1
            ChecklistEditorScreen(
                noteId = if (noteId == -1L) null else noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.NotesArchive.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            NotesArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                }
            )
        }
        
        composable(
            route = Screen.NotesTrash.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            NotesTrashScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ScheduleViewer.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
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
            route = Screen.ScheduleEditor.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
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
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: -1
            RoutineEditorScreen(
                routineId = if (routineId == -1L) null else routineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.RoutineStatistics.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            RoutineStatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.FinancesOverview.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            FinancesOverviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.BudgetManager.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                if (targetState.destination.route?.startsWith("settings/labels") == true) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                } else {
                    fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            BudgetManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAddCategory = { navController.navigate(Screen.LabelsAndCategories.createRoute("expense")) }
            )
        }
        
        composable(
            route = Screen.TransactionEditor.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                if (targetState.destination.route?.startsWith("settings/labels") == true) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                } else {
                    fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1
            TransactionEditorScreen(
                transactionId = if (transactionId == -1L) null else transactionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { categoryType -> 
                    navController.navigate(Screen.LabelsAndCategories.createRoute(categoryType))
                }
            )
        }
        
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                if (targetState.destination.route?.startsWith("settings/labels") == true) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                } else {
                    fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLabels = { navController.navigate(Screen.LabelsAndCategories.createRoute()) }
            )
        }
        
        composable(
            route = Screen.LabelsAndCategories.route,
            arguments = listOf(navArgument("labelType") { 
                type = NavType.StringType
                defaultValue = "all"
            }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) { backStackEntry ->
            val labelType = backStackEntry.arguments?.getString("labelType")
            com.allubie.nana.ui.screens.settings.LabelsAndCategoriesScreen(
                database = com.allubie.nana.data.NanaDatabase.getDatabase(
                    androidx.compose.ui.platform.LocalContext.current
                ),
                onNavigateBack = { navController.popBackStack() },
                initialLabelType = if (labelType == "all") null else labelType
            )
        }
    }
}
