package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.*
import com.allubie.nana.data.model.*
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val noteImageDao: NoteImageDao,
    private val checklistItemDao: ChecklistItemDao
) {
    // --- NoteDao ---
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    fun getRecentNonChecklistNotes(limit: Int = 3): Flow<List<Note>> = noteDao.getRecentNonChecklistNotes(limit)
    suspend fun getRecentNonChecklistNotesOnce(limit: Int = 3): List<Note> = noteDao.getRecentNonChecklistNotesOnce(limit)
    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()
    fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()
    fun getDeletedNotes(): Flow<List<Note>> = noteDao.getDeletedNotes()
    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    fun searchNotes(query: String): Flow<List<Note>> {
        val sanitizedQuery = query.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
        val ftsQuery = if (sanitizedQuery.isNotBlank()) {
            sanitizedQuery.split("\\s+".toRegex()).joinToString(" ") { "$it*" }
        } else {
            ""
        }
        return noteDao.searchNotes(ftsQuery)
    }
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun updatePinStatus(id: Long, isPinned: Boolean) = noteDao.updatePinStatus(id, isPinned)
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean) = noteDao.updateArchiveStatus(id, isArchived)
    suspend fun updateDeleteStatus(id: Long, isDeleted: Boolean) = noteDao.updateDeleteStatus(id, isDeleted)
    suspend fun emptyTrash() = noteDao.emptyTrash()
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    // --- NoteImageDao ---
    fun getImagesForNote(noteId: Long): Flow<List<NoteImage>> = noteImageDao.getImagesForNote(noteId)
    suspend fun getImagesForNoteSync(noteId: Long): List<NoteImage> = noteImageDao.getImagesForNoteSync(noteId)
    suspend fun insertImage(image: NoteImage): Long = noteImageDao.insertImage(image)
    suspend fun deleteImage(image: NoteImage) = noteImageDao.deleteImage(image)
    suspend fun deleteImagesForNote(noteId: Long) = noteImageDao.deleteImagesForNote(noteId)
    suspend fun deleteImageById(imageId: Long) = noteImageDao.deleteImageById(imageId)

    // --- ChecklistItemDao ---
    fun getItemsForNote(noteId: Long): Flow<List<ChecklistItem>> = checklistItemDao.getItemsForNote(noteId)
    suspend fun getItemById(id: Long): ChecklistItem? = checklistItemDao.getItemById(id)
    suspend fun insertItem(item: ChecklistItem): Long = checklistItemDao.insertItem(item)
    suspend fun insertItems(items: List<ChecklistItem>) = checklistItemDao.insertItems(items)
    suspend fun updateItem(item: ChecklistItem) = checklistItemDao.updateItem(item)
    suspend fun deleteItem(item: ChecklistItem) = checklistItemDao.deleteItem(item)
    suspend fun deleteItemsForNote(noteId: Long) = checklistItemDao.deleteItemsForNote(noteId)
}
