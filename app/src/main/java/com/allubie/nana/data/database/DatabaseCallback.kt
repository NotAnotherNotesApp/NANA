package com.allubie.nana.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.allubie.nana.data.entity.ExpenseCategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Pre-populate expense categories
        INSTANCE?.let { database ->
            CoroutineScope(Dispatchers.IO).launch {
                populateDefaultCategories(database)
            }
        }
    }
    
    private suspend fun populateDefaultCategories(database: AppDatabase) {
        val expenseDao = database.expenseDao()
        
        // No hard-coded categories. Start with an empty set and let users create their own.
        val defaultCategories = emptyList<ExpenseCategoryEntity>()
        defaultCategories.forEach { category -> expenseDao.insertCategory(category) }
    }
    
    companion object {
        private var INSTANCE: AppDatabase? = null
        
        fun setInstance(instance: AppDatabase) {
            INSTANCE = instance
        }
    }
}
