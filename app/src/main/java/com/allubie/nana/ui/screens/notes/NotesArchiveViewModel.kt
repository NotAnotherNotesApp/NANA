package com.allubie.nana.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotesArchiveViewModel(private val noteDao: NoteDao) : ViewModel() {
    
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    
    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateArchiveStatus(note.id, false)
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
                NotesArchiveViewModel(application.database.noteDao())
            }
        }
    }
}
