package com.allubie.nana.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.model.Event
import com.allubie.nana.data.repository.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ScheduleViewModel(
    private val eventRepository: EventRepository,
    preferencesManager: PreferencesManager
) : ViewModel() {
    
    data class ScheduleUiState(
        val selectedDate: Long = System.currentTimeMillis(),
        val isLoading: Boolean = true,
        val use24HourFormat: Boolean = false,
        val eventsForSelectedDay: List<Event> = emptyList()
    )
    
    private val _selectedDate = MutableStateFlow(Date())
    private val _isLoading = MutableStateFlow(true)
    private val _use24HourFormat = preferencesManager.use24HourFormat
    
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _eventsForSelectedDay = _selectedDate.flatMapLatest { date ->
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        
        eventRepository.getEventsForDay(startOfDay, endOfDay)
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }

    val uiState: StateFlow<ScheduleUiState> = combine(
        _selectedDate, _isLoading, _use24HourFormat, _eventsForSelectedDay
    ) { date, loading, use24, events ->
        ScheduleUiState(
            selectedDate = date.time,
            isLoading = loading,
            use24HourFormat = use24,
            eventsForSelectedDay = events
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState())
    
    fun selectDate(date: Date) {
        _selectedDate.value = date
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
        }
    }
    
    fun togglePin(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event.copy(isPinned = !event.isPinned))
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                val eventRepository = EventRepository(application.database.eventDao())
                ScheduleViewModel(
                    eventRepository,
                    application.preferencesManager
                )
            }
        }
    }
}
