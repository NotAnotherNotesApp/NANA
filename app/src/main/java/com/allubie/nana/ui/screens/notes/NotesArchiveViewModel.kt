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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotesArchiveViewModel(private val noteRepository: NoteRepository, private val application: NanaApplication) : ViewModel() {
    
    val archivedNotes: Flow<List<Note>> = noteRepository.getArchivedNotes()
    
    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateArchiveStatus(note.id, false)
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
                NotesArchiveViewModel(noteRepository, application)
            }
        }
    }
}
