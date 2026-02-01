package com.allubie.nana.ui.screens.notes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.LabelDao
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.dao.NoteImageDao
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.data.model.Note
import com.allubie.nana.data.model.NoteImage
import com.allubie.nana.data.repository.LabelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class NoteEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val colorIndex: Int = 0,
    val labels: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val images: List<NoteImage> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

class NoteEditorViewModel(
    private val noteDao: NoteDao,
    private val noteImageDao: NoteImageDao,
    private val labelDao: LabelDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()
    
    // Available note labels from database
    val availableLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.NOTE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val labelRepository = LabelRepository(labelDao)
    
    init {
        viewModelScope.launch {
            labelRepository.seedPresetsIfNeeded()
        }
    }
    
    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val note = noteDao.getNoteById(noteId)
            val images = noteImageDao.getImagesForNoteSync(noteId)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        colorIndex = note.color,
                        labels = note.labels.split(",").filter { label -> label.isNotBlank() },
                        isPinned = note.isPinned,
                        isLoading = false,
                        images = images,
                        updatedAt = note.updatedAt
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
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
    
    fun saveNote() {
        viewModelScope.launch {
            val state = _uiState.value
            val note = Note(
                id = state.id ?: 0,
                title = state.title,
                content = state.content,
                color = state.colorIndex,
                labels = state.labels.joinToString(","),
                isPinned = state.isPinned,
                updatedAt = System.currentTimeMillis()
            )
            val noteId = noteDao.insertNote(note)
            
            // Save images for new notes
            if (state.id == null) {
                state.images.forEach { image ->
                    noteImageDao.insertImage(image.copy(noteId = noteId))
                }
            }
        }
    }
    
    fun addImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Copy image to app storage
                val fileName = "img_${UUID.randomUUID()}.jpg"
                val imagesDir = File(context.filesDir, "note_images")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val destFile = File(imagesDir, fileName)
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val noteId = _uiState.value.id ?: 0
                val newImage = NoteImage(
                    noteId = noteId,
                    imagePath = destFile.absolutePath,
                    position = _uiState.value.images.size
                )
                
                // If note already exists, save immediately
                if (noteId > 0) {
                    noteImageDao.insertImage(newImage)
                }
                
                _uiState.update { it.copy(images = it.images + newImage) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(errorMessage = "Failed to add image") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun removeImage(image: NoteImage) {
        viewModelScope.launch {
            // Delete from database if exists
            if (image.id > 0) {
                noteImageDao.deleteImage(image)
            }
            
            // Delete file
            val file = File(image.imagePath)
            if (file.exists()) file.delete()
            
            _uiState.update { it.copy(images = it.images - image) }
        }
    }
    
    fun archiveNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            if (noteId != null) {
                noteDao.updateArchiveStatus(noteId, true)
                onComplete()
            }
        }
    }
    
    fun unarchiveNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            if (noteId != null) {
                noteDao.updateArchiveStatus(noteId, false)
                onComplete()
            }
        }
    }
    
    fun deleteNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            if (noteId != null) {
                noteDao.updateDeleteStatus(noteId, true)
                onComplete()
            }
        }
    }
    
    fun togglePin() {
        viewModelScope.launch {
            val noteId = _uiState.value.id
            val newPinState = !_uiState.value.isPinned
            if (noteId != null) {
                noteDao.updatePinStatus(noteId, newPinState)
                _uiState.update { it.copy(isPinned = newPinState) }
            }
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                NoteEditorViewModel(
                    application.database.noteDao(),
                    application.database.noteImageDao(),
                    application.database.labelDao()
                )
            }
        }
    }
}
