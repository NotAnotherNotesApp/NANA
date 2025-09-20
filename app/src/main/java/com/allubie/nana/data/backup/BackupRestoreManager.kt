package com.allubie.nana.data.backup

import android.content.Context
import android.net.Uri
import com.allubie.nana.data.preferences.AppPreferences
import com.allubie.nana.ui.viewmodel.ExpensesViewModel
import com.allubie.nana.ui.viewmodel.NotesViewModel
import com.allubie.nana.ui.viewmodel.RoutinesViewModel
import com.allubie.nana.ui.viewmodel.SchedulesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Serializable snapshot of all app data used for export/import.
 */
data class AppBackupData(
    val exportDate: String,
    val notes: List<String> = emptyList(),
    val schedules: List<String> = emptyList(),
    val routines: List<String> = emptyList(),
    val expenses: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val preferences: Map<String, String> = emptyMap()
)

object BackupRestoreManager {

    /**
     * Export all application data to the given [uri]. Invokes [onResult] with success flag & message.
     */
    suspend fun exportData(
        context: Context,
        uri: Uri,
        appPreferences: AppPreferences,
        notesViewModel: NotesViewModel?,
        schedulesViewModel: SchedulesViewModel?,
        routinesViewModel: RoutinesViewModel?,
        expensesViewModel: ExpensesViewModel?,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val notesData = safeCollectList {
                    notesViewModel?.notes?.first()?.map { note ->
                        JSONObject().apply {
                            put("id", note.id)
                            put("title", note.title)
                            put("content", note.content)
                            put("category", note.category)
                            put("isPinned", note.isPinned)
                            put("isArchived", note.isArchived)
                            put("isDeleted", note.isDeleted)
                            put("createdAt", note.createdAt.toString())
                            put("updatedAt", note.updatedAt.toString())
                        }.toString()
                    } ?: emptyList()
                }

                val schedulesData = safeCollectList {
                    schedulesViewModel?.allSchedules?.first()?.map { schedule ->
                        JSONObject().apply {
                            put("id", schedule.id)
                            put("title", schedule.title)
                            put("description", schedule.description)
                            put("date", schedule.date.toString())
                            put("startTime", schedule.startTime.toString())
                            put("endTime", schedule.endTime.toString())
                            put("category", schedule.category)
                            put("isCompleted", schedule.isCompleted)
                            put("reminderMinutes", schedule.reminderMinutes)
                            put("createdAt", schedule.createdAt)
                        }.toString()
                    } ?: emptyList()
                }

                val routinesData = safeCollectList {
                    routinesViewModel?.routines?.first()?.map { routine ->
                        JSONObject().apply {
                            put("id", routine.id)
                            put("title", routine.title)
                            put("description", routine.description)
                            put("frequency", routine.frequency)
                            put("reminderTime", routine.reminderTime)
                            put("isActive", routine.isActive)
                            put("isPinned", routine.isPinned)
                            put("createdAt", routine.createdAt.toString())
                        }.toString()
                    } ?: emptyList()
                }

                val expensesData = safeCollectList {
                    expensesViewModel?.allExpenses?.first()?.map { expense ->
                        JSONObject().apply {
                            put("id", expense.id)
                            put("title", expense.title)
                            put("amount", expense.amount)
                            put("category", expense.category)
                            put("date", expense.date.toString())
                            put("createdAt", expense.createdAt)
                        }.toString()
                    } ?: emptyList()
                }

                val categoriesData = safeCollectList {
                    expensesViewModel?.allCategories?.first()?.map { category ->
                        JSONObject().apply {
                            put("name", category.name)
                            put("iconName", category.iconName)
                            put("colorHex", category.colorHex)
                            put("monthlyBudget", category.monthlyBudget)
                        }.toString()
                    } ?: emptyList()
                }

                val backupData = AppBackupData(
                    exportDate = exportDate,
                    notes = notesData,
                    schedules = schedulesData,
                    routines = routinesData,
                    expenses = expensesData,
                    categories = categoriesData,
                    preferences = mapOf(
                        "currency" to appPreferences.currency,
                        "darkTheme" to appPreferences.isDarkTheme.toString(),
                        "amoledTheme" to appPreferences.isAmoledTheme.toString(),
                        "notificationsEnabled" to appPreferences.notificationsEnabled.toString(),
                        "routineRemindersEnabled" to appPreferences.routineRemindersEnabled.toString(),
                        "scheduleRemindersEnabled" to appPreferences.scheduleRemindersEnabled.toString(),
                        "defaultReminderMinutes" to appPreferences.defaultReminderMinutes.toString(),
                        "is24HourFormat" to appPreferences.is24HourFormat.toString(),
                        "totalMonthlyBudget" to appPreferences.totalMonthlyBudget.toString(),
                        "appVersion" to "1.0.0"
                    )
                )

                val json = JSONObject().apply {
                    put("exportDate", backupData.exportDate)
                    put("notes", backupData.notes)
                    put("schedules", backupData.schedules)
                    put("routines", backupData.routines)
                    put("expenses", backupData.expenses)
                    put("categories", backupData.categories)
                    put("preferences", JSONObject(backupData.preferences))
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toString(2).toByteArray())
                    outputStream.flush()
                }
            }
            onResult(true, "Data exported successfully!")
        } catch (e: Exception) {
            onResult(false, "Failed to export data: ${e.message}")
        }
    }

    /**
     * Import application data from [uri]. Existing data is appended (no destructive clear yet).
     */
    suspend fun importData(
        context: Context,
        uri: Uri,
        appPreferences: AppPreferences,
        notesViewModel: NotesViewModel?,
        schedulesViewModel: SchedulesViewModel?,
        routinesViewModel: RoutinesViewModel?,
        expensesViewModel: ExpensesViewModel?,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Failed to read file")

                val json = JSONObject(jsonString)
                val preferences = json.optJSONObject("preferences")

                // Preferences restore (safe parsing)
                preferences?.optString("currency")?.takeIf { it.isNotEmpty() }?.let { appPreferences.updateCurrency(it) }
                preferences?.optString("darkTheme")?.toBooleanStrictOrNull()?.let { appPreferences.updateDarkTheme(it) }
                preferences?.optString("amoledTheme")?.toBooleanStrictOrNull()?.let { appPreferences.updateAmoledTheme(it) }
                preferences?.optString("notificationsEnabled")?.toBooleanStrictOrNull()?.let { appPreferences.updateNotifications(it) }
                preferences?.optString("routineRemindersEnabled")?.toBooleanStrictOrNull()?.let { appPreferences.updateRoutineReminders(it) }
                preferences?.optString("scheduleRemindersEnabled")?.toBooleanStrictOrNull()?.let { appPreferences.updateScheduleReminders(it) }
                preferences?.optString("defaultReminderMinutes")?.toIntOrNull()?.let { appPreferences.updateDefaultReminderMinutes(it) }
                preferences?.optString("is24HourFormat")?.toBooleanStrictOrNull()?.let { appPreferences.updateTimeFormat(it) }
                preferences?.optString("totalMonthlyBudget")?.toDoubleOrNull()?.let { appPreferences.updateTotalMonthlyBudget(it) }

                // Notes
                json.optJSONArray("notes")?.let { arr ->
                    if (notesViewModel != null) {
                        for (i in 0 until arr.length()) {
                            runCatching {
                                val noteJson = JSONObject(arr.getString(i))
                                val title = noteJson.optString("title", "")
                                val content = noteJson.optString("content", "")
                                val category = noteJson.optString("category", "")
                                if (title.isNotBlank()) notesViewModel.createNote(title, content, category)
                            }
                        }
                    }
                }

                // Schedules
                json.optJSONArray("schedules")?.let { arr ->
                    if (schedulesViewModel != null) {
                        for (i in 0 until arr.length()) {
                            runCatching {
                                val scheduleJson = JSONObject(arr.getString(i))
                                val title = scheduleJson.optString("title", "")
                                if (title.isBlank()) return@runCatching
                                val description = scheduleJson.optString("description", "")
                                val dateStr = scheduleJson.optString("date", "")
                                val startStr = scheduleJson.optString("startTime", "")
                                val endStr = scheduleJson.optString("endTime", "")
                                val category = scheduleJson.optString("category", "General")
                                val locationRaw = scheduleJson.optString("location", "")
                                val location = locationRaw.ifBlank { null }
                                val isRecurring = scheduleJson.optBoolean("isRecurring", false)
                                val recurringPatternRaw = scheduleJson.optString("recurringPattern", "")
                                val recurringPattern = recurringPatternRaw.ifBlank { null }
                                val reminderMinutes = scheduleJson.optInt("reminderMinutes", 15)

                                // Fallback current date approximation (UTC epoch days) since toLocalDateTime not available
                                val instantNow = kotlinx.datetime.Clock.System.now()
                                val epochDays = (instantNow.epochSeconds / 86400L).toInt()
                                val nowDate = kotlinx.datetime.LocalDate.fromEpochDays(epochDays)
                                val date = runCatching { if (dateStr.isNotBlank()) kotlinx.datetime.LocalDate.parse(dateStr) else nowDate }.getOrDefault(nowDate)
                                val startTime = runCatching { if (startStr.isNotBlank()) kotlinx.datetime.LocalTime.parse(startStr) else kotlinx.datetime.LocalTime(9, 0) }.getOrDefault(kotlinx.datetime.LocalTime(9,0))
                                val endTime = runCatching {
                                    if (endStr.isNotBlank()) kotlinx.datetime.LocalTime.parse(endStr) else {
                                        val hour = (startTime.hour + 1).coerceAtMost(23)
                                        kotlinx.datetime.LocalTime(hour, startTime.minute)
                                    }
                                }.getOrDefault(kotlinx.datetime.LocalTime((startTime.hour + 1).coerceAtMost(23), startTime.minute))

                                schedulesViewModel.createSchedule(
                                    title = title,
                                    description = description,
                                    startTime = startTime,
                                    endTime = endTime,
                                    date = date,
                                    location = location,
                                    category = category.ifBlank { "General" },
                                    isRecurring = isRecurring,
                                    recurringPattern = recurringPattern,
                                    reminderMinutes = reminderMinutes
                                )
                            }
                        }
                    }
                }

                // Routines
                json.optJSONArray("routines")?.let { arr ->
                    if (routinesViewModel != null) {
                        for (i in 0 until arr.length()) {
                            runCatching {
                                val routineJson = JSONObject(arr.getString(i))
                                val title = routineJson.optString("title", "")
                                if (title.isBlank()) return@runCatching
                                val description = routineJson.optString("description", "")
                                val frequency = routineJson.optString("frequency", "Daily")
                                routinesViewModel.createRoutine(title, description, frequency, null)
                            }
                        }
                    }
                }

                // Expenses
                json.optJSONArray("expenses")?.let { arr ->
                    if (expensesViewModel != null) {
                        for (i in 0 until arr.length()) {
                            runCatching {
                                val expenseJson = JSONObject(arr.getString(i))
                                val title = expenseJson.optString("title", "")
                                val amount = expenseJson.optDouble("amount", 0.0)
                                val category = expenseJson.optString("category", "General")
                                if (title.isNotBlank() && amount > 0) {
                                    expensesViewModel.createExpense(title, amount, category)
                                }
                            }
                        }
                    }
                }

                // Categories
                json.optJSONArray("categories")?.let { arr ->
                    if (expensesViewModel != null) {
                        for (i in 0 until arr.length()) {
                            runCatching {
                                val categoryJson = JSONObject(arr.getString(i))
                                val name = categoryJson.optString("name", "")
                                if (name.isBlank()) return@runCatching
                                val iconName = categoryJson.optString("iconName", "ShoppingCart")
                                val colorHex = categoryJson.optString("colorHex", "#96CEB4")
                                val monthlyBudget = categoryJson.optDouble("monthlyBudget", 0.0)
                                expensesViewModel.createCategory(name, iconName, colorHex, monthlyBudget)
                            }
                        }
                    }
                }
            }
            onResult(true, "Data imported successfully!")
        } catch (e: Exception) {
            onResult(false, "Failed to import data: ${e.message}")
        }
    }

    private inline fun <T> safeCollectList(block: () -> List<T>): List<T> = try { block() } catch (_: Exception) { emptyList() }
}
