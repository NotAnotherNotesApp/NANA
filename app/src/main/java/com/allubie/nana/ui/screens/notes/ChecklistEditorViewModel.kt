package com.allubie.nana.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.ChecklistItemDao
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.model.ChecklistItem
import com.allubie.nana.data.model.Note
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val colorIndex: Int = 0,
    val labels: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

class ChecklistEditorViewModel(
    private val noteDao: NoteDao,
    private val checklistItemDao: ChecklistItemDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChecklistEditorUiState())
    val uiState: StateFlow<ChecklistEditorUiState> = _uiState.asStateFlow()
    
    private var itemsCollectionJob: Job? = null
    
    fun loadChecklist(noteId: Long) {
        // Cancel any existing collection to prevent memory leaks
        itemsCollectionJob?.cancel()
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val note = noteDao.getNoteById(noteId)
            if (note != null && note.isChecklist) {
                // Store note data first
                _uiState.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        colorIndex = note.color,
                        labels = note.labels.split(",").filter { label -> label.isNotBlank() },
                        isPinned = note.isPinned,
                        updatedAt = note.updatedAt
                    )
                }
                
                // Collect items in a cancellable job
                itemsCollectionJob = viewModelScope.launch {
                    checklistItemDao.getItemsForNote(noteId).collect { items ->
                        _uiState.update { it.copy(items = items, isLoading = false) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        itemsCollectionJob?.cancel()
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun addItem(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val noteId = _uiState.value.id
            val newPosition = _uiState.value.items.size
            val newItem = ChecklistItem(
                noteId = noteId ?: 0,
                text = text,
                isChecked = false,
                position = newPosition
            )
            
            if (noteId != null && noteId > 0) {
                // Note already exists, save item immediately
                val itemId = checklistItemDao.insertItem(newItem)
                _uiState.update { 
                    it.copy(items = it.items + newItem.copy(id = itemId))
                }
            } else {
                // Note doesn't exist yet, just add to local state
                _uiState.update { 
                    it.copy(items = it.items + newItem)
                }
            }
        }
    }
    
    fun updateItemText(item: ChecklistItem, newText: String) {
        viewModelScope.launch {
            val updatedItem = item.copy(text = newText)
            if (item.id > 0) {
                checklistItemDao.updateItem(updatedItem)
            }
            _uiState.update { state ->
                state.copy(
                    items = state.items.map { 
                        if (it.id == item.id && it.position == item.position) updatedItem else it 
                    }
                )
            }
        }
    }
    
    fun toggleItemChecked(item: ChecklistItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            if (item.id > 0) {
                checklistItemDao.updateItem(updatedItem)
            }
            _uiState.update { state ->
                state.copy(
                    items = state.items.map { 
                        if (it.id == item.id && it.position == item.position) updatedItem else it 
                    }
                )
            }
        }
    }
    
    fun deleteItem(item: ChecklistItem) {
        viewModelScope.launch {
            if (item.id > 0) {
                checklistItemDao.deleteItem(item)
            }
            _uiState.update { state ->
                state.copy(items = state.items.filter { it.id != item.id || it.position != item.position })
            }
        }
    }
    
    fun moveItem(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()
            if (fromIndex in currentItems.indices && toIndex in currentItems.indices) {
                val item = currentItems.removeAt(fromIndex)
                currentItems.add(toIndex, item)
                
                // Update positions
                val updatedItems = currentItems.mapIndexed { index, checklistItem ->
                    checklistItem.copy(position = index)
                }
                
                _uiState.update { it.copy(items = updatedItems) }
                
                // Persist position changes
                updatedItems.forEach { item ->
                    if (item.id > 0) {
                        checklistItemDao.updateItem(item)
                    }
                }
            }
        }
    }
    
    fun updateColor(colorIndex: Int) {
        _uiState.update { it.copy(colorIndex = colorIndex) }
    }
    
    fun addLabel(label: String) {
        if (label.isNotBlank() && !_uiState.value.labels.contains(label)) {
            _uiState.update { it.copy(labels = it.labels + label) }
        }
    }
    
    fun removeLabel(label: String) {
        _uiState.update { it.copy(labels = it.labels - label) }
    }
    
    fun togglePin() {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            val newPinState = !_uiState.value.isPinned
            if (noteId != null) {
                noteDao.updatePinStatus(noteId, newPinState)
            }
            _uiState.update { it.copy(isPinned = newPinState) }
        }
    }
    
    fun saveChecklist() {
        viewModelScope.launch {
            val state = _uiState.value
            val note = Note(
                id = state.id ?: 0,
                title = state.title.ifBlank { "Checklist" },
                content = "", // Checklists don't use content field
                color = state.colorIndex,
                labels = state.labels.joinToString(","),
                isPinned = state.isPinned,
                isChecklist = true,
                updatedAt = System.currentTimeMillis()
            )
            val noteId = noteDao.insertNote(note)
            
            // Save all items with the note ID
            if (state.id == null) {
                // New checklist - save all items
                state.items.forEachIndexed { index, item ->
                    checklistItemDao.insertItem(
                        item.copy(noteId = noteId, position = index)
                    )
                }
            }
            
            _uiState.update { it.copy(id = noteId) }
        }
    }
    
    fun archiveChecklist(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            if (noteId != null) {
                noteDao.updateArchiveStatus(noteId, true)
                onComplete()
            }
        }
    }
    
    fun deleteChecklist(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            if (noteId != null) {
                noteDao.updateDeleteStatus(noteId, true)
                onComplete()
            }
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                ChecklistEditorViewModel(
                    application.database.noteDao(),
                    application.database.checklistItemDao()
                )
            }
        }
    }
}
