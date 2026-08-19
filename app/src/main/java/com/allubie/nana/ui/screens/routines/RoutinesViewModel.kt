package com.allubie.nana.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.PreferencesManager
import com.allubie.nana.data.model.Routine
import com.allubie.nana.data.model.isScheduledFor
import com.allubie.nana.data.model.RoutineCompletion
import com.allubie.nana.data.model.RoutineType
import com.allubie.nana.data.repository.RoutineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TimerState(
    val routineId: Long = 0,
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val targetSeconds: Int = 0,
    val startedOnDate: String = "" // Track the date when timer was started
)

class RoutinesViewModel(
    private val routineRepository: RoutineRepository,
    preferencesManager: PreferencesManager
) : ViewModel() {
    
    data class RoutinesUiState(
        val use24HourFormat: Boolean = false,
        val selectedDate: Date = Date(),
        val isLoading: Boolean = true,
        val routines: List<Routine> = emptyList(),
        val completedTodayIds: Set<Long> = emptySet(),
        val todayCompletions: Map<Long, RoutineCompletion> = emptyMap(),
        val timerState: TimerState = TimerState()
    )
    
    private val _use24HourFormat = preferencesManager.use24HourFormat
    
    private val _selectedDate = MutableStateFlow(Date())
    
    private val _routines = routineRepository.getActiveRoutines()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val _timerState = MutableStateFlow(TimerState())
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RoutinesUiState> = combine(
        _use24HourFormat,
        _selectedDate,
        _routines,
        _selectedDate.flatMapLatest { date ->
            val dateString = dateFormat.format(date)
            routineRepository.getCompletionsForDate(dateString)
        },
        _timerState
    ) { use24, selectedDate, allActiveRoutines, completionsForDate, timerState ->
        val routinesForDate = allActiveRoutines.filter { it.isScheduledFor(selectedDate) }
        val scheduledIds = routinesForDate.map { it.id }.toSet()
        val completedIds = completionsForDate
            .filter { it.isCompleted && it.routineId in scheduledIds }
            .map { it.routineId }
            .toSet()
        val completionsMap = completionsForDate.associateBy { it.routineId }

        RoutinesUiState(
            use24HourFormat = use24,
            selectedDate = selectedDate,
            isLoading = false,
            routines = routinesForDate,
            completedTodayIds = completedIds,
            todayCompletions = completionsMap,
            timerState = timerState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutinesUiState())
    
    fun selectDate(date: Date) {
        _selectedDate.value = date
    }
    
    // Helper to check if selected date is in the future
    private fun isSelectedDateFuture(): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        return _selectedDate.value.after(today.time)
    }
    
    fun toggleCompletion(routine: Routine) {
        // Prevent marking future dates
        if (isSelectedDateFuture()) return
        
        viewModelScope.launch {
            val dateString = dateFormat.format(_selectedDate.value)
            val existing = routineRepository.getCompletionForDate(routine.id, dateString)
            if (existing != null) {
                routineRepository.deleteCompletion(existing)
            } else {
                routineRepository.insertCompletion(
                    RoutineCompletion(
                        routineId = routine.id,
                        date = dateString,
                        isCompleted = true
                    )
                )
            }
        }
    }
    
    fun incrementCounter(routine: Routine) {
        // Prevent marking future dates
        if (isSelectedDateFuture()) return
        
        viewModelScope.launch {
            val dateString = dateFormat.format(_selectedDate.value)
            val existing = routineRepository.getCompletionForDate(routine.id, dateString)
            val currentCount = existing?.currentCount ?: 0
            
            // Don't increment beyond target count
            if (currentCount >= routine.targetCount) return@launch
            
            val newCount = currentCount + 1
            val isCompleted = newCount >= routine.targetCount
            
            routineRepository.insertCompletion(
                RoutineCompletion(
                    id = existing?.id ?: 0,
                    routineId = routine.id,
                    date = dateString,
                    isCompleted = isCompleted,
                    currentCount = newCount
                )
            )
        }
    }
    
    fun decrementCounter(routine: Routine) {
        // Prevent marking future dates
        if (isSelectedDateFuture()) return
        
        viewModelScope.launch {
            val dateString = dateFormat.format(_selectedDate.value)
            val existing = routineRepository.getCompletionForDate(routine.id, dateString)
            val currentCount = existing?.currentCount ?: 0
            if (currentCount > 0) {
                val newCount = currentCount - 1
                if (newCount == 0) {
                    existing?.let { routineRepository.deleteCompletion(it) }
                } else {
                    routineRepository.insertCompletion(
                        RoutineCompletion(
                            id = existing?.id ?: 0,
                            routineId = routine.id,
                            date = dateString,
                            isCompleted = false,
                            currentCount = newCount
                        )
                    )
                }
            }
        }
    }
    
    fun startTimer(routine: Routine) {
        // Prevent starting timer for future dates
        if (isSelectedDateFuture()) return
        if (_timerState.value.isRunning) return
        
        viewModelScope.launch {
            val dateString = dateFormat.format(_selectedDate.value)
            val existing = routineRepository.getCompletionForDate(routine.id, dateString)
            val startSeconds = existing?.elapsedSeconds ?: 0
            val targetSeconds = routine.durationMinutes * 60
            
            _timerState.value = TimerState(
                routineId = routine.id,
                isRunning = true,
                elapsedSeconds = startSeconds,
                targetSeconds = targetSeconds,
                startedOnDate = dateString // Store the date when timer started
            )
            
            while (_timerState.value.isRunning && _timerState.value.elapsedSeconds < targetSeconds) {
                delay(1000)
                _timerState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                
                // Save progress periodically
                if (_timerState.value.elapsedSeconds % 5 == 0) {
                    saveTimerProgress(routine.id, _timerState.value.elapsedSeconds, targetSeconds)
                }
            }
            
            // Timer completed
            if (_timerState.value.elapsedSeconds >= targetSeconds) {
                saveTimerProgress(routine.id, targetSeconds, targetSeconds)
            }
            
            _timerState.update { it.copy(isRunning = false) }
        }
    }
    
    fun pauseTimer() {
        viewModelScope.launch {
            val state = _timerState.value
            if (state.isRunning) {
                _timerState.update { it.copy(isRunning = false) }
                saveTimerProgress(state.routineId, state.elapsedSeconds, state.targetSeconds)
            }
        }
    }
    
    fun resetTimer(routine: Routine) {
        viewModelScope.launch {
            _timerState.value = TimerState()
            val dateString = dateFormat.format(_selectedDate.value)
            val existing = routineRepository.getCompletionForDate(routine.id, dateString)
            existing?.let { routineRepository.deleteCompletion(it) }
        }
    }
    
    private suspend fun saveTimerProgress(routineId: Long, elapsedSeconds: Int, targetSeconds: Int) {
        // Use the date when timer was started, not the currently selected date
        val dateString = _timerState.value.startedOnDate.ifEmpty { dateFormat.format(_selectedDate.value) }
        val existing = routineRepository.getCompletionForDate(routineId, dateString)
        routineRepository.insertCompletion(
            RoutineCompletion(
                id = existing?.id ?: 0,
                routineId = routineId,
                date = dateString,
                isCompleted = elapsedSeconds >= targetSeconds,
                elapsedSeconds = elapsedSeconds
            )
        )
    }
    
    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                val routineRepository = RoutineRepository(
                    application.database.routineDao(),
                    application.database.routineCompletionDao()
                )
                RoutinesViewModel(
                    routineRepository,
                    application.preferencesManager
                )
            }
        }
    }
}
