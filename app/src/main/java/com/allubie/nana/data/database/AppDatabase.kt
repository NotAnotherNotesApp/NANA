package com.allubie.nana.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allubie.nana.data.dao.ExpenseDao
import com.allubie.nana.data.dao.NoteDao
import com.allubie.nana.data.dao.RoutineDao
import com.allubie.nana.data.dao.ScheduleDao
import com.allubie.nana.data.entity.ExpenseEntity
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import com.allubie.nana.data.entity.NoteEntity
import com.allubie.nana.data.entity.RoutineEntity
import com.allubie.nana.data.entity.RoutineCompletionEntity
import com.allubie.nana.data.entity.ScheduleEntity
import com.allubie.nana.data.converter.DateTimeConverters

@Database(
    entities = [
        NoteEntity::class,
        RoutineEntity::class,
        RoutineCompletionEntity::class,
        ScheduleEntity::class,
        ExpenseEntity::class,
        ExpenseCategoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
// Centralized DateTimeConverters (moved to data.converter package to avoid duplication)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun routineDao(): RoutineDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add reminderMinutes column to schedules table
                db.execSQL("ALTER TABLE schedules ADD COLUMN reminderMinutes INTEGER NOT NULL DEFAULT 15")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add rich text fields to notes table
                db.execSQL("ALTER TABLE notes ADD COLUMN richContent TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN noteType TEXT NOT NULL DEFAULT 'text'")
                
                // Update existing notes to have richContent equal to content
                db.execSQL("UPDATE notes SET richContent = content WHERE richContent = ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add htmlContent column; seed with richContent (or content fallback)
                db.execSQL("ALTER TABLE notes ADD COLUMN htmlContent TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE notes SET htmlContent = CASE WHEN richContent != '' THEN richContent ELSE content END")
            }
        }
    }
}
