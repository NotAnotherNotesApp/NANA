package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.LabelDao
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.model.PresetLabels
import kotlinx.coroutines.flow.Flow

class LabelRepository(private val labelDao: LabelDao) {
    
    fun getAllLabels(): Flow<List<Label>> = labelDao.getAllLabels()
    
    fun getLabelsByType(type: LabelType): Flow<List<Label>> = labelDao.getLabelsByType(type)
    
    suspend fun getLabelsByTypeSync(type: LabelType): List<Label> = labelDao.getLabelsByTypeSync(type)
    
    suspend fun getLabelById(id: Long): Label? = labelDao.getLabelById(id)
    
    suspend fun getLabelByNameAndType(name: String, type: LabelType): Label? = 
        labelDao.getLabelByNameAndType(name, type)
    
    suspend fun insertLabel(label: Label): Long = labelDao.insertLabel(label)
    
    suspend fun updateLabel(label: Label) = labelDao.updateLabel(label)
    
    suspend fun hideLabel(id: Long) = labelDao.hideLabel(id)
    
    suspend fun deleteLabel(id: Long) = labelDao.deleteLabelById(id)
    
    suspend fun updateSortOrder(id: Long, sortOrder: Int) = labelDao.updateSortOrder(id, sortOrder)
    
    suspend fun createLabel(
        name: String,
        type: LabelType,
        iconName: String? = null,
        color: Int
    ): Long {
        val maxSortOrder = labelDao.getMaxSortOrder(type) ?: -1
        val label = Label(
            name = name,
            type = type,
            iconName = iconName,
            color = color,
            isPreset = false,
            sortOrder = maxSortOrder + 1
        )
        return labelDao.insertLabel(label)
    }
    
    /**
     * Seeds preset labels if the labels table is empty.
     * Should be called on app startup.
     */
    suspend fun seedPresetsIfNeeded() {
        val count = labelDao.getLabelCount()
        if (count == 0) {
            labelDao.insertLabels(PresetLabels.all)
        }
    }
}
