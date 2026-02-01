package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.Routine
import com.allubie.nana.data.model.RoutineCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRoutines(): Flow<List<Routine>>
    
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<Routine>>
    
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): Routine?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long
    
    @Update
    suspend fun updateRoutine(routine: Routine)
    
    @Delete
    suspend fun deleteRoutine(routine: Routine)
    
    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)
    
    @Query("SELECT * FROM routines")
    suspend fun getAllRoutinesSync(): List<Routine>
    
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()
}

@Dao
interface RoutineCompletionDao {
    @Query("SELECT * FROM routine_completions WHERE routineId = :routineId ORDER BY date DESC")
    fun getCompletionsForRoutine(routineId: Long): Flow<List<RoutineCompletion>>
    
    @Query("SELECT * FROM routine_completions WHERE routineId = :routineId AND date = :date LIMIT 1")
    suspend fun getCompletionForDate(routineId: Long, date: String): RoutineCompletion?
    
    @Query("SELECT * FROM routine_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<RoutineCompletion>>
    
    @Query("SELECT * FROM routine_completions WHERE date >= :startDate AND date <= :endDate")
    fun getCompletionsInRange(startDate: String, endDate: String): Flow<List<RoutineCompletion>>
    
    @Query("SELECT COUNT(*) FROM routine_completions WHERE routineId = :routineId")
    suspend fun getTotalCompletions(routineId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: RoutineCompletion): Long
    
    @Delete
    suspend fun deleteCompletion(completion: RoutineCompletion)
    
    @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND date = :date")
    suspend fun deleteCompletionForDate(routineId: Long, date: String)
    
    @Query("DELETE FROM routine_completions WHERE routineId = :routineId")
    suspend fun deleteCompletionsForRoutine(routineId: Long)
    
    @Query("SELECT * FROM routine_completions")
    suspend fun getAllCompletionsSync(): List<RoutineCompletion>
    
    @Query("DELETE FROM routine_completions")
    suspend fun deleteAllCompletions()
}
