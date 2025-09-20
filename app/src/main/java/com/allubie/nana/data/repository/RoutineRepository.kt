package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.RoutineDao
import com.allubie.nana.data.entity.RoutineEntity
import com.allubie.nana.data.entity.RoutineCompletionEntity
import com.allubie.nana.data.entity.RoutineFactory
import com.allubie.nana.core.BaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import java.util.UUID

class RoutineRepository(private val routineDao: RoutineDao) : BaseRepository() {
    
    fun getActiveRoutines(): Flow<List<RoutineEntity>> = routineDao.getActiveRoutinesFlow()
    
    suspend fun getRoutineById(id: String): RoutineEntity? = routineDao.getRoutineById(id)
    
    suspend fun insertRoutine(routine: RoutineEntity) = routineDao.insertRoutine(routine)
    
    suspend fun updateRoutine(routine: RoutineEntity) = routineDao.updateRoutine(routine)
    
    suspend fun deleteRoutine(routine: RoutineEntity) = routineDao.deleteRoutine(routine)
    
    suspend fun pinRoutine(id: String) = routineDao.setPinned(id, true)
    
    suspend fun unpinRoutine(id: String) = routineDao.setPinned(id, false)
    
    suspend fun isCompletedToday(routineId: String): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return routineDao.getCompletionForDate(routineId, today) != null
    }
    
    suspend fun toggleCompletion(routineId: String, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        val existingCompletion = routineDao.getCompletionForDate(routineId, date)
        if (existingCompletion != null) {
            routineDao.deleteCompletion(existingCompletion)
        } else {
            val completion = RoutineCompletionEntity(
                id = idProvider.newId(),
                routineId = routineId,
                completionDate = date
            )
            routineDao.insertCompletion(completion)
        }
    }
    
    suspend fun getCompletionsForDate(date: LocalDate): List<RoutineCompletionEntity> {
        return routineDao.getCompletionsForDate(date)
    }
    
    suspend fun getStreakCount(routineId: String): Int {
        return try {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val completions = routineDao.getCompletionsForRoutine(routineId)
            var streak = 0
            var currentDate = today
            
            while (completions.any { it.completionDate == currentDate }) {
                streak++
                currentDate = currentDate.minus(DatePeriod(days = 1))
            }
            
            streak
        } catch (e: Exception) {
            0
        }
    }
    
    suspend fun getCompletionRate(routineId: String, days: Int = 30): Float {
        return try {
            val endDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val startDate = endDate.minus(DatePeriod(days = days))
            
            val completions = routineDao.getCompletionsForRoutine(routineId)
            val completedDays = completions.count { it.completionDate >= startDate && it.completionDate <= endDate }
            
            if (days > 0) completedDays.toFloat() / days.toFloat() else 0f
        } catch (e: Exception) {
            0f
        }
    }
    
    suspend fun createRoutine(
        title: String,
        description: String,
        frequency: String,
        reminderTime: String? = null
    ): RoutineEntity {
        val routine = RoutineFactory.create(id = idProvider.newId(), title = title, description = description, frequency = frequency, reminderTime = reminderTime)
        insertRoutine(routine)
        return routine
    }
}
