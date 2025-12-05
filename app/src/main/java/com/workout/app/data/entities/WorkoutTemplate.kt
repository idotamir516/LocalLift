package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reusable workout blueprint created by the user.
 */
@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
