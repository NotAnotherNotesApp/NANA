package com.allubie.nana.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(private val noteDao: NoteDao) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteDao.getAllNotes()
        } else {
            noteDao.searchNotes(query)
        }
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.updatePinStatus(note.id, !note.isPinned)
        }
    }
    
    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateArchiveStatus(note.id, true)
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateDeleteStatus(note.id, true)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                NotesViewModel(application.database.noteDao())
            }
        }
    }
}
