package com.allubie.nana.ui.screens.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.dao.EventDao
import com.allubie.nana.data.dao.LabelDao
import com.allubie.nana.data.model.Event
import com.allubie.nana.data.model.Label
import com.allubie.nana.data.model.LabelType
import com.allubie.nana.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "Event",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isAllDay: Boolean = false,
    val isPinned: Boolean = false,
    val reminderMinutes: List<Int> = listOf(15),
    val recurrenceRule: String? = null,
    val isLoading: Boolean = false
)

class ScheduleEditorViewModel(
    private val eventDao: EventDao,
    private val labelDao: LabelDao,
    private val applicationContext: Context,
    preferencesManager: PreferencesManager
) : ViewModel() {
    
    val use24HourFormat: StateFlow<Boolean> = preferencesManager.use24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val eventLabels: StateFlow<List<Label>> = labelDao.getLabelsByType(LabelType.EVENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _uiState = MutableStateFlow(ScheduleEditorUiState())
    val uiState: StateFlow<ScheduleEditorUiState> = _uiState.asStateFlow()
    
    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val event = eventDao.getEventById(eventId)
            if (event != null) {
                _uiState.update {
                    it.copy(
                        id = event.id,
                        title = event.title,
                        description = event.description,
                        location = event.location,
                        category = event.category,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        isAllDay = event.isAllDay,
                        isPinned = event.isPinned,
                        reminderMinutes = event.reminderMinutes.split(",")
                            .mapNotNull { m -> m.toIntOrNull() },
                        recurrenceRule = event.recurrenceRule,
                        isLoading = false
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
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }
    
    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    fun updateStartTime(time: Long) {
        _uiState.update { it.copy(startTime = time) }
    }
    
    fun updateEndTime(time: Long?) {
        _uiState.update { it.copy(endTime = time) }
    }
    
    fun updateAllDay(isAllDay: Boolean) {
        _uiState.update { it.copy(isAllDay = isAllDay) }
    }
    
    fun updateRecurrence(rule: String?) {
        _uiState.update { it.copy(recurrenceRule = rule) }
    }
    
    fun updateReminders(minutes: List<Int>) {
        _uiState.update { it.copy(reminderMinutes = minutes) }
    }
    
    fun saveEvent() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Cancel existing reminders if editing an event
            if (state.id != null) {
                ReminderScheduler.cancelEventReminders(
                    applicationContext,
                    state.id,
                    state.reminderMinutes
                )
            }
            
            val event = Event(
                id = state.id ?: 0,
                title = state.title,
                description = state.description,
                location = state.location,
                category = state.category,
                startTime = state.startTime,
                endTime = state.endTime,
                isAllDay = state.isAllDay,
                reminderMinutes = state.reminderMinutes.joinToString(","),
                recurrenceRule = state.recurrenceRule,
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = eventDao.insertEvent(event)
            
            // Schedule reminders for the event
            val savedEvent = if (state.id != null) event else event.copy(id = insertedId)
            ReminderScheduler.scheduleAllEventReminders(applicationContext, savedEvent)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                ScheduleEditorViewModel(
                    application.database.eventDao(),
                    application.database.labelDao(),
                    application.applicationContext,
                    application.preferencesManager
                )
            }
        }
    }
}
