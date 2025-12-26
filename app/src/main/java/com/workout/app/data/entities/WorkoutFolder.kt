package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A folder for organizing workout templates.
 */
@Entity(tableName = "workout_folders")
data class WorkoutFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String? = null, // Optional color for folder icon
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0 // For manual ordering of folders
)
