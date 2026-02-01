package com.allubie.nana.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.allubie.nana.NanaApplication
import com.allubie.nana.data.dao.RoutineCompletionDao
import com.allubie.nana.data.dao.RoutineDao
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
    private val routineDao: RoutineDao,
    private val completionDao: RoutineCompletionDao
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
                routineDao.getActiveRoutines(),
                completionDao.getCompletionsInRange(startOfWeek, today),
                completionDao.getCompletionsInRange(startOfMonth, today)
            ) { routines, weekCompletions, monthCompletions ->
                val totalRoutines = routines.size
                if (totalRoutines == 0) return@combine RoutineStatistics()
                
                // Calculate weekly data
                val weeklyData = mutableListOf<Float>()
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                
                for (i in 0..6) {
                    val dayString = dateFormat.format(cal.time)
                    val completedOnDay = weekCompletions.count { it.date == dayString }
                    weeklyData.add(completedOnDay.toFloat() / totalRoutines)
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                // Calculate per-routine stats
                val routineStats = routines.map { routine ->
                    val completions = monthCompletions.filter { it.routineId == routine.id }
                    RoutineStat(
                        name = routine.title,
                        completionRate = if (30 > 0) (completions.size * 100 / 30).coerceAtMost(100) else 0,
                        streak = calculateStreak(routine.id, monthCompletions.filter { it.routineId == routine.id })
                    )
                }
                
                RoutineStatistics(
                    weeklyCompletionRate = if (totalRoutines > 0) 
                        (weekCompletions.distinctBy { "${it.routineId}-${it.date}" }.size * 100 / (totalRoutines * 7)).coerceAtMost(100) 
                        else 0,
                    monthlyCompletionRate = if (totalRoutines > 0) 
                        (monthCompletions.distinctBy { "${it.routineId}-${it.date}" }.size * 100 / (totalRoutines * 30)).coerceAtMost(100) 
                        else 0,
                    currentStreak = calculateOverallStreak(routines.map { it.id }.toSet(), weekCompletions),
                    longestStreak = calculateLongestStreak(routines.map { it.id }.toSet(), monthCompletions),
                    weeklyData = weeklyData,
                    routineStats = routineStats
                )
            }.collect { stats ->
                _statistics.value = stats
            }
        }
    }
    
    private fun calculateStreak(routineId: Long, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (completions.isEmpty()) return 0
        
        val sortedDates = completions.map { it.date }.sorted().reversed()
        var streak = 0
        val calendar = Calendar.getInstance()
        
        for (i in sortedDates.indices) {
            val expectedDate = dateFormat.format(calendar.time)
            if (sortedDates.contains(expectedDate)) {
                streak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateOverallStreak(routineIds: Set<Long>, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (routineIds.isEmpty() || completions.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        
        for (i in 0..30) {
            val dateString = dateFormat.format(calendar.time)
            val completedRoutines = completions.filter { it.date == dateString }.map { it.routineId }.toSet()
            
            if (completedRoutines == routineIds) {
                streak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateLongestStreak(routineIds: Set<Long>, completions: List<com.allubie.nana.data.model.RoutineCompletion>): Int {
        if (routineIds.isEmpty() || completions.isEmpty()) return 0
        
        // Get all unique dates with completions, sorted
        val allDates = completions.map { it.date }.distinct().sorted()
        if (allDates.isEmpty()) return 0
        
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: Date? = null
        
        for (dateString in allDates) {
            val completedRoutines = completions.filter { it.date == dateString }.map { it.routineId }.toSet()
            
            // Check if all routines were completed on this day
            if (completedRoutines == routineIds) {
                val currentDate = dateFormat.parse(dateString)
                
                if (previousDate == null) {
                    currentStreak = 1
                } else {
                    // Check if this date is consecutive
                    val diffDays = ((currentDate?.time ?: 0) - previousDate.time) / (24 * 60 * 60 * 1000)
                    currentStreak = if (diffDays == 1L) currentStreak + 1 else 1
                }
                
                longestStreak = maxOf(longestStreak, currentStreak)
                previousDate = currentDate
            } else {
                // Reset streak if not all routines completed
                currentStreak = 0
                previousDate = null
            }
        }
        
        return longestStreak
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NanaApplication
                RoutineStatisticsViewModel(
                    application.database.routineDao(),
                    application.database.routineCompletionDao()
                )
            }
        }
    }
}
