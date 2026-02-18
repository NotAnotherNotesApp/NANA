package com.allubie.nana.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.allubie.nana.data.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BackupPreferences(
    val themeMode: String? = null,
    val currencyCode: String? = null,
    val currencySymbol: String? = null,
    val timezone: String? = null,
    val use24HourFormat: Boolean? = null
)

data class BackupData(
    val version: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: List<Note> = emptyList(),
    val noteImages: List<NoteImage> = emptyList(),
    val checklistItems: List<ChecklistItem> = emptyList(),
    val events: List<Event> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val routineCompletions: List<RoutineCompletion> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val labels: List<Label> = emptyList(),
    val preferences: BackupPreferences? = null
)

class BackupManager(
    private val context: Context,
    private val database: NanaDatabase,
    private val preferencesManager: PreferencesManager
) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()
    
    suspend fun exportBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val notes = database.noteDao().getAllNotesSync()
            val noteImages = database.noteImageDao().getAllImagesSync()
            val checklistItems = database.checklistItemDao().getAllItemsSync()
            val events = database.eventDao().getAllEventsSync()
            val routines = database.routineDao().getAllRoutinesSync()
            val routineCompletions = database.routineCompletionDao().getAllCompletionsSync()
            val transactions = database.transactionDao().getAllTransactionsSync()
            val budgets = database.budgetDao().getAllBudgetsSync()
            val labels = database.labelDao().getAllLabelsSync()
            
            // Export preferences
            val prefs = BackupPreferences(
                themeMode = preferencesManager.themeMode.first().name.lowercase(),
                currencyCode = preferencesManager.currencyCode.first(),
                currencySymbol = preferencesManager.currencySymbol.first(),
                timezone = preferencesManager.timezone.first(),
                use24HourFormat = preferencesManager.use24HourFormat.first()
            )
            
            val backupData = BackupData(
                notes = notes,
                noteImages = noteImages,
                checklistItems = checklistItems,
                events = events,
                routines = routines,
                routineCompletions = routineCompletions,
                transactions = transactions,
                budgets = budgets,
                labels = labels,
                preferences = prefs
            )
            
            val json = gson.toJson(backupData)
            
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "nana_backup_${dateFormat.format(Date())}.json"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupFile = File(downloadsDir, fileName)
            backupFile.writeText(json)
            
            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open file"))
            
            val json = inputStream.bufferedReader().use { it.readText() }
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            // Clear existing data (order matters for foreign keys)
            database.noteImageDao().deleteAllImages()
            database.checklistItemDao().deleteAllItems()
            database.noteDao().deleteAllNotes()
            database.routineCompletionDao().deleteAllCompletions()
            database.routineDao().deleteAllRoutines()
            database.eventDao().deleteAllEvents()
            database.transactionDao().deleteAllTransactions()
            database.budgetDao().deleteAllBudgets()
            database.labelDao().deleteAllLabels()
            
            // Import notes
            backupData.notes.forEach { note ->
                database.noteDao().insertNote(note)
            }
            
            // Import note images
            backupData.noteImages.forEach { image ->
                database.noteImageDao().insertImage(image)
            }
            
            // Import checklist items
            backupData.checklistItems.forEach { item ->
                database.checklistItemDao().insertItem(item)
            }
            
            // Import events
            backupData.events.forEach { event ->
                database.eventDao().insertEvent(event)
            }
            
            // Import routines
            backupData.routines.forEach { routine ->
                database.routineDao().insertRoutine(routine)
            }
            
            // Import routine completions
            backupData.routineCompletions.forEach { completion ->
                database.routineCompletionDao().insertCompletion(completion)
            }
            
            // Import transactions
            backupData.transactions.forEach { transaction ->
                database.transactionDao().insertTransaction(transaction)
            }
            
            // Import budgets
            backupData.budgets.forEach { budget ->
                database.budgetDao().insertBudget(budget)
            }
            
            // Import labels
            backupData.labels.forEach { label ->
                database.labelDao().insertLabel(label)
            }
            
            // Restore preferences
            backupData.preferences?.let { prefs ->
                prefs.themeMode?.let { mode ->
                    val themeMode = when (mode) {
                        "light" -> com.allubie.nana.ui.theme.ThemeMode.LIGHT
                        "dark" -> com.allubie.nana.ui.theme.ThemeMode.DARK
                        "amoled" -> com.allubie.nana.ui.theme.ThemeMode.AMOLED
                        else -> com.allubie.nana.ui.theme.ThemeMode.SYSTEM
                    }
                    preferencesManager.setThemeMode(themeMode)
                }
                val code = prefs.currencyCode
                val symbol = prefs.currencySymbol
                if (code != null && symbol != null) {
                    preferencesManager.setCurrency(code, symbol)
                }
                prefs.timezone?.let { preferencesManager.setTimezone(it) }
                prefs.use24HourFormat?.let { preferencesManager.setUse24HourFormat(it) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
