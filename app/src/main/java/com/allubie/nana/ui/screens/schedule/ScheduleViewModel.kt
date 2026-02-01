package com.allubie.nana.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.dao.EventDao
import com.allubie.nana.data.model.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ScheduleViewModel(
    private val eventDao: EventDao,
    preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val use24HourFormat: StateFlow<Boolean> = preferencesManager.use24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForSelectedDay: StateFlow<List<Event>> = _selectedDate.flatMapLatest { date ->
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
        
        eventDao.getEventsForDay(startOfDay, endOfDay)
    }.onStart { _isLoading.value = true }
     .onEach { _isLoading.value = false }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun selectDate(date: Date) {
        _selectedDate.value = date
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.deleteEvent(event)
        }
    }
    
    fun togglePin(event: Event) {
        viewModelScope.launch {
            eventDao.updateEvent(event.copy(isPinned = !event.isPinned))
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                ScheduleViewModel(
                    application.database.eventDao(),
                    application.preferencesManager
                )
            }
        }
    }
}
