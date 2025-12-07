package com.workout.app.ui.screens

import android.content.Context
import com.workout.app.data.AppDatabase
import com.workout.app.data.Exercise
import com.workout.app.data.dao.SessionWithDetails
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.util.AudioPlayer
import com.workout.app.util.PreviousLiftSource
import com.workout.app.util.SettingsManager
import com.workout.app.util.TimerManager
import com.workout.app.util.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder for ActiveWorkoutScreen.
 * Manages workout state, set logging, rest timer, and database persistence.
 */
class ActiveWorkoutState(
    private val sessionId: Long,
    private val database: AppDatabase,
    private val scope: CoroutineScope,
    context: Context
) {
    private val _session = MutableStateFlow<WorkoutSession?>(null)
    val session: StateFlow<WorkoutSession?> = _session.asStateFlow()
    
    private val _exercises = MutableStateFlow<List<ActiveExercise>>(emptyList())
    val exercises: StateFlow<List<ActiveExercise>> = _exercises.asStateFlow()
    
    private val _showExercisePicker = MutableStateFlow(false)
    val showExercisePicker: StateFlow<Boolean> = _showExercisePicker.asStateFlow()
    
    private val audioPlayer = AudioPlayer(context)
    private val settingsManager = SettingsManager.getInstance(context)
    
    private val timerManager = TimerManager(scope) {
        // Play sound when timer completes
        audioPlayer.playNotificationSound()
    }
    val timerState: StateFlow<TimerState> = timerManager.timerState
    
    init {
        loadSession()
    }
    
    private fun loadSession() {
        scope.launch {
            // Load session
            val sessionWithDetails = database.sessionDao().getSessionWithDetailsSync(sessionId)
            if (sessionWithDetails != null) {
                _session.value = sessionWithDetails.session
                
                // Determine how to fetch previous lift data based on settings
                val previousLiftSource = settingsManager.getPreviousLiftSourceSync()
                val templateId = sessionWithDetails.session.templateId
                
                // Convert to ActiveExercise format
                _exercises.value = sessionWithDetails.exercises.map { exerciseWithSets ->
                    val exerciseName = exerciseWithSets.exerciseLog.exerciseName
                    
                    // Load previous set data for this exercise
                    val previousSets = when {
                        previousLiftSource == PreviousLiftSource.BY_TEMPLATE && templateId != null -> {
                            database.sessionDao().getPreviousSetsForExerciseByTemplate(
                                templateId = templateId,
                                exerciseName = exerciseName,
                                currentSessionId = sessionId
                            )
                        }
                        else -> {
                            database.sessionDao().getPreviousSetsForExerciseAny(
                                exerciseName = exerciseName,
                                currentSessionId = sessionId
                            )
                        }
                    }
                    
                    ActiveExercise(
                        exerciseLog = exerciseWithSets.exerciseLog,
                        exerciseName = exerciseName,
                        restSeconds = exerciseWithSets.sets.firstOrNull()?.restSeconds,
                        showRpe = exerciseWithSets.exerciseLog.showRpe,
                        sets = exerciseWithSets.sets.sortedBy { it.setNumber }.map { setLog ->
                            // Find matching previous set (same set number and set type)
                            val matchingPreviousSet = previousSets.find { prev ->
                                prev.setNumber == setLog.setNumber && prev.setType == setLog.setType
                            }
                            
                            ActiveSet(
                                setLog = setLog,
                                setNumber = setLog.setNumber,
                                weight = setLog.weightLbs?.toInt(),
                                reps = setLog.reps,
                                rpe = setLog.rpe,
                                isCompleted = setLog.completedAt != null,
                                previousWeight = matchingPreviousSet?.weightLbs?.toInt(),
                                previousReps = matchingPreviousSet?.reps,
                                restSeconds = setLog.restSeconds,
                                setType = setLog.setType
                            )
                        },
                        isExpanded = true
                    )
                }.sortedBy { it.exerciseLog.orderIndex }
            }
        }
    }
    
    /**
     * Updates the weight for a set and auto-saves to database.
     */
    fun updateSetWeight(exerciseIndex: Int, setNumber: Int, weight: Int?) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToUpdate = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToUpdate.setLog.copy(weightLbs = weight?.toFloat())
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(weight = weight, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Auto-save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
    }
    
    /**
     * Updates the reps for a set and auto-saves to database.
     */
    fun updateSetReps(exerciseIndex: Int, setNumber: Int, reps: Int?) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToUpdate = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToUpdate.setLog.copy(reps = reps)
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(reps = reps, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Auto-save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
    }
    
    /**
     * Updates the rest time for a set and auto-saves to database.
     */
    fun updateSetRest(exerciseIndex: Int, setNumber: Int, restSeconds: Int?) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToUpdate = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToUpdate.setLog.copy(restSeconds = restSeconds)
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(restSeconds = restSeconds, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Auto-save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
    }
    
    /**
     * Updates the set type (Regular/Warmup/Drop) and auto-saves to database.
     */
    fun updateSetType(exerciseIndex: Int, setNumber: Int, setType: com.workout.app.data.entities.SetType) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToUpdate = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToUpdate.setLog.copy(setType = setType)
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(setType = setType, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Auto-save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
    }
    
    /**
     * Updates the RPE (Rate of Perceived Exertion) for a set and auto-saves to database.
     */
    fun updateSetRpe(exerciseIndex: Int, setNumber: Int, rpe: Float?) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToUpdate = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToUpdate.setLog.copy(rpe = rpe)
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(rpe = rpe, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Auto-save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
    }
    
    /**
     * Marks a set as complete, saves to database, and starts rest timer.
     */
    fun completeSet(exerciseIndex: Int, setNumber: Int) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToComplete = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Toggle completion state
        val newIsCompleted = !setToComplete.isCompleted
        val completedAt = if (newIsCompleted) System.currentTimeMillis() else null
        
        // Update both the ActiveSet fields AND the underlying SetLog
        val updatedSetLog = setToComplete.setLog.copy(completedAt = completedAt)
        val updatedSets = exercise.sets.map { set ->
            if (set.setNumber == setNumber) {
                set.copy(isCompleted = newIsCompleted, setLog = updatedSetLog)
            } else {
                set
            }
        }
        
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
        
        // Save to database
        scope.launch {
            database.sessionDao().updateSetLog(updatedSetLog)
        }
        
        // Start rest timer if completing (not un-completing) and rest time is configured for this set
        if (newIsCompleted && setToComplete.restSeconds != null && setToComplete.restSeconds > 0) {
            timerManager.start(setToComplete.restSeconds)
        }
    }
    
    /**
     * Adds a new set to an exercise.
     */
    fun addSet(exerciseIndex: Int) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val newSetNumber = (exercise.sets.maxOfOrNull { it.setNumber } ?: 0) + 1
        
        // Get rest time from the last set, or use the exercise default
        val lastSetRestTime = exercise.sets.lastOrNull()?.restSeconds ?: exercise.restSeconds
        
        scope.launch {
            // Create new set in database
            val newSetLog = SetLog(
                exerciseLogId = exercise.exerciseLog.id,
                setNumber = newSetNumber,
                restSeconds = lastSetRestTime
            )
            val newSetId = database.sessionDao().insertSetLog(newSetLog)
            
            // Update local state
            val newSet = ActiveSet(
                setLog = newSetLog.copy(id = newSetId),
                setNumber = newSetNumber,
                weight = null,
                reps = null,
                isCompleted = false,
                restSeconds = lastSetRestTime
            )
            
            val updatedExercise = exercise.copy(sets = exercise.sets + newSet)
            currentExercises[exerciseIndex] = updatedExercise
            _exercises.value = currentExercises
        }
    }
    
    /**
     * Removes a set from an exercise.
     */
    fun removeSet(exerciseIndex: Int, setNumber: Int) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        val setToRemove = exercise.sets.find { it.setNumber == setNumber } ?: return
        
        // Don't allow removing the last set
        if (exercise.sets.size <= 1) return
        
        scope.launch {
            // Delete from database
            database.sessionDao().deleteSetLog(setToRemove.setLog.id)
        }
        
        // Update local state
        val updatedSets = exercise.sets.filter { it.setNumber != setNumber }
        currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _exercises.value = currentExercises
    }
    
    /**
     * Toggles exercise card expansion.
     */
    fun toggleExpand(exerciseIndex: Int) {
        val currentExercises = _exercises.value.toMutableList()
        if (exerciseIndex >= currentExercises.size) return
        
        val exercise = currentExercises[exerciseIndex]
        currentExercises[exerciseIndex] = exercise.copy(isExpanded = !exercise.isExpanded)
        _exercises.value = currentExercises
    }
    
    /**
     * Shows the exercise picker bottom sheet.
     */
    fun showExercisePicker() {
        _showExercisePicker.value = true
    }
    
    /**
     * Hides the exercise picker bottom sheet.
     */
    fun hideExercisePicker() {
        _showExercisePicker.value = false
    }
    
    /**
     * Adds a new exercise to the workout.
     */
    fun addExercise(exercise: Exercise) {
        scope.launch {
            val currentExercises = _exercises.value.toMutableList()
            val newOrderIndex = currentExercises.size
            
            // Get defaults from settings
            val defaultRestSeconds = settingsManager.getDefaultRestSecondsSync()
            val defaultSets = settingsManager.getDefaultSetsPerExerciseSync()
            val defaultShowRpe = settingsManager.getShowRpeByDefaultSync()
            
            // Create exercise log in database
            val exerciseLog = ExerciseLog(
                sessionId = sessionId,
                exerciseName = exercise.name,
                orderIndex = newOrderIndex,
                showRpe = defaultShowRpe
            )
            val exerciseLogId = database.sessionDao().insertExerciseLog(exerciseLog)
            
            // Create default sets based on settings
            val setLogs = mutableListOf<ActiveSet>()
            repeat(defaultSets) { setIndex ->
                val setLog = SetLog(
                    exerciseLogId = exerciseLogId,
                    setNumber = setIndex + 1,
                    restSeconds = defaultRestSeconds
                )
                val setId = database.sessionDao().insertSetLog(setLog)
                setLogs.add(
                    ActiveSet(
                        setLog = setLog.copy(id = setId),
                        setNumber = setIndex + 1,
                        weight = null,
                        reps = null,
                        isCompleted = false,
                        restSeconds = defaultRestSeconds
                    )
                )
            }
            
            // Update local state
            val newExercise = ActiveExercise(
                exerciseLog = exerciseLog.copy(id = exerciseLogId),
                exerciseName = exercise.name,
                restSeconds = defaultRestSeconds,
                showRpe = defaultShowRpe,
                sets = setLogs,
                isExpanded = true
            )
            
            currentExercises.add(newExercise)
            _exercises.value = currentExercises
            
            hideExercisePicker()
        }
    }
    
    /**
     * Reorders exercises in the workout.
     */
    fun reorderExercises(fromIndex: Int, toIndex: Int) {
        val currentExercises = _exercises.value.toMutableList()
        if (fromIndex < 0 || fromIndex >= currentExercises.size ||
            toIndex < 0 || toIndex >= currentExercises.size) return
        
        // Swap in local state
        val item = currentExercises.removeAt(fromIndex)
        currentExercises.add(toIndex, item)
        _exercises.value = currentExercises
        
        // Update order indices in database
        scope.launch {
            currentExercises.forEachIndexed { index, exercise ->
                if (exercise.exerciseLog.orderIndex != index) {
                    database.sessionDao().updateExerciseLog(
                        exercise.exerciseLog.copy(orderIndex = index)
                    )
                }
            }
        }
    }
    
    // Timer controls
    fun pauseTimer() = timerManager.pause()
    fun resumeTimer() = timerManager.resume()
    fun skipTimer() = timerManager.skip()
    fun addTimerTime() = timerManager.addTime(5)
    fun subtractTimerTime() = timerManager.subtractTime(5)
    
    /**
     * Finishes the workout.
     */
    suspend fun finishWorkout() {
        database.sessionDao().completeWorkout(sessionId)
        timerManager.cancel()
    }
    
    /**
     * Cancels the workout (deletes the session).
     */
    suspend fun cancelWorkout() {
        database.sessionDao().deleteSession(sessionId)
        timerManager.cancel()
    }
}
