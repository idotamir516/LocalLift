package com.workout.app

import android.app.Application
import com.workout.app.data.AppDatabase

class WorkoutApp : Application() {
    
    // Lazy-initialized database singleton
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // Database is lazily initialized on first access
    }
    
    companion object {
        // Helper to access database from any context
        fun getDatabase(application: Application): AppDatabase {
            return (application as WorkoutApp).database
        }
    }
}
