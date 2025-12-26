package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Type of training phase.
 */
enum class PhaseType {
    BULK,
    CUT,
    MAINTENANCE,
    STRENGTH,
    HYPERTROPHY,
    DELOAD,
    CUSTOM
}

/**
 * A training phase represents a period of time with a specific training goal.
 * A phase is considered "active" if today's date falls within its date range.
 */
@Entity(tableName = "training_phases")
data class TrainingPhase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: PhaseType,
    val startDate: Long, // Epoch millis
    val endDate: Long? = null, // null = open-ended (ongoing until explicitly ended)
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if this phase is currently active based on today's date.
     * A phase is active if: startDate <= today AND (endDate is null OR endDate >= today)
     */
    fun isActiveOn(date: Long = System.currentTimeMillis()): Boolean {
        // Normalize to start of day for comparison
        val startOfDay = date / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000)
        val phaseStart = startDate / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000)
        val phaseEnd = endDate?.let { it / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) }
        
        return phaseStart <= startOfDay && (phaseEnd == null || phaseEnd >= startOfDay)
    }
    
    val isCurrentlyActive: Boolean
        get() = isActiveOn()
}
