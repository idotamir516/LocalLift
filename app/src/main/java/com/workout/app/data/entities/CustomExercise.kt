package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user-created custom exercise.
 * Custom exercises are stored in the database and combined with the static exercise library.
 */
@Entity(tableName = "custom_exercises")
data class CustomExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMuscle: String,
    val auxiliaryMuscles: String = "", // Comma-separated list
    val isBodyweight: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts auxiliary muscles string to a list.
     */
    fun getAuxiliaryMusclesList(): List<String> {
        return if (auxiliaryMuscles.isBlank()) {
            emptyList()
        } else {
            auxiliaryMuscles.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }
}
