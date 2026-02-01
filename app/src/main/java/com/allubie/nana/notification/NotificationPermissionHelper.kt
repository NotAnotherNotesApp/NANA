package com.allubie.nana.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}

@Composable
fun rememberNotificationPermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): NotificationPermissionState {
    var hasPermission by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult(isGranted)
    }
    
    return remember(hasPermission) {
        NotificationPermissionState(
            hasPermission = hasPermission,
            requestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
    }
}

data class NotificationPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)
