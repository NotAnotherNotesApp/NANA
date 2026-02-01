package com.allubie.nana.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.allubie.nana.MainActivity
import com.allubie.nana.R

object NotificationHelper {
    
    const val CHANNEL_EVENTS = "events_channel"
    const val CHANNEL_ROUTINES = "routines_channel"
    const val CHANNEL_REMINDERS = "reminders_channel"
    
    private const val CHANNEL_EVENTS_NAME = "Event Reminders"
    private const val CHANNEL_ROUTINES_NAME = "Routine Reminders"
    private const val CHANNEL_REMINDERS_NAME = "General Reminders"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Events channel
            val eventsChannel = NotificationChannel(
                CHANNEL_EVENTS,
                CHANNEL_EVENTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming events and schedule items"
                enableVibration(true)
                enableLights(true)
            }
            
            // Routines channel
            val routinesChannel = NotificationChannel(
                CHANNEL_ROUTINES,
                CHANNEL_ROUTINES_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for daily routine reminders"
                enableVibration(true)
            }
            
            // General reminders channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General reminder notifications"
            }
            
            notificationManager.createNotificationChannels(
                listOf(eventsChannel, routinesChannel, remindersChannel)
            )
        }
    }
    
    fun showEventNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        eventId: Long
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "schedule")
            putExtra("event_id", eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_notification_event)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
            e.printStackTrace()
        }
    }
    
    fun showRoutineNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        routineId: Long
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "routines")
            putExtra("routine_id", routineId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Action to mark as done
        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_MARK_ROUTINE_DONE
            putExtra(ReminderReceiver.EXTRA_ROUTINE_ID, routineId)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINES)
            .setSmallIcon(R.drawable.ic_notification_routine)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_done,
                "Mark Done",
                donePendingIntent
            )
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
