package com.allubie.nana.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.model.isScheduledFor
import com.allubie.nana.data.model.Routine
import com.allubie.nana.data.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class RoutineStatistics(
    val weeklyCompletionRate: Int = 0,
    val monthlyCompletionRate: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyData: List<Float> = List(7) { 0f },
    val routineStats: List<RoutineStat> = emptyList()
)

data class RoutineStat(
    val name: String,
    val completionRate: Int,
    val streak: Int
)

class RoutineStatisticsViewModel(
    private val routineRepository: RoutineRepository
) : ViewModel() {
    
    private val _statistics = MutableStateFlow(RoutineStatistics())
    val statistics: StateFlow<RoutineStatistics> = _statistics.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val today = dateFormat.format(calendar.time)
            
            // Get start of week
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startOfWeek = dateFormat.format(calendar.time)
            
            // Get start of month
            calendar.time = Date()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = dateFormat.format(calendar.time)
            
            combine(
                routineRepository.getActiveRoutines(),
                routineRepository.getCompletionsInRange(startOfWeek, today),
                routineRepository.getCompletionsInRange(startOfMonth, today)
            ) { routines, weekCompletions, monthCompletions ->
                val totalRoutines = routines.size
                if (totalRoutines == 0) return@combine RoutineStatistics()
                
                // Calculate weekly data
                val weeklyData = mutableListOf<Float>()
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                
                var scheduledInWeekCount = 0
                var completedInWeekCount = 0

                for (i in 0..6) {
                    val dayString = dateFormat.format(cal.time)
                    val scheduledOnDay = routines.filter { it.isScheduledFor(cal.time) }
                    val completedOnDay = weekCompletions.count { it.date == dayString }
                    
                    scheduledInWeekCount += scheduledOnDay.size
                    completedInWeekCount += completedOnDay

                    val rate = if (scheduledOnDay.isNotEmpty()) {
                        (completedOnDay.toFloat() / scheduledOnDay.size).coerceAtMost(1f)
                    } else {
                        0f
                    }
                    weeklyData.add(rate)
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                // Calculate per-routine stats
                val routineStats = routines.map { routine ->
                    val completions = monthCompletions.filter { it.routineId == routine.id }
                    
                    // Count how many days this routine was scheduled in past 30 days
                    val past30Cal = Calendar.getInstance()
                    var scheduledDaysInMonth = 0
                    for (d in 0..29) {
                        if (routine.isScheduledFor(past30Cal.time)) {
                            scheduledDaysInMonth++
                        }
                        past30Cal.add(Calendar.DAY_OF_MONTH, -1)
                    }

                    val rate = if (scheduledDaysInMonth > 0) {
                        (completions.size * 100 / scheduledDaysInMonth).coerceAtMost(100)
                    } else 0

                    RoutineStat(
                        name = routine.title,
                        completionRate = rate,
                        streak = calculateStreak(routine, monthCompletions.filter { it.routineId == routine.id })
                    )
                }
                
                val weeklyRate = if (scheduledInWeekCount > 0) {
                    (completedInWeekCount * 100 / scheduledInWeekCount).coerceAtMost(100)
                } else 0

                var scheduledInMonthCount = 0
                val monthCal = Calendar.getInstance()
                for (d in 0..29) {
                    scheduledInMonthCount += routines.count { it.isScheduledFor(monthCal.time) }
                    monthCal.add(Calendar.DAY_OF_MONTH, -1)
                }
                val monthlyRate = if (scheduledInMonthCount > 0) {
                    (monthCompletions.size * 100 / scheduledInMonthCount).coerceAtMost(100)
                } else 0

                RoutineStatistics(
                    weeklyCompletionRate = weeklyRate,
                    monthlyCompletionRate = monthlyRate,
                    currentStreak = calculateOverallStreak(routines, monthCompletions),
                    longestStreak = calculateLongestStreak(routines, monthCompletions),
                    weeklyData = weeklyData,
                    routineStats = routineStats
                )
            }.collect { stats ->
                _statistics.value = stats
            }
        }
    }
    
    private fun calculateStreak(routine: Routine, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (completions.isEmpty()) return 0
        
        val completedDates = completions.map { it.date }.toSet()
        var streak = 0
        val calendar = Calendar.getInstance()
        
        for (i in 0..60) {
            val expectedDate = dateFormat.format(calendar.time)
            if (routine.isScheduledFor(calendar.time)) {
                if (completedDates.contains(expectedDate)) {
                    streak++
                } else {
                    break
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return streak
    }
    
    private fun calculateOverallStreak(routines: List<Routine>, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (routines.isEmpty() || completions.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        
        for (i in 0..30) {
            val dateString = dateFormat.format(calendar.time)
            val scheduledOnDay = routines.filter { it.isScheduledFor(calendar.time) }.map { it.id }.toSet()
            
            if (scheduledOnDay.isNotEmpty()) {
                val completedRoutines = completions.filter { it.date == dateString }.map { it.routineId }.toSet()
                if (completedRoutines.containsAll(scheduledOnDay)) {
                    streak++
                } else {
                    break
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return streak
    }
    
    private fun calculateLongestStreak(routines: List<Routine>, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (routines.isEmpty() || completions.isEmpty()) return 0
        
        val allDates = completions.map { it.date }.distinct().sorted()
        if (allDates.isEmpty()) return 0
        
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: Date? = null
        
        for (dateString in allDates) {
            val currentDate = dateFormat.parse(dateString) ?: continue
            val scheduledOnDay = routines.filter { it.isScheduledFor(currentDate) }.map { it.id }.toSet()
            val completedRoutines = completions.filter { it.date == dateString }.map { it.routineId }.toSet()
            
            if (scheduledOnDay.isNotEmpty() && completedRoutines.containsAll(scheduledOnDay)) {
                if (previousDate == null) {
                    currentStreak = 1
                } else {
                    val diffDays = (currentDate.time - previousDate.time) / (24 * 60 * 60 * 1000)
                    currentStreak = if (diffDays == 1L) currentStreak + 1 else 1
                }
                longestStreak = maxOf(longestStreak, currentStreak)
                previousDate = currentDate
            } else if (scheduledOnDay.isNotEmpty()) {
                currentStreak = 0
                previousDate = null
            }
        }
        
        return longestStreak
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NanaApplication
                val routineRepository = RoutineRepository(
                    application.database.routineDao(),
                    application.database.routineCompletionDao()
                )
                RoutineStatisticsViewModel(
                    routineRepository
                )
            }
        }
    }
}
