package com.allubie.nana.database

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.allubie.nana.data.database.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.io.IOException

/**
 * Verifies schema migrations from version 1 -> 4 for critical columns (richContent, htmlContent).
 */
@RunWith(AndroidJUnit4::class)
class NoteMigrationTest {
    private val dbName = "migration-test-db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create database at version 1
        helper.createDatabase(dbName, 1).apply {
            // Insert minimal legacy row (assumes columns id, title, content exist)
            try {
                execSQL("INSERT INTO notes (id, title, content) VALUES (1, 'Legacy Title', 'Legacy Body')")
            } catch (_: Exception) {
                // If schema differs in early versions, ignore; test focus is migration chain presence
            }
            close()
        }

        // Run migrations up to latest (4)
        val db = helper.runMigrationsAndValidate(dbName, 4, true, AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)

        // Validate new columns exist and seeded
        db.query("PRAGMA table_info(notes)").use { cursor ->
            var hasRich = false
            var hasHtml = false
            while (cursor.moveToNext()) {
                val name = cursor.getString(1)
                if (name == "richContent") hasRich = true
                if (name == "htmlContent") hasHtml = true
            }
            assertTrue("richContent column missing after migration", hasRich)
            assertTrue("htmlContent column missing after migration", hasHtml)
        }

        db.query("SELECT richContent, htmlContent FROM notes WHERE id = 1").use { cursor ->
            if (cursor.moveToFirst()) {
                val rich = cursor.getString(0)
                val html = cursor.getString(1)
                assertTrue("richContent not backfilled", rich.isNotEmpty())
                assertTrue("htmlContent not seeded", html.isNotEmpty())
            } else {
                fail("Legacy note row missing after migration")
            }
        }
    }
}
