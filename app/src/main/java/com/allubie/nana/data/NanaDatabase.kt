package com.allubie.nana.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allubie.nana.data.dao.*
import com.allubie.nana.data.model.*

@Database(
    entities = [
        Note::class,
        NoteImage::class,
        ChecklistItem::class,
        Event::class,
        Routine::class,
        RoutineCompletion::class,
        Transaction::class,
        Budget::class,
        Label::class
    ],
    version = 9,
    exportSchema = true
)
abstract class NanaDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun noteImageDao(): NoteImageDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun eventDao(): EventDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineCompletionDao(): RoutineCompletionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: NanaDatabase? = null
        
        // Migration from version 6 to 7
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration
            }
        }
        
        // Migration from version 7 to 8 - Add indexes and foreign keys
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indexes to notes table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isDeleted_isArchived ON notes(isDeleted, isArchived)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isPinned ON notes(isPinned)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_updatedAt ON notes(updatedAt)")
                
                // Add indexes to note_images table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_images_noteId ON note_images(noteId)")
                
                // Add indexes to checklist_items table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_checklist_items_noteId ON checklist_items(noteId)")
                
                // Add indexes to events table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_startTime ON events(startTime)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_endTime ON events(endTime)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_isPinned ON events(isPinned)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_category ON events(category)")
                
                // Add indexes to routines table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routines_isActive ON routines(isActive)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routines_isPinned ON routines(isPinned)")
                
                // Add indexes to routine_completions table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_routineId ON routine_completions(routineId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_date ON routine_completions(date)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_completions_routineId_date ON routine_completions(routineId, date)")
                
                // Add indexes to transactions table
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions(category)")
                
                // Add unique index to budgets table
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_category ON budgets(category)")
            }
        }
        
        // Migration from version 8 to 9 - Add labels table
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create labels table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS labels (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        iconName TEXT,
                        color INTEGER NOT NULL,
                        isPreset INTEGER NOT NULL DEFAULT 0,
                        isHidden INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Add indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_labels_type ON labels(type)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_labels_name_type ON labels(name, type)")
            }
        }

        fun getDatabase(context: Context): NanaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NanaDatabase::class.java,
                    "nana_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    // Keep fallback for development - remove for production
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
