package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.workout.app.data.entities.TemplateExercise
import com.workout.app.data.entities.TemplateSet
import com.workout.app.data.entities.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Data class for template exercise with its sets using Room relations.
 */
data class TemplateExerciseWithSets(
    @Embedded val exercise: TemplateExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateExerciseId"
    )
    val sets: List<TemplateSet>
)

/**
 * Data class for template with its exercises using Room relations.
 */
data class TemplateWithExercises(
    @Embedded val template: WorkoutTemplate,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExercise>
)

/**
 * Full template with exercises and their sets.
 */
data class FullTemplateWithExercisesAndSets(
    val template: WorkoutTemplate,
    val exercises: List<TemplateExerciseWithSets>
)

@Dao
interface TemplateDao {
    
    // Get all templates with exercises
    @Transaction
    @Query("SELECT * FROM workout_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<TemplateWithExercises>>
    
    // Get templates in a specific folder
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE folderId = :folderId ORDER BY name ASC")
    fun getTemplatesInFolder(folderId: Long): Flow<List<TemplateWithExercises>>
    
    // Get templates without a folder (root level)
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE folderId IS NULL ORDER BY name ASC")
    fun getTemplatesWithoutFolder(): Flow<List<TemplateWithExercises>>
    
    // Get single template with exercises as Flow
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    fun getTemplateWithExercises(templateId: Long): Flow<TemplateWithExercises?>
    
    // Get single template with exercises (sync version for one-time fetch)
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateWithExercisesSync(templateId: Long): TemplateWithExercises?
    
    // Get single template by ID
    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): WorkoutTemplate?
    
    // Get template by name (for duplicate detection)
    @Query("SELECT * FROM workout_templates WHERE name = :name LIMIT 1")
    suspend fun getTemplateByName(name: String): WorkoutTemplate?
    
    // Get exercises for a template ordered by orderIndex
    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun getExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>>
    
    // Get exercises for a template (suspend version for one-time fetch)
    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getExercisesForTemplateOnce(templateId: Long): List<TemplateExercise>
    
    // Insert new template, returns the new ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplate): Long
    
    // Update existing template
    @Update
    suspend fun updateTemplate(template: WorkoutTemplate)
    
    // Delete template by ID (exercises cascade automatically)
    @Query("DELETE FROM workout_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Long)
    
    // Delete template entity
    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    // Insert template exercise
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(exercise: TemplateExercise): Long
    
    // Delete template exercise entity
    @Delete
    suspend fun deleteTemplateExercise(exercise: TemplateExercise)

    // TemplateSet operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateSet(set: TemplateSet): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateSets(sets: List<TemplateSet>)
    
    @Query("SELECT * FROM template_sets WHERE templateExerciseId = :exerciseId ORDER BY setNumber ASC")
    suspend fun getTemplateSetsForExercise(exerciseId: Long): List<TemplateSet>
    
    @Query("DELETE FROM template_sets WHERE templateExerciseId = :exerciseId")
    suspend fun deleteTemplateSetsForExercise(exerciseId: Long)

    // Get full template with exercises and sets
    @Transaction
    suspend fun getFullTemplateWithExercisesAndSets(templateId: Long): FullTemplateWithExercisesAndSets? {
        val template = getTemplateById(templateId) ?: return null
        val exercises = getExercisesForTemplateOnce(templateId)
        val exercisesWithSets = exercises.map { exercise ->
            val sets = getTemplateSetsForExercise(exercise.id)
            TemplateExerciseWithSets(exercise, sets)
        }
        return FullTemplateWithExercisesAndSets(template, exercisesWithSets)
    }

    // Insert exercise
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: TemplateExercise): Long    // Insert multiple exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<TemplateExercise>)
    
    // Update exercise
    @Update
    suspend fun updateExercise(exercise: TemplateExercise)
    
    // Update multiple exercises (for reordering)
    @Update
    suspend fun updateExercises(exercises: List<TemplateExercise>)
    
    // Update exercise note
    @Query("UPDATE template_exercises SET note = :note WHERE id = :exerciseId")
    suspend fun updateTemplateExerciseNote(exerciseId: Long, note: String?)
    
    // Delete exercise
    @Query("DELETE FROM template_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)
    
    // Delete all exercises for a template
    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesForTemplate(templateId: Long)
    
    // Move template to a folder
    @Query("UPDATE workout_templates SET folderId = :folderId, updatedAt = :updatedAt WHERE id = :templateId")
    suspend fun moveTemplateToFolder(templateId: Long, folderId: Long?, updatedAt: Long = System.currentTimeMillis())
    
    // Transaction: Save template with exercises
    @Transaction
    suspend fun saveTemplateWithExercises(template: WorkoutTemplate, exercises: List<TemplateExercise>): Long {
        val templateId = if (template.id == 0L) {
            insertTemplate(template)
        } else {
            updateTemplate(template.copy(updatedAt = System.currentTimeMillis()))
            template.id
        }
        
        // Delete existing exercises and insert new ones
        deleteExercisesForTemplate(templateId)
        val exercisesWithTemplateId = exercises.mapIndexed { index, exercise ->
            exercise.copy(templateId = templateId, orderIndex = index)
        }
        insertExercises(exercisesWithTemplateId)
        
        return templateId
    }
}
