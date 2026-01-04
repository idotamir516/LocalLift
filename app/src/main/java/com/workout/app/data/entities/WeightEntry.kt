package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Unit for weight measurement.
 */
enum class WeightUnit {
    KG,
    LBS
}

/**
 * A weight entry represents a single body weight measurement at a point in time.
 */
@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["date"])]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double, // Weight value
    val unit: WeightUnit = WeightUnit.KG,
    val date: Long, // Epoch millis - the date of measurement
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert weight to kilograms.
     */
    fun toKg(): Double = when (unit) {
        WeightUnit.KG -> weight
        WeightUnit.LBS -> weight * 0.453592
    }
    
    /**
     * Convert weight to pounds.
     */
    fun toLbs(): Double = when (unit) {
        WeightUnit.KG -> weight * 2.20462
        WeightUnit.LBS -> weight
    }
    
    /**
     * Get formatted weight string with unit.
     */
    fun formatted(): String = when (unit) {
        WeightUnit.KG -> String.format("%.1f kg", weight)
        WeightUnit.LBS -> String.format("%.1f lbs", weight)
    }
}
