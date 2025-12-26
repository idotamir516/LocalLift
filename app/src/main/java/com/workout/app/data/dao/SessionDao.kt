package com.workout.app.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.SetType
import com.workout.app.data.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Data class for exercise log with its sets using Room relations.
 */
data class ExerciseWithSets(
    @Embedded val exerciseLog: ExerciseLog,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseLogId"
    )
    val sets: List<SetLog>
)

/**
 * Data class for session with full exercise and set data using Room relations.
 */
data class SessionWithDetails(
    @Embedded val session: WorkoutSession,
    @Relation(
        entity = ExerciseLog::class,
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val exercises: List<ExerciseWithSets>
)

@Dao
interface SessionDao {
    
    // Get in-progress workout (for crash recovery)
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSession?>
    
    // Get in-progress workout (suspend version)
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 LIMIT 1")
    suspend fun getActiveSessionOnce(): WorkoutSession?
    
    // Get all completed sessions (history, reverse chronological)
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>
    
    // Get session by ID
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?
    
    // Get exercise logs for a session
    @Query("SELECT * FROM exercise_logs WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getExerciseLogsForSession(sessionId: Long): List<ExerciseLog>
    
    // Get exercise logs for a session (Flow version)
    @Query("SELECT * FROM exercise_logs WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getExerciseLogsForSessionFlow(sessionId: Long): Flow<List<ExerciseLog>>
    
    // Get sets for an exercise log
    @Query("SELECT * FROM set_logs WHERE exerciseLogId = :exerciseLogId ORDER BY setNumber ASC")
    suspend fun getSetsForExerciseLog(exerciseLogId: Long): List<SetLog>
    
    // Get sets for an exercise log (Flow version)
    @Query("SELECT * FROM set_logs WHERE exerciseLogId = :exerciseLogId ORDER BY setNumber ASC")
    fun getSetsForExerciseLogFlow(exerciseLogId: Long): Flow<List<SetLog>>
    
    // Get all sets for a session
    @Query("""
        SELECT set_logs.* FROM set_logs 
        INNER JOIN exercise_logs ON set_logs.exerciseLogId = exercise_logs.id 
        WHERE exercise_logs.sessionId = :sessionId 
        ORDER BY exercise_logs.orderIndex ASC, set_logs.setNumber ASC
    """)
    fun getSetsForSession(sessionId: Long): Flow<List<SetLog>>
    
    // Insert new session
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long
    
    // Update session
    @Update
    suspend fun updateSession(session: WorkoutSession)
    
    // Delete session (exercise logs and set logs cascade)
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
    
    // Insert exercise log
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog): Long
    
    // Insert multiple exercise logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLogs(exerciseLogs: List<ExerciseLog>): List<Long>
    
    // Update exercise log
    @Update
    suspend fun updateExerciseLog(exerciseLog: ExerciseLog)
    
    // Update multiple exercise logs
    @Update
    suspend fun updateExerciseLogs(exerciseLogs: List<ExerciseLog>)
    
    // Delete exercise log
    @Query("DELETE FROM exercise_logs WHERE id = :exerciseLogId")
    suspend fun deleteExerciseLog(exerciseLogId: Long)
    
    // Insert set log
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(setLog: SetLog): Long
    
    // Insert multiple set logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLogs(setLogs: List<SetLog>)
    
    // Update set log
    @Update
    suspend fun updateSetLog(setLog: SetLog)
    
    // Update multiple set logs
    @Update
    suspend fun updateSetLogs(setLogs: List<SetLog>)
    
    // Delete set log
    @Query("DELETE FROM set_logs WHERE id = :setLogId")
    suspend fun deleteSetLog(setLogId: Long)

    // Get session with full details as Flow
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithDetails(sessionId: Long): Flow<SessionWithDetails?>

    // Get session with full details (suspend version)
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionWithDetailsSync(sessionId: Long): SessionWithDetails?

    // Get all completed sessions with details (for export)
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY completedAt DESC")
    suspend fun getAllCompletedSessionsWithDetails(): List<SessionWithDetails>

    // Alias methods for navigation compatibility
    suspend fun saveExerciseLog(exerciseLog: ExerciseLog): Long = insertExerciseLog(exerciseLog)
    suspend fun saveSetLog(setLog: SetLog): Long = insertSetLog(setLog)
    
    // Transaction: Complete workout
    @Transaction
    suspend fun completeWorkout(sessionId: Long) {
        val session = getSessionById(sessionId) ?: return
        updateSession(
            session.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Data class to hold previous set information for display.
     */
    data class PreviousSetData(
        val exerciseName: String,
        val setNumber: Int,
        val setType: SetType,
        val weightLbs: Float?,
        val reps: Int?
    )
    
    /**
     * Get previous set data for an exercise from the last workout using the same template.
     * Matches by exercise name, set number, and set type.
     * 
     * @param templateId The template ID to match
     * @param exerciseName The exercise name to find
     * @param currentSessionId The current session ID to exclude
     */
    @Query("""
        SELECT el.exerciseName, sl.setNumber, sl.setType, sl.weightLbs, sl.reps
        FROM set_logs sl
        INNER JOIN exercise_logs el ON sl.exerciseLogId = el.id
        INNER JOIN workout_sessions ws ON el.sessionId = ws.id
        WHERE ws.templateId = :templateId
          AND el.exerciseName = :exerciseName
          AND ws.isCompleted = 1
          AND ws.id != :currentSessionId
        ORDER BY ws.completedAt DESC
    """)
    suspend fun getPreviousSetsForExerciseByTemplate(
        templateId: Long,
        exerciseName: String,
        currentSessionId: Long
    ): List<PreviousSetData>
    
    /**
     * Get previous set data for an exercise from any workout (any template).
     * Matches by exercise name, set number, and set type.
     * 
     * @param exerciseName The exercise name to find
     * @param currentSessionId The current session ID to exclude
     */
    @Query("""
        SELECT el.exerciseName, sl.setNumber, sl.setType, sl.weightLbs, sl.reps
        FROM set_logs sl
        INNER JOIN exercise_logs el ON sl.exerciseLogId = el.id
        INNER JOIN workout_sessions ws ON el.sessionId = ws.id
        WHERE el.exerciseName = :exerciseName
          AND ws.isCompleted = 1
          AND ws.id != :currentSessionId
        ORDER BY ws.completedAt DESC
    """)
    suspend fun getPreviousSetsForExerciseAny(
        exerciseName: String,
        currentSessionId: Long
    ): List<PreviousSetData>
    
    /**
     * Data class for historical lift data including session date and RPE.
     */
    data class HistoricalSetData(
        val sessionId: Long,
        val sessionDate: Long,
        val setNumber: Int,
        val setType: SetType,
        val weightLbs: Float?,
        val reps: Int?,
        val rpe: Float?
    )
    
    /**
     * Get historical lift data for an exercise across all completed sessions.
     * Returns sets grouped by session, ordered by date descending.
     * 
     * @param exerciseName The exercise name to find
     * @param limit Maximum number of sessions to return
     */
    @Query("""
        SELECT ws.id as sessionId, ws.completedAt as sessionDate, 
               sl.setNumber, sl.setType, sl.weightLbs, sl.reps, sl.rpe
        FROM set_logs sl
        INNER JOIN exercise_logs el ON sl.exerciseLogId = el.id
        INNER JOIN workout_sessions ws ON el.sessionId = ws.id
        WHERE el.exerciseName = :exerciseName
          AND ws.isCompleted = 1
          AND ws.completedAt IS NOT NULL
        ORDER BY ws.completedAt DESC, sl.setNumber ASC
    """)
    suspend fun getHistoricalSetsForExercise(
        exerciseName: String
    ): List<HistoricalSetData>
}
