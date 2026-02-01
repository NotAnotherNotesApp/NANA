package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    
    @Query("SELECT * FROM labels WHERE isHidden = 0 ORDER BY sortOrder ASC")
    fun getAllLabels(): Flow<List<Label>>
    
    @Query("SELECT * FROM labels WHERE type = :type AND isHidden = 0 ORDER BY sortOrder ASC")
    fun getLabelsByType(type: LabelType): Flow<List<Label>>
    
    @Query("SELECT * FROM labels WHERE type = :type AND isHidden = 0 ORDER BY sortOrder ASC")
    suspend fun getLabelsByTypeSync(type: LabelType): List<Label>
    
    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getLabelById(id: Long): Label?
    
    @Query("SELECT * FROM labels WHERE name = :name AND type = :type AND isHidden = 0 LIMIT 1")
    suspend fun getLabelByNameAndType(name: String, type: LabelType): Label?
    
    @Query("SELECT COUNT(*) FROM labels")
    suspend fun getLabelCount(): Int
    
    @Query("SELECT MAX(sortOrder) FROM labels WHERE type = :type")
    suspend fun getMaxSortOrder(type: LabelType): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(label: Label): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabels(labels: List<Label>)
    
    @Update
    suspend fun updateLabel(label: Label)
    
    @Query("UPDATE labels SET isHidden = 1 WHERE id = :id")
    suspend fun hideLabel(id: Long)
    
    @Query("UPDATE labels SET isHidden = 0 WHERE id = :id")
    suspend fun unhideLabel(id: Long)
    
    @Delete
    suspend fun deleteLabel(label: Label)
    
    @Query("DELETE FROM labels WHERE id = :id AND isPreset = 0")
    suspend fun deleteLabelById(id: Long)
    
    @Query("UPDATE labels SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
}
