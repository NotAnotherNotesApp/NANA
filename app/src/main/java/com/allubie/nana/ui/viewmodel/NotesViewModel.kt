package com.allubie.nana.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.allubie.nana.data.entity.NoteEntity
import com.allubie.nana.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    val notes = repository.getAllNotes()
    val archivedNotes = repository.getArchivedNotes()
    val deletedNotes = repository.getDeletedNotes()
    
    private var searchJob: kotlinx.coroutines.Job? = null
    fun searchNotes(query: String) {
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(searchQuery = "", searchResults = emptyList())
            return
        }
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Simple debounce
            kotlinx.coroutines.delay(250)
            repository.searchNotes(query).collect { results ->
                _uiState.value = _uiState.value.copy(searchResults = results)
            }
        }
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", searchResults = emptyList())
    }
    
    fun createNote(title: String, content: String, category: String? = null, richContent: String = "", htmlContent: String = "", noteType: String = "text") {
        viewModelScope.launch {
            repository.createNote(title, content, category, richContent, htmlContent, noteType)
        }
    }
    
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = kotlinx.datetime.Clock.System.now()))
        }
    }
    
    fun updateNoteContent(noteId: String, title: String, content: String, richContent: String, htmlContent: String, noteType: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            note?.let {
                val updatedNote = it.copy(
                    title = title,
                    content = content,
                    richContent = richContent,
                    htmlContent = htmlContent,
                    noteType = noteType,
                    updatedAt = kotlinx.datetime.Clock.System.now()
                )
                repository.updateNote(updatedNote)
            }
        }
    }
    
    fun getNoteById(id: Long, callback: (NoteEntity?) -> Unit) {
        viewModelScope.launch {
            val note = repository.getNoteById(id.toString())
            callback(note)
        }
    }
    
    fun getNoteByStringId(id: String, callback: (NoteEntity?) -> Unit) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            callback(note)
        }
    }
    
    fun togglePin(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) {
                repository.unpinNote(noteId)
            } else {
                repository.pinNote(noteId)
            }
        }
    }
    
    fun moveToTrash(noteId: String) {
        viewModelScope.launch {
            repository.moveToTrash(noteId)
        }
    }
    
    fun restoreFromTrash(noteId: String) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }
    
    fun toggleArchive(noteId: String, isArchived: Boolean) {
        viewModelScope.launch {
            if (isArchived) {
                repository.unarchiveNote(noteId)
            } else {
                repository.archiveNote(noteId)
            }
        }
    }
    
    fun archiveNote(noteId: String) {
        viewModelScope.launch {
            repository.archiveNote(noteId)
        }
    }
    
    fun deleteNotePermanently(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
    
    fun setViewMode(isGridView: Boolean) {
        _uiState.value = _uiState.value.copy(isGridView = isGridView)
    }
}

data class NotesUiState(
    val searchQuery: String = "",
    val searchResults: List<NoteEntity> = emptyList(),
    val isGridView: Boolean = true,
    val selectedCategory: String? = null
)

class NotesViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
