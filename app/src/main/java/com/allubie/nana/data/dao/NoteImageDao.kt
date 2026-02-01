package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.NoteImage
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteImageDao {
    @Query("SELECT * FROM note_images WHERE noteId = :noteId ORDER BY position ASC")
    fun getImagesForNote(noteId: Long): Flow<List<NoteImage>>
    
    @Query("SELECT * FROM note_images WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getImagesForNoteSync(noteId: Long): List<NoteImage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: NoteImage): Long
    
    @Delete
    suspend fun deleteImage(image: NoteImage)
    
    @Query("DELETE FROM note_images WHERE noteId = :noteId")
    suspend fun deleteImagesForNote(noteId: Long)
    
    @Query("DELETE FROM note_images WHERE id = :imageId")
    suspend fun deleteImageById(imageId: Long)
    
    @Query("SELECT * FROM note_images")
    suspend fun getAllImagesSync(): List<NoteImage>
    
    @Query("DELETE FROM note_images")
    suspend fun deleteAllImages()
}
