package com.allubie.nana.data.repository

import com.allubie.nana.data.dao.*
import com.allubie.nana.data.model.*
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao
) {
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>> = eventDao.getEventsForDay(startOfDay, endOfDay)
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<Event>> = eventDao.getEventsInRange(startTime, endTime)
    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    fun getUpcomingEvents(now: Long, limit: Int = 10): Flow<List<Event>> = eventDao.getUpcomingEvents(now, limit)
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)
}
