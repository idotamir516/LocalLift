package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Type of set: Regular, Warmup, or Drop-set
 */
enum class SetType {
    REGULAR,  // Normal working set (shows number)
    WARMUP,   // Warmup set (shows "W")
    DROP_SET  // Drop set (shows "D")
}

/**
 * Single set performed within an exercise.
 */
@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseLog::class,
            parentColumns = ["id"],
            childColumns = ["exerciseLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseLogId")]
)
data class SetLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseLogId: Long,
    val setNumber: Int,
    val reps: Int? = null,
    val weightLbs: Float? = null,
    val restSeconds: Int? = null,
    val rpe: Float? = null,
    val setType: SetType = SetType.REGULAR,
    val completedAt: Long? = null
)
