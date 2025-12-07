package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.workout.app.data.entities.CustomExercise
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for custom exercises.
 */
@Dao
interface CustomExerciseDao {
    
    @Query("SELECT * FROM custom_exercises ORDER BY name ASC")
    fun getAllCustomExercises(): Flow<List<CustomExercise>>
    
    @Query("SELECT * FROM custom_exercises ORDER BY name ASC")
    suspend fun getAllCustomExercisesSync(): List<CustomExercise>
    
    @Query("SELECT * FROM custom_exercises WHERE id = :id")
    suspend fun getCustomExerciseById(id: Long): CustomExercise?
    
    @Query("SELECT * FROM custom_exercises WHERE name = :name LIMIT 1")
    suspend fun getCustomExerciseByName(name: String): CustomExercise?
    
    @Query("SELECT * FROM custom_exercises WHERE primaryMuscle = :muscle ORDER BY name ASC")
    suspend fun getCustomExercisesByMuscle(muscle: String): List<CustomExercise>
    
    @Insert
    suspend fun insertCustomExercise(exercise: CustomExercise): Long
    
    @Update
    suspend fun updateCustomExercise(exercise: CustomExercise)
    
    @Delete
    suspend fun deleteCustomExercise(exercise: CustomExercise)
    
    @Query("DELETE FROM custom_exercises WHERE id = :id")
    suspend fun deleteCustomExerciseById(id: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM custom_exercises WHERE name = :name)")
    suspend fun exerciseExists(name: String): Boolean
}
