package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.*
import com.allubie.nana.data.model.*
import kotlinx.coroutines.flow.Flow

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val routineCompletionDao: RoutineCompletionDao
) {
    // --- RoutineDao ---
    fun getActiveRoutines(): Flow<List<Routine>> = routineDao.getActiveRoutines()
    fun getAllRoutines(): Flow<List<Routine>> = routineDao.getAllRoutines()
    suspend fun getRoutineById(id: Long): Routine? = routineDao.getRoutineById(id)
    suspend fun insertRoutine(routine: Routine): Long = routineDao.insertRoutine(routine)
    suspend fun updateRoutine(routine: Routine) = routineDao.updateRoutine(routine)
    suspend fun saveRoutine(routine: Routine): Long {
        return if (routine.id > 0) {
            routineDao.updateRoutine(routine)
            routine.id
        } else {
            routineDao.insertRoutine(routine)
        }
    }
    suspend fun deleteRoutine(routine: Routine) = routineDao.deleteRoutine(routine)
    suspend fun deleteRoutineById(id: Long) = routineDao.deleteRoutineById(id)

    // --- RoutineCompletionDao ---
    fun getCompletionsForRoutine(routineId: Long): Flow<List<RoutineCompletion>> = routineCompletionDao.getCompletionsForRoutine(routineId)
    suspend fun getCompletionForDate(routineId: Long, date: String): RoutineCompletion? = routineCompletionDao.getCompletionForDate(routineId, date)
    fun getCompletionsForDate(date: String): Flow<List<RoutineCompletion>> = routineCompletionDao.getCompletionsForDate(date)
    fun getCompletionsInRange(startDate: String, endDate: String): Flow<List<RoutineCompletion>> = routineCompletionDao.getCompletionsInRange(startDate, endDate)
    suspend fun getTotalCompletions(routineId: Long): Int = routineCompletionDao.getTotalCompletions(routineId)
    suspend fun insertCompletion(completion: RoutineCompletion): Long = routineCompletionDao.insertCompletion(completion)
    suspend fun deleteCompletion(completion: RoutineCompletion) = routineCompletionDao.deleteCompletion(completion)
    suspend fun deleteCompletionForDate(routineId: Long, date: String) = routineCompletionDao.deleteCompletionForDate(routineId, date)
    suspend fun deleteCompletionsForRoutine(routineId: Long) = routineCompletionDao.deleteCompletionsForRoutine(routineId)
}
