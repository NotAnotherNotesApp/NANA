package com.allubie.nana.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.provider.Settings
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.model.Event
import com.allubie.nana.data.model.Routine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

object ReminderScheduler {
    
    private fun getAlarmManager(context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getAlarmManager(context).canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    fun getExactAlarmSettingsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }
    
    fun scheduleEventReminder(
        context: Context,
        event: Event,
        reminderMinutes: Int
    ) {
        val alarmManager = getAlarmManager(context)
        
        // Calculate trigger time
        val triggerTime = event.startTime - (reminderMinutes * 60 * 1000L)
        
        // Don't schedule if time has passed
        if (triggerTime <= System.currentTimeMillis()) return
        
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val eventTime = timeFormat.format(Date(event.startTime))
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_EVENT_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EXTRA_EVENT_TIME, eventTime)
            putExtra(ReminderReceiver.EXTRA_REMINDER_MINUTES, reminderMinutes)
        }
        
        val requestCode = stableRequestCode(event.id, reminderMinutes.toLong())
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Fall back to inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Fall back to inexact alarm if permission denied
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
        Log.d("ReminderScheduler", "Scheduled event reminder: id=${event.id}, minutes=$reminderMinutes, requestCode=$requestCode, trigger=$triggerTime")
    }
    
    fun scheduleAllEventReminders(context: Context, event: Event) {
        val reminderMinutes = event.reminderMinutes
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        
        reminderMinutes.forEach { minutes ->
            scheduleEventReminder(context, event, minutes)
        }
    }
    
    fun cancelEventReminders(context: Context, eventId: Long, reminderMinutes: List<Int>) {
        val alarmManager = getAlarmManager(context)
        
        reminderMinutes.forEach { minutes ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_EVENT_REMINDER
            }

            val requestCode = stableRequestCode(eventId, minutes.toLong())

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }
    }
    
    fun scheduleRoutineReminder(
        context: Context,
        routine: Routine
    ) {
        val reminderTime = routine.reminderTime ?: return
        val scheduledDays = routine.scheduledDays
        
        // Parse time (format: "HH:mm")
        val timeParts = reminderTime.split(":")
        if (timeParts.size != 2) return
        
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        getAlarmManager(context)
        
        // Schedule for each day of the week
        val daysToSchedule = if (scheduledDays.isEmpty()) {
            // Daily if no specific days
            listOf(0, 1, 2, 3, 4, 5, 6)
        } else {
            scheduledDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
        
        daysToSchedule.forEach { dayOfWeek ->
            scheduleRoutineForDay(context, routine, hour, minute, dayOfWeek)
        }
    }
    
    private fun scheduleRoutineForDay(
        context: Context,
        routine: Routine,
        hour: Int,
        minute: Int,
        dayOfWeek: Int // 0 = Sunday, 6 = Saturday
    ) {
        val alarmManager = getAlarmManager(context)
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Set to the correct day of week
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-based
            var daysUntil = dayOfWeek - currentDayOfWeek
            if (daysUntil < 0) daysUntil += 7
            if (daysUntil == 0 && timeInMillis <= System.currentTimeMillis()) {
                daysUntil = 7
            }
            add(Calendar.DAY_OF_MONTH, daysUntil)
        }
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_ROUTINE_REMINDER
            putExtra(ReminderReceiver.EXTRA_ROUTINE_ID, routine.id)
            putExtra(ReminderReceiver.EXTRA_ROUTINE_TITLE, routine.title)
        }
        
        val requestCode = stableRequestCode(routine.id, dayOfWeek.toLong())
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            // Schedule repeating weekly alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For exact alarms, schedule next occurrence and reschedule after it fires
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
        Log.d("ReminderScheduler", "Scheduled routine reminder: id=${routine.id}, day=$dayOfWeek, requestCode=$requestCode, time=${calendar.timeInMillis}")
    }
    
    fun cancelRoutineReminders(context: Context, routineId: Long) {
        val alarmManager = getAlarmManager(context)
        
        // Cancel all 7 possible day alarms
        for (dayOfWeek in 0..6) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_ROUTINE_REMINDER
            }
            
            val requestCode = stableRequestCode(routineId, dayOfWeek.toLong())

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }
    }

    private fun stableRequestCode(vararg parts: Long): Int {
        var code = 17L
        for (p in parts) {
            code = code * 31 + (p and 0xFFFFFFFF)
        }
        return (code and 0x7FFFFFFF).toInt()
    }
    
    suspend fun rescheduleAllReminders(context: Context, database: NanaDatabase) {
        // Reschedule all upcoming events
        val events = database.eventDao().getAllEvents().first()
        val now = System.currentTimeMillis()
        
        events.filter { it.startTime > now }.forEach { event ->
            scheduleAllEventReminders(context, event)
        }
        
        // Reschedule all routines
        val routines = database.routineDao().getAllRoutines().first()
        routines.filter { it.reminderTime != null }.forEach { routine ->
            scheduleRoutineReminder(context, routine)
        }
    }
}
