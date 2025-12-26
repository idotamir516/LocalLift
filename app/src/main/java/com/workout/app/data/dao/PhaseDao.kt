package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.workout.app.data.entities.TrainingPhase
import kotlinx.coroutines.flow.Flow

@Dao
interface PhaseDao {
    
    // Get all phases ordered by start date (most recent first)
    @Query("SELECT * FROM training_phases ORDER BY startDate DESC")
    fun getAllPhases(): Flow<List<TrainingPhase>>
    
    // Get all phases (suspend version)
    @Query("SELECT * FROM training_phases ORDER BY startDate DESC")
    suspend fun getAllPhasesOnce(): List<TrainingPhase>
    
    // Get the phase that is active on a specific date
    // A phase is active if: startDate <= date AND (endDate is null OR endDate >= date)
    @Query("""
        SELECT * FROM training_phases 
        WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date)
        ORDER BY startDate DESC
        LIMIT 1
    """)
    fun getActivePhaseForDate(date: Long): Flow<TrainingPhase?>
    
    // Get the phase that is active today (suspend version)
    @Query("""
        SELECT * FROM training_phases 
        WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date)
        ORDER BY startDate DESC
        LIMIT 1
    """)
    suspend fun getActivePhaseForDateOnce(date: Long): TrainingPhase?
    
    // Get phase by ID
    @Query("SELECT * FROM training_phases WHERE id = :phaseId")
    suspend fun getPhaseById(phaseId: Long): TrainingPhase?
    
    // Insert new phase
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: TrainingPhase): Long
    
    // Update existing phase
    @Update
    suspend fun updatePhase(phase: TrainingPhase)
    
    // Delete phase
    @Delete
    suspend fun deletePhase(phase: TrainingPhase)
    
    // Delete phase by ID
    @Query("DELETE FROM training_phases WHERE id = :phaseId")
    suspend fun deletePhaseById(phaseId: Long)
    
    // Check for overlapping phases when creating/updating a phase
    // A phase overlaps if: its start is before our end AND its end is after our start
    // For open-ended phases (endDate = null), we treat them as extending to infinity
    @Query("""
        SELECT * FROM training_phases 
        WHERE id != :excludePhaseId
        AND startDate < :endDate
        AND (endDate IS NULL OR endDate > :startDate)
        LIMIT 1
    """)
    suspend fun findOverlappingPhase(startDate: Long, endDate: Long, excludePhaseId: Long = 0): TrainingPhase?
    
    // Check for overlapping phases when creating an open-ended phase (no end date)
    @Query("""
        SELECT * FROM training_phases 
        WHERE id != :excludePhaseId
        AND (endDate IS NULL OR endDate > :startDate)
        LIMIT 1
    """)
    suspend fun findOverlappingPhaseForOpenEndedPhase(startDate: Long, excludePhaseId: Long = 0): TrainingPhase?
}
