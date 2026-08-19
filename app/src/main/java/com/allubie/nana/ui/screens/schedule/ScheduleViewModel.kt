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
import com.allubie.nana.util.occursOn
import com.allubie.nana.util.projectForDay
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
        val eventsForSelectedDay: List<Event> = emptyList(),
        val allEvents: List<Event> = emptyList()
    )
    
    private val _selectedDate = MutableStateFlow(Date())
    private val _isLoading = MutableStateFlow(true)
    private val _use24HourFormat = preferencesManager.use24HourFormat
    
    private val _allEvents = eventRepository.getAllEvents()
        .onStart { _isLoading.value = true }
        .onEach { _isLoading.value = false }

    val uiState: StateFlow<ScheduleUiState> = combine(
        _selectedDate, _isLoading, _use24HourFormat, _allEvents
    ) { date, loading, use24, allEvents ->
        val eventsForDay = allEvents
            .filter { it.occursOn(date.time) }
            .map { it.projectForDay(date.time) }
            .sortedBy { it.startTime }

        ScheduleUiState(
            selectedDate = date.time,
            isLoading = loading,
            use24HourFormat = use24,
            eventsForSelectedDay = eventsForDay,
            allEvents = allEvents
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
