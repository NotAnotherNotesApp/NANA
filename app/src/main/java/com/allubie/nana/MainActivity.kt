package com.allubie.nana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.notification.rememberNotificationPermissionState
import com.allubie.nana.ui.navigation.NanaNavHost
import com.allubie.nana.ui.navigation.Screen
import com.allubie.nana.ui.navigation.bottomNavItems
import com.allubie.nana.ui.theme.NanaTheme
import com.allubie.nana.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val preferencesManager = (application as NanaApplication).preferencesManager
        
        setContent {
            val themeMode by preferencesManager.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            
            NanaTheme(themeMode = themeMode) {
                // Request notification permission on first launch
                val notificationPermissionState = rememberNotificationPermissionState()
                LaunchedEffect(Unit) {
                    if (!notificationPermissionState.hasPermission) {
                        notificationPermissionState.requestPermission()
                    }
                }
                
                NanaApp()
            }
        }
    }
}

@Composable
fun NanaApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine if bottom bar should be shown
    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }
    
    // Calculate the navigation bar height for consistent padding
    val density = LocalDensity.current
    val navigationBarHeight = with(density) { 
        WindowInsets.navigationBars.getBottom(density).toDp() 
    }
    val bottomBarHeight = 80.dp + navigationBarHeight
    
    // Animate the bottom bar offset instead of visibility to prevent content jumping
    val bottomBarOffset by animateDpAsState(
        targetValue = if (showBottomBar) 0.dp else bottomBarHeight,
        animationSpec = tween(300),
        label = "bottomBarOffset"
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.offset { IntOffset(0, bottomBarOffset.roundToPx()) }
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { 
                        it.route == screen.route 
                    } == true
                    
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon 
                                              else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        // Only apply padding when bottom bar is shown
        // Screens without bottom bar handle their own insets
        NanaNavHost(
            navController = navController,
            modifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier
        )
    }
}
