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

class NotesTrashViewModel(private val noteDao: NoteDao) : ViewModel() {
    
    val deletedNotes: Flow<List<Note>> = noteDao.getDeletedNotes()
    
    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateDeleteStatus(note.id, false)
        }
    }
    
    fun permanentlyDeleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
    
    fun emptyTrash() {
        viewModelScope.launch {
            noteDao.emptyTrash()
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                NotesTrashViewModel(application.database.noteDao())
            }
        }
    }
}
