package com.allubie.nana

import android.app.Application
import androidx.room.InvalidationTracker
import com.allubie.nana.data.BackupManager
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.notification.NotificationHelper
import com.allubie.nana.widget.WidgetRefreshWorker
import com.allubie.nana.widget.requestBudgetWidgetRefresh
import com.allubie.nana.widget.updateChecklistWidgets
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
        BackupManager(this, database, preferencesManager)
    }

    private val budgetWidgetDbObserver = object : InvalidationTracker.Observer("transactions", "budgets") {
        override fun onInvalidated(tables: Set<String>) {
            requestBudgetWidgetRefresh(this@NanaApplication)
        }
    }

    private val checklistWidgetDbObserver = object : InvalidationTracker.Observer("notes", "checklist_items") {
        override fun onInvalidated(tables: Set<String>) {
            applicationScope.launch {
                updateChecklistWidgets(this@NanaApplication)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Restore saved timezone
        applicationScope.launch {
            val savedTimezone = preferencesManager.timezone.first()
            TimeZone.setDefault(TimeZone.getTimeZone(savedTimezone))
        }

        database.invalidationTracker.addObserver(budgetWidgetDbObserver)
        database.invalidationTracker.addObserver(checklistWidgetDbObserver)
        WidgetRefreshWorker.schedule(this)
    }
}
