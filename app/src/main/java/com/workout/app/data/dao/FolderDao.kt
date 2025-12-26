package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.workout.app.data.entities.WorkoutFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    
    // Get all folders ordered by orderIndex
    @Query("SELECT * FROM workout_folders ORDER BY orderIndex ASC, name ASC")
    fun getAllFolders(): Flow<List<WorkoutFolder>>
    
    // Get all folders (suspend version)
    @Query("SELECT * FROM workout_folders ORDER BY orderIndex ASC, name ASC")
    suspend fun getAllFoldersOnce(): List<WorkoutFolder>
    
    // Get folder by ID
    @Query("SELECT * FROM workout_folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): WorkoutFolder?
    
    // Get folder by name (for duplicate detection)
    @Query("SELECT * FROM workout_folders WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): WorkoutFolder?
    
    // Insert new folder, returns the new ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: WorkoutFolder): Long
    
    // Update existing folder
    @Update
    suspend fun updateFolder(folder: WorkoutFolder)
    
    // Delete folder by ID (templates will have folderId set to null due to foreign key)
    @Query("DELETE FROM workout_folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)
    
    // Delete folder entity
    @Delete
    suspend fun deleteFolder(folder: WorkoutFolder)
    
    // Get count of templates in a folder
    @Query("SELECT COUNT(*) FROM workout_templates WHERE folderId = :folderId")
    suspend fun getTemplateCountInFolder(folderId: Long): Int
    
    // Get the maximum order index (for adding new folders at the end)
    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM workout_folders")
    suspend fun getMaxOrderIndex(): Int
}
