package com.allubie.nana

import android.app.Application
import com.allubie.nana.data.BackupManager
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.repository.LabelRepository
import com.allubie.nana.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.TimeZone

class NanaApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    val database: NanaDatabase by lazy {
        NanaDatabase.getDatabase(this)
    }
    
    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(this)
    }
    
    val backupManager: BackupManager by lazy {
        BackupManager(this, database)
    }
    
    val labelRepository: LabelRepository by lazy {
        LabelRepository(database.labelDao())
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Seed preset labels if needed (do this early, on IO thread)
        applicationScope.launch(Dispatchers.IO) {
            labelRepository.seedPresetsIfNeeded()
        }
        
        // Restore saved timezone
        applicationScope.launch {
            val savedTimezone = preferencesManager.timezone.first()
            TimeZone.setDefault(TimeZone.getTimeZone(savedTimezone))
        }
    }
}
