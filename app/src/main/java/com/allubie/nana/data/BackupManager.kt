package com.allubie.nana.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.room.withTransaction
import com.allubie.nana.data.model.*
import com.allubie.nana.widget.updateAllWidgets
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
            // Read all data in a single transaction for a consistent snapshot
            val backupData = database.withTransaction {
                val notes = database.noteDao().getAllNotesSync()
                val noteImages = database.noteImageDao().getAllImagesSync()
                val checklistItems = database.checklistItemDao().getAllItemsSync()
                val events = database.eventDao().getAllEventsSync()
                val routines = database.routineDao().getAllRoutinesSync()
                val routineCompletions = database.routineCompletionDao().getAllCompletionsSync()
                val transactions = database.transactionDao().getAllTransactionsSync()
                val budgets = database.budgetDao().getAllBudgetsSync()
                val labels = database.labelDao().getAllLabelsSync()

                BackupData(
                    notes = notes,
                    noteImages = noteImages,
                    checklistItems = checklistItems,
                    events = events,
                    routines = routines,
                    routineCompletions = routineCompletions,
                    transactions = transactions,
                    budgets = budgets,
                    labels = labels
                )
            }
            
            // Export preferences (outside transaction — DataStore is separate)
            val prefs = BackupPreferences(
                themeMode = preferencesManager.themeMode.first().name.lowercase(),
                currencyCode = preferencesManager.currencyCode.first(),
                currencySymbol = preferencesManager.currencySymbol.first(),
                timezone = preferencesManager.timezone.first(),
                use24HourFormat = preferencesManager.use24HourFormat.first()
            )
            val backupDataWithPrefs = backupData.copy(preferences = prefs)
            
            val json = gson.toJson(backupDataWithPrefs)
            
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
            
            database.withTransaction {
                // Clear existing tables in dependent order to satisfy foreign keys
                database.noteImageDao().deleteAllImages()
                database.checklistItemDao().deleteAllItems()
                database.noteDao().deleteAllNotes()
                database.routineCompletionDao().deleteAllCompletions()
                database.routineDao().deleteAllRoutines()
                database.eventDao().deleteAllEvents()
                database.transactionDao().deleteAllTransactions()
                database.budgetDao().deleteAllBudgets()
                database.labelDao().deleteAllLabels()
                
                backupData.notes.forEach { database.noteDao().insertNote(it) }
                backupData.noteImages.forEach { database.noteImageDao().insertImage(it) }
                backupData.checklistItems.forEach { database.checklistItemDao().insertItem(it) }
                backupData.events.forEach { database.eventDao().insertEvent(it) }
                backupData.routines.forEach { database.routineDao().insertRoutine(it) }
                backupData.routineCompletions.forEach { database.routineCompletionDao().insertCompletion(it) }
                backupData.transactions.forEach { database.transactionDao().insertTransaction(it) }
                backupData.budgets.forEach { database.budgetDao().insertBudget(it) }
                backupData.labels.forEach { database.labelDao().insertLabel(it) }
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
            
            updateAllWidgets(context)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
