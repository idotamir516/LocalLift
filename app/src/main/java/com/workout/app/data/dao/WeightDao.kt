package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.workout.app.data.entities.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    
    // Get all weight entries ordered by date descending
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>
    
    // Get most recent weight entry
    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getMostRecentWeight(): Flow<WeightEntry?>
    
    // Get weight entries within a date range
    @Query("SELECT * FROM weight_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getWeightEntriesInRange(startDate: Long, endDate: Long): Flow<List<WeightEntry>>
    
    // Get weight entries for the last N days
    @Query("SELECT * FROM weight_entries WHERE date >= :sinceDate ORDER BY date ASC")
    fun getWeightEntriesSince(sinceDate: Long): Flow<List<WeightEntry>>
    
    // Get weight entry by ID
    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getWeightEntryById(id: Long): WeightEntry?
    
    // Get weight entry for a specific date (if exists)
    @Query("SELECT * FROM weight_entries WHERE date >= :startOfDay AND date < :endOfDay LIMIT 1")
    suspend fun getWeightEntryForDate(startOfDay: Long, endOfDay: Long): WeightEntry?
    
    // Insert a new weight entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry): Long
    
    // Update an existing weight entry
    @Update
    suspend fun updateWeightEntry(entry: WeightEntry)
    
    // Delete a weight entry
    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntry)
    
    // Delete weight entry by ID
    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightEntryById(id: Long)
    
    // Get count of weight entries
    @Query("SELECT COUNT(*) FROM weight_entries")
    fun getWeightEntryCount(): Flow<Int>
    
    // Get average weight for a date range
    @Query("SELECT AVG(weight) FROM weight_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageWeightInRange(startDate: Long, endDate: Long): Double?
    
    // Get min weight for a date range
    @Query("SELECT MIN(weight) FROM weight_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getMinWeightInRange(startDate: Long, endDate: Long): Double?
    
    // Get max weight for a date range
    @Query("SELECT MAX(weight) FROM weight_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getMaxWeightInRange(startDate: Long, endDate: Long): Double?
}
