package com.workout.app.data

import androidx.room.TypeConverter
import com.workout.app.data.entities.PhaseType

/**
 * Type converters for Room database.
 */
class Converters {
    
    @TypeConverter
    fun fromPhaseType(type: PhaseType): String {
        return type.name
    }
    
    @TypeConverter
    fun toPhaseType(value: String): PhaseType {
        return try {
            PhaseType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PhaseType.CUSTOM
        }
    }
}
