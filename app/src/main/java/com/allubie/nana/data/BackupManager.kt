package com.allubie.nana.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.allubie.nana.data.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BackupData(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: List<Note> = emptyList(),
    val noteImages: List<NoteImage> = emptyList(),
    val events: List<Event> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val routineCompletions: List<RoutineCompletion> = emptyList(),
    val transactions: List<Transaction> = emptyList()
)

class BackupManager(private val context: Context, private val database: NanaDatabase) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()
    
    suspend fun exportBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val notes = database.noteDao().getAllNotesSync()
            val noteImages = mutableListOf<NoteImage>()
            notes.forEach { note ->
                noteImages.addAll(database.noteImageDao().getImagesForNoteSync(note.id))
            }
            val events = database.eventDao().getAllEventsSync()
            val routines = database.routineDao().getAllRoutinesSync()
            val routineCompletions = database.routineCompletionDao().getAllCompletionsSync()
            val transactions = database.transactionDao().getAllTransactionsSync()
            
            val backupData = BackupData(
                notes = notes,
                noteImages = noteImages,
                events = events,
                routines = routines,
                routineCompletions = routineCompletions,
                transactions = transactions
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
            
            // Clear existing data
            database.noteDao().deleteAllNotes()
            database.eventDao().deleteAllEvents()
            database.routineDao().deleteAllRoutines()
            database.routineCompletionDao().deleteAllCompletions()
            database.transactionDao().deleteAllTransactions()
            
            // Import notes
            backupData.notes.forEach { note ->
                database.noteDao().insertNote(note)
            }
            
            // Import note images
            backupData.noteImages.forEach { image ->
                database.noteImageDao().insertImage(image)
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
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
