package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.ChecklistItem
import com.allubie.nana.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?
    
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isDeleted = 0")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE notes SET isDeleted = :isDeleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDeleteStatus(id: Long, isDeleted: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSync(): List<Note>
    
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}

@Dao
interface ChecklistItemDao {
    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY position ASC")
    fun getItemsForNote(noteId: Long): Flow<List<ChecklistItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItem>)
    
    @Update
    suspend fun updateItem(item: ChecklistItem)
    
    @Delete
    suspend fun deleteItem(item: ChecklistItem)
    
    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteItemsForNote(noteId: Long)
    
    @Query("SELECT * FROM checklist_items")
    suspend fun getAllItemsSync(): List<ChecklistItem>
    
    @Query("DELETE FROM checklist_items")
    suspend fun deleteAllItems()
}
