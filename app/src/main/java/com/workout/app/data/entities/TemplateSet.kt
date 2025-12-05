package com.workout.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single set within a template exercise.
 * Stores the target weight, reps, rest time, and set type for template planning.
 */
@Entity(
    tableName = "template_sets",
    foreignKeys = [
        ForeignKey(
            entity = TemplateExercise::class,
            parentColumns = ["id"],
            childColumns = ["templateExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateExerciseId")]
)
data class TemplateSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateExerciseId: Long,
    val setNumber: Int,
    val targetWeight: Int? = null,
    val targetReps: Int? = null,
    val restSeconds: Int? = null,
    val setType: SetType = SetType.REGULAR
)
