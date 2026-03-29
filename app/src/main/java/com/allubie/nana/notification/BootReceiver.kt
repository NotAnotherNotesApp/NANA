package com.allubie.nana.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.allubie.nana.widget.WidgetRefreshWorker
import com.allubie.nana.widget.updateAllWidgets

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Reschedule all reminders after device reboot
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? com.allubie.nana.NanaApplication
                    app?.let {
                        ReminderScheduler.rescheduleAllReminders(context, it.database)
                        WidgetRefreshWorker.schedule(context)
                        updateAllWidgets(context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
