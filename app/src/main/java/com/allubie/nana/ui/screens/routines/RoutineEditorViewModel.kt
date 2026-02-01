package com.allubie.nana.ui.screens.routines

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.RoutineDao
import com.allubie.nana.data.model.Routine
import com.allubie.nana.data.model.RoutineType
import com.allubie.nana.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutineEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val description: String = "",
    val iconName: String = "check_circle",
    val reminderTime: String? = null,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val isLoading: Boolean = false,
    val routineType: RoutineType = RoutineType.SIMPLE,
    val targetCount: Int = 1,
    val durationMinutes: Int = 5
)

class RoutineEditorViewModel(
    private val routineDao: RoutineDao,
    private val applicationContext: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()
    
    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val routine = routineDao.getRoutineById(routineId)
            if (routine != null) {
                _uiState.update {
                    it.copy(
                        id = routine.id,
                        title = routine.title,
                        description = routine.description,
                        iconName = routine.iconName,
                        reminderTime = routine.reminderTime,
                        selectedDays = routine.daysOfWeek.split(",")
                            .mapNotNull { d -> d.toIntOrNull() }.toSet(),
                        isLoading = false,
                        routineType = RoutineType.valueOf(routine.routineType),
                        targetCount = routine.targetCount,
                        durationMinutes = routine.durationMinutes
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
    
    fun updateIconName(iconName: String) {
        _uiState.update { it.copy(iconName = iconName) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updateReminderTime(time: String?) {
        _uiState.update { it.copy(reminderTime = time) }
    }
    
    fun updateRoutineType(type: RoutineType) {
        _uiState.update { it.copy(routineType = type) }
    }
    
    fun updateTargetCount(count: Int) {
        _uiState.update { it.copy(targetCount = count.coerceAtLeast(1)) }
    }
    
    fun updateDurationMinutes(minutes: Int) {
        _uiState.update { it.copy(durationMinutes = minutes.coerceAtLeast(1)) }
    }
    
    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val newDays = if (state.selectedDays.contains(day)) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = newDays)
        }
    }
    
    fun saveRoutine() {
        viewModelScope.launch {
            val state = _uiState.value
            val routine = Routine(
                id = state.id ?: 0,
                title = state.title,
                description = state.description,
                iconName = state.iconName,
                reminderTime = state.reminderTime,
                daysOfWeek = state.selectedDays.sorted().joinToString(","),
                scheduledDays = state.selectedDays.map { it - 1 }.sorted().joinToString(","),
                routineType = state.routineType.name,
                targetCount = state.targetCount,
                durationMinutes = state.durationMinutes,
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = routineDao.insertRoutine(routine)
            
            // Schedule reminders for the routine
            val savedRoutine = if (state.id != null) routine else routine.copy(id = insertedId)
            if (savedRoutine.reminderTime != null) {
                ReminderScheduler.scheduleRoutineReminder(applicationContext, savedRoutine)
            }
        }
    }
    
    fun deleteRoutine() {
        viewModelScope.launch {
            _uiState.value.id?.let { id ->
                // Cancel scheduled reminders
                ReminderScheduler.cancelRoutineReminders(applicationContext, id)
                routineDao.deleteRoutineById(id)
            }
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                RoutineEditorViewModel(application.database.routineDao(), application.applicationContext)
            }
        }
    }
}
