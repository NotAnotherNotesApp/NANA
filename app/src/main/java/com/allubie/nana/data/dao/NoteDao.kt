package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotesFlow(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotesFlow(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotesFlow(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR richContent LIKE '%' || :searchQuery || '%' OR htmlContent LIKE '%' || :searchQuery || '%') AND isDeleted = 0 AND isArchived = 0")
    fun searchNotesFlow(searchQuery: String): Flow<List<NoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    
    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun moveToTrash(id: String)
    
    @Query("UPDATE notes SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: String)
    
    @Query("UPDATE notes SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)
    
    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)
    
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()
}
