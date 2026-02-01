package com.allubie.nana.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_EVENT_REMINDER = "com.allubie.nana.ACTION_EVENT_REMINDER"
        const val ACTION_ROUTINE_REMINDER = "com.allubie.nana.ACTION_ROUTINE_REMINDER"
        const val ACTION_MARK_ROUTINE_DONE = "com.allubie.nana.ACTION_MARK_ROUTINE_DONE"
        
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_TIME = "event_time"
        const val EXTRA_REMINDER_MINUTES = "reminder_minutes"
        
        const val EXTRA_ROUTINE_ID = "routine_id"
        const val EXTRA_ROUTINE_TITLE = "routine_title"
        
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_EVENT_REMINDER -> handleEventReminder(context, intent)
            ACTION_ROUTINE_REMINDER -> handleRoutineReminder(context, intent)
            ACTION_MARK_ROUTINE_DONE -> handleMarkRoutineDone(context, intent)
            Intent.ACTION_BOOT_COMPLETED -> handleBootCompleted(context)
        }
    }
    
    private fun handleEventReminder(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Event Reminder"
        val eventTime = intent.getStringExtra(EXTRA_EVENT_TIME) ?: ""
        val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINUTES, 15)
        
        if (eventId == -1L) return
        
        val message = when (reminderMinutes) {
            0 -> "Starting now"
            1 -> "Starting in 1 minute"
            else -> "Starting in $reminderMinutes minutes"
        }
        
        val notificationId = (eventId * 100 + reminderMinutes).toInt()
        
        NotificationHelper.showEventNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = if (eventTime.isNotEmpty()) "$message - $eventTime" else message,
            eventId = eventId
        )
    }
    
    private fun handleRoutineReminder(context: Context, intent: Intent) {
        val routineId = intent.getLongExtra(EXTRA_ROUTINE_ID, -1)
        val title = intent.getStringExtra(EXTRA_ROUTINE_TITLE) ?: "Routine Reminder"
        
        if (routineId == -1L) return
        
        val notificationId = (routineId + 50000).toInt()
        
        NotificationHelper.showRoutineNotification(
            context = context,
            notificationId = notificationId,
            title = "Time for: $title",
            message = "Tap to track your progress",
            routineId = routineId
        )
    }
    
    private fun handleMarkRoutineDone(context: Context, intent: Intent) {
        val routineId = intent.getLongExtra(EXTRA_ROUTINE_ID, -1)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        
        if (notificationId != -1) {
            NotificationHelper.cancelNotification(context, notificationId)
        }
        
        // Mark routine as completed in database
        if (routineId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? com.allubie.nana.NanaApplication
                    app?.database?.let { db ->
                        val completionDao = db.routineCompletionDao()
                        val routineDao = db.routineDao()
                        
                        // Create completion record for today
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val todayString = dateFormat.format(java.util.Date())
                        
                        // Check if already completed today
                        val existing = completionDao.getCompletionForDate(routineId, todayString)
                        if (existing == null || !existing.isCompleted) {
                            completionDao.insertCompletion(
                                com.allubie.nana.data.model.RoutineCompletion(
                                    id = existing?.id ?: 0,
                                    routineId = routineId,
                                    date = todayString,
                                    isCompleted = true
                                )
                            )
                        }
                        
                        // Also update the routine's streak
                        val routine = routineDao.getRoutineById(routineId)
                        routine?.let {
                            routineDao.updateRoutine(it.copy(
                                currentStreak = it.currentStreak + 1,
                                updatedAt = System.currentTimeMillis()
                            ))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        // Reschedule all reminders after device reboot
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? com.allubie.nana.NanaApplication
                app?.let {
                    ReminderScheduler.rescheduleAllReminders(context, it.database)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
