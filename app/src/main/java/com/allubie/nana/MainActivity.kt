package com.allubie.nana

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.ui.navigation.MainNavigation
import com.allubie.nana.ui.theme.NANATheme
import com.allubie.nana.notification.NotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    // Enable edge-to-edge with transparent scrims to avoid contrast lines from system bars
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = android.graphics.Color.TRANSPARENT,
            darkScrim = android.graphics.Color.TRANSPARENT
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = android.graphics.Color.TRANSPARENT,
            darkScrim = android.graphics.Color.TRANSPARENT
        )
    )
        
        // Initialize notification service
        NotificationService(this)
        
    setContent {
            val context = LocalContext.current
            val appPreferences = remember { AppPreferences(context) }
            
            // Check for notification navigation intent
            // val initialScreen = intent.getStringExtra("screen") // TODO: Implement deep linking
            
            // Request notification permission for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                appPreferences.updateNotifications(isGranted)
            }
            
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    if (!hasPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            
            NANATheme(
                darkTheme = appPreferences.isDarkTheme,
                amoledTheme = appPreferences.isAmoledTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    MainNavigation(appPreferences = appPreferences)
                }
            }
        }
    }
}
