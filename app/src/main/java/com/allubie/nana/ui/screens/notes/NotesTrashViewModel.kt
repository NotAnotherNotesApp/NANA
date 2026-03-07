package com.allubie.nana.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.model.Note
import com.allubie.nana.widget.updateNotesWidgets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesTrashViewModel(private val noteDao: NoteDao, private val application: NanaApplication) : ViewModel() {
    
    val deletedNotes: Flow<List<Note>> = noteDao.getDeletedNotes()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    fun clearMessage() {
        _message.value = null
    }
    
    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteDao.updateDeleteStatus(note.id, false)
            updateNotesWidgets(application)
            _message.value = "Note restored"
        }
    }
    
    fun permanentlyDeleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
            updateNotesWidgets(application)
            _message.value = "Note permanently deleted"
        }
    }
    
    fun emptyTrash() {
        viewModelScope.launch {
            noteDao.emptyTrash()
            updateNotesWidgets(application)
            _message.value = "Trash emptied"
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                NotesTrashViewModel(application.database.noteDao(), application)
            }
        }
    }
}
