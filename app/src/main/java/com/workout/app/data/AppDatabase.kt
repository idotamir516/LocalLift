package com.workout.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.workout.app.data.dao.CustomExerciseDao
import com.workout.app.data.dao.FolderDao
import com.workout.app.data.dao.PhaseDao
import com.workout.app.data.dao.SessionDao
import com.workout.app.data.dao.TemplateDao
import com.workout.app.data.entities.CustomExercise
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.TemplateExercise
import com.workout.app.data.entities.TemplateSet
import com.workout.app.data.entities.TrainingPhase
import com.workout.app.data.entities.WorkoutFolder
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.data.entities.WorkoutTemplate

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create workout_folders table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS workout_folders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorHex TEXT,
                createdAt INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // SQLite doesn't support adding foreign keys via ALTER TABLE
        // So we need to recreate the workout_templates table with the foreign key
        
        // 1. Create new table with foreign key
        database.execSQL("""
            CREATE TABLE workout_templates_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                folderId INTEGER DEFAULT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY (folderId) REFERENCES workout_folders(id) ON DELETE SET NULL
            )
        """)
        
        // 2. Copy data from old table
        database.execSQL("""
            INSERT INTO workout_templates_new (id, name, folderId, createdAt, updatedAt)
            SELECT id, name, NULL, createdAt, updatedAt FROM workout_templates
        """)
        
        // 3. Drop old table
        database.execSQL("DROP TABLE workout_templates")
        
        // 4. Rename new table
        database.execSQL("ALTER TABLE workout_templates_new RENAME TO workout_templates")
        
        // 5. Create index on folderId
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_templates_folderId ON workout_templates(folderId)")
    }
}

// Migration to fix databases that ran the broken migration 6->7
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create workout_folders table if it doesn't exist
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS workout_folders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorHex TEXT,
                createdAt INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Recreate workout_templates with proper foreign key
        database.execSQL("""
            CREATE TABLE workout_templates_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                folderId INTEGER DEFAULT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY (folderId) REFERENCES workout_folders(id) ON DELETE SET NULL
            )
        """)
        
        // Copy data - handle both cases (with or without folderId column)
        database.execSQL("""
            INSERT INTO workout_templates_new (id, name, folderId, createdAt, updatedAt)
            SELECT id, name, folderId, createdAt, updatedAt FROM workout_templates
        """)
        
        // Drop old table and index
        database.execSQL("DROP INDEX IF EXISTS index_workout_templates_folderId")
        database.execSQL("DROP TABLE workout_templates")
        
        // Rename new table
        database.execSQL("ALTER TABLE workout_templates_new RENAME TO workout_templates")
        
        // Create index on folderId
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_templates_folderId ON workout_templates(folderId)")
    }
}

// Migration to add training phases
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS training_phases (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                endDate INTEGER,
                projectedEndDate INTEGER,
                notes TEXT,
                createdAt INTEGER NOT NULL
            )
        """)
    }
}

// Migration to remove projectedEndDate from training phases
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support DROP COLUMN, so we need to recreate the table
        database.execSQL("""
            CREATE TABLE training_phases_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                endDate INTEGER,
                notes TEXT,
                createdAt INTEGER NOT NULL
            )
        """)
        
        // Copy data (use projectedEndDate as endDate if endDate is null)
        database.execSQL("""
            INSERT INTO training_phases_new (id, name, type, startDate, endDate, notes, createdAt)
            SELECT id, name, type, startDate, COALESCE(endDate, projectedEndDate), notes, createdAt 
            FROM training_phases
        """)
        
        database.execSQL("DROP TABLE training_phases")
        database.execSQL("ALTER TABLE training_phases_new RENAME TO training_phases")
    }
}

@Database(
    entities = [
        WorkoutTemplate::class,
        TemplateExercise::class,
        TemplateSet::class,
        WorkoutSession::class,
        ExerciseLog::class,
        SetLog::class,
        CustomExercise::class,
        WorkoutFolder::class,
        TrainingPhase::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun templateDao(): TemplateDao
    abstract fun sessionDao(): SessionDao
    abstract fun customExerciseDao(): CustomExerciseDao
    abstract fun folderDao(): FolderDao
    abstract fun phaseDao(): PhaseDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
