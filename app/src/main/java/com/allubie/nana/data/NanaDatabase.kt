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
        NoteFts::class,
        NoteImage::class,
        ChecklistItem::class,
        Event::class,
        Routine::class,
        RoutineCompletion::class,
        Transaction::class,
        Budget::class,
        Label::class
    ],
    version = 12,
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
        
        // v6 -> v7 (no-op)
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }
        
        // v7 -> v8: Add query performance indexes and foreign keys
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isDeleted_isArchived ON notes(isDeleted, isArchived)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isPinned ON notes(isPinned)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_updatedAt ON notes(updatedAt)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_images_noteId ON note_images(noteId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_checklist_items_noteId ON checklist_items(noteId)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_startTime ON events(startTime)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_endTime ON events(endTime)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_isPinned ON events(isPinned)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_events_category ON events(category)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routines_isActive ON routines(isActive)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routines_isPinned ON routines(isPinned)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_routineId ON routine_completions(routineId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_date ON routine_completions(date)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_completions_routineId_date ON routine_completions(routineId, date)")
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions(category)")
                
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_category ON budgets(category)")
            }
        }
        
        // v8 -> v9: Add custom labels and categories table
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_labels_type ON labels(type)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_labels_name_type ON labels(name, type)")
            }
        }
        
        // v9 -> v10: Add CASCADE delete foreign keys
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS note_images_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, noteId INTEGER NOT NULL, imagePath TEXT NOT NULL, position INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL, FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO note_images_new (id, noteId, imagePath, position, createdAt) SELECT id, noteId, imagePath, position, createdAt FROM note_images")
                database.execSQL("DROP TABLE note_images")
                database.execSQL("ALTER TABLE note_images_new RENAME TO note_images")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_images_noteId ON note_images(noteId)")

                database.execSQL("CREATE TABLE IF NOT EXISTS checklist_items_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, noteId INTEGER NOT NULL, text TEXT NOT NULL, isChecked INTEGER NOT NULL DEFAULT 0, position INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO checklist_items_new (id, noteId, text, isChecked, position) SELECT id, noteId, text, isChecked, position FROM checklist_items")
                database.execSQL("DROP TABLE checklist_items")
                database.execSQL("ALTER TABLE checklist_items_new RENAME TO checklist_items")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_checklist_items_noteId ON checklist_items(noteId)")

                database.execSQL("CREATE TABLE IF NOT EXISTS routine_completions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, routineId INTEGER NOT NULL, date TEXT NOT NULL, isCompleted INTEGER NOT NULL DEFAULT 0, currentCount INTEGER NOT NULL DEFAULT 0, elapsedSeconds INTEGER NOT NULL DEFAULT 0, completedAt INTEGER NOT NULL, FOREIGN KEY(routineId) REFERENCES routines(id) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO routine_completions_new (id, routineId, date, isCompleted, currentCount, elapsedSeconds, completedAt) SELECT id, routineId, date, isCompleted, currentCount, elapsedSeconds, completedAt FROM routine_completions")
                database.execSQL("DROP TABLE routine_completions")
                database.execSQL("ALTER TABLE routine_completions_new RENAME TO routine_completions")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_routineId ON routine_completions(routineId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_routine_completions_date ON routine_completions(date)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_completions_routineId_date ON routine_completions(routineId, date)")
            }
        }
        
        // v10 -> v11: Add FTS4 full-text search index for notes
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `notes_fts` USING FTS4(`title`, `content`, content=`notes`)")
                database.execSQL("INSERT INTO notes_fts(notes_fts) VALUES ('rebuild')")
            }
        }
        
        // v11 -> v12: Scope budgets per month and year
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cal = java.util.Calendar.getInstance()
                val currentMonth = cal.get(java.util.Calendar.MONTH)
                val currentYear = cal.get(java.util.Calendar.YEAR)
                
                database.execSQL("ALTER TABLE budgets ADD COLUMN budgetMonth INTEGER NOT NULL DEFAULT $currentMonth")
                database.execSQL("ALTER TABLE budgets ADD COLUMN budgetYear INTEGER NOT NULL DEFAULT $currentYear")
                database.execSQL("DROP INDEX IF EXISTS index_budgets_category")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_category_budgetMonth_budgetYear ON budgets(category, budgetMonth, budgetYear)")
            }
        }

        fun getDatabase(context: Context): NanaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NanaDatabase::class.java,
                    "nana_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
