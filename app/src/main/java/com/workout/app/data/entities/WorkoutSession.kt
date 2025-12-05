package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single workout instance (completed or in-progress).
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long? = null,
    val templateName: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false
)
