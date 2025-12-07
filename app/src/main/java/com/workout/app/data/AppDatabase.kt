package com.workout.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.workout.app.data.dao.CustomExerciseDao
import com.workout.app.data.dao.SessionDao
import com.workout.app.data.dao.TemplateDao
import com.workout.app.data.entities.CustomExercise
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.TemplateExercise
import com.workout.app.data.entities.TemplateSet
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.data.entities.WorkoutTemplate

@Database(
    entities = [
        WorkoutTemplate::class,
        TemplateExercise::class,
        TemplateSet::class,
        WorkoutSession::class,
        ExerciseLog::class,
        SetLog::class,
        CustomExercise::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun templateDao(): TemplateDao
    abstract fun sessionDao(): SessionDao
    abstract fun customExerciseDao(): CustomExerciseDao
    
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
