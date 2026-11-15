package com.example.breedingcalendar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.breedingcalendar.data.entity.CalendarEvent
import com.example.breedingcalendar.data.entity.CustomEventType

@Database(
    entities = [
        CalendarEvent::class,
        CustomEventType::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    ALTER TABLE custom_event_types
                    ADD COLUMN memoFormat TEXT NOT NULL DEFAULT '%s %d'
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE custom_event_types
                    ADD COLUMN incrementNumber INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE calendar_events
                    ADD COLUMN individualGroup TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE calendar_events
                    ADD COLUMN individualNumber INTEGER
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "breeding_calendar_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}