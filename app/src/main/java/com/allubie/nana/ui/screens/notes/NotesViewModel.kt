package com.allubie.nana.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.model.Note
import com.allubie.nana.data.repository.NoteRepository
import com.allubie.nana.widget.updateNotesWidgets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotesUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList()
)

class NotesViewModel(private val noteRepository: NoteRepository, private val application: NanaApplication) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notes = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            noteRepository.getAllNotes()
        } else {
            noteRepository.searchNotes(query)
        }
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }
     
    val uiState: StateFlow<NotesUiState> = combine(
        _searchQuery, _isLoading, _notes
    ) { query, loading, notesList ->
        NotesUiState(
            searchQuery = query,
            isLoading = loading,
            notes = notesList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteRepository.updatePinStatus(note.id, !note.isPinned)
            updateNotesWidgets(application)
        }
    }
    
    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateArchiveStatus(note.id, true)
            updateNotesWidgets(application)
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateDeleteStatus(note.id, true)
            updateNotesWidgets(application)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                val database = application.database
                val noteRepository = NoteRepository(
                    database.noteDao(),
                    database.noteImageDao(),
                    database.checklistItemDao()
                )
                NotesViewModel(noteRepository, application)
            }
        }
    }
}
