package com.allubie.nana.data.dao

import androidx.room.*
import com.allubie.nana.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?
    
    @Query("SELECT * FROM events WHERE startTime > :now ORDER BY startTime ASC LIMIT :limit")
    fun getUpcomingEvents(now: Long, limit: Int = 10): Flow<List<Event>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
    
    @Query("SELECT * FROM events")
    suspend fun getAllEventsSync(): List<Event>
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
