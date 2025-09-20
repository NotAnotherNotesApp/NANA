package com.allubie.nana

import android.app.Application
import androidx.room.Room
import com.allubie.nana.data.database.AppDatabase
import com.allubie.nana.data.database.DatabaseCallback
import com.allubie.nana.data.repository.ExpenseRepository
import com.allubie.nana.data.repository.NoteRepository
import com.allubie.nana.data.repository.RoutineRepository
import com.allubie.nana.data.repository.ScheduleRepository

class NanaApplication : Application() {
    
    val database by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nana_database"
        )
    .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
    // Allow destructive migration only when no defined migration covers the path
    .fallbackToDestructiveMigration(true)
        .addCallback(DatabaseCallback())
        .build()
        
        // Set the instance for the callback
        DatabaseCallback.setInstance(db)
        db
    }
    
    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val routineRepository by lazy { RoutineRepository(database.routineDao()) }
    val scheduleRepository by lazy { ScheduleRepository(database.scheduleDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
}
