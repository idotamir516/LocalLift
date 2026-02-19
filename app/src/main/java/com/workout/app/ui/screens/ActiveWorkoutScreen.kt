package com.workout.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.ui.components.ExerciseCard
import com.workout.app.ui.components.RestTimerDisplay
import com.workout.app.ui.components.SetData
import com.workout.app.ui.components.shared.ConfirmationDialog
import com.workout.app.util.TimerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

/**
 * Data class representing an exercise with its sets during an active workout.
 */
data class ActiveExercise(
    val exerciseLog: ExerciseLog,
    val exerciseName: String,
    val restSeconds: Int? = null,
    val showRpe: Boolean = false,
    val sets: List<ActiveSet> = emptyList(),
    val isExpanded: Boolean = true,
    val templateExerciseId: Long? = null,
    val note: String? = null
)

/**
 * Data class representing a set during an active workout.
 */
data class ActiveSet(
    val setLog: SetLog,
    val setNumber: Int,
    val weight: Int? = null,
    val reps: Int? = null,
    val rpe: Float? = null,
    val isCompleted: Boolean = false,
    val previousWeight: Int? = null,
    val previousReps: Int? = null,
    val previousRpe: Float? = null,
    val restSeconds: Int? = null,
    val setType: com.workout.app.data.entities.SetType = com.workout.app.data.entities.SetType.REGULAR
)

/**
 * Active Workout screen - displays the current workout with exercise cards and set logging.
 * 
 * @param session The current workout session
 * @param exercises List of exercises with their sets
 * @param timerState Current state of the rest timer
 * @param onSetWeightChange Callback when a set's weight changes
 * @param onSetRepsChange Callback when a set's reps changes
 * @param onSetTypeChange Callback when a set's type changes (Regular/Warmup/Drop)
 * @param onSetComplete Callback when a set is marked complete
 * @param onAddSet Callback to add a set to an exercise
 * @param onRemoveSet Callback to remove a set from an exercise
 * @param onAddExercise Callback to add a new exercise
 * @param onMoveExercise Callback to reorder exercises (fromIndex, toIndex)
 * @param onToggleExpand Callback to expand/collapse an exercise card
 * @param onTimerPause Callback when timer is paused
 * @param onTimerResume Callback when timer is resumed
 * @param onTimerSkip Callback when timer is skipped
 * @param onTimerAddTime Callback to add 5 seconds to timer
 * @param onTimerSubtractTime Callback to subtract 5 seconds from timer
 * @param onFinishWorkout Callback when workout is finished
 * @param onCancelWorkout Callback when workout is cancelled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    session: WorkoutSession? = null,
    exercises: List<ActiveExercise> = emptyList(),
    timerState: TimerState = TimerState(),
    timerStartsExpanded: Boolean = true,
    snackbarEvent: SharedFlow<SnackbarEvent>? = null,
    onSetWeightChange: (exerciseIndex: Int, setNumber: Int, weight: Int?) -> Unit = { _, _, _ -> },
    onSetRepsChange: (exerciseIndex: Int, setNumber: Int, reps: Int?) -> Unit = { _, _, _ -> },
    onSetRestChange: (exerciseIndex: Int, setNumber: Int, restSeconds: Int?) -> Unit = { _, _, _ -> },
    onSetTypeChange: (exerciseIndex: Int, setNumber: Int, setType: com.workout.app.data.entities.SetType) -> Unit = { _, _, _ -> },
    onSetRpeChange: (exerciseIndex: Int, setNumber: Int, rpe: Float?) -> Unit = { _, _, _ -> },
    onSetComplete: (exerciseIndex: Int, setNumber: Int) -> Unit = { _, _ -> },
    onAddSet: (exerciseIndex: Int) -> Unit = {},
    onRemoveSet: (exerciseIndex: Int, setId: Long) -> Unit = { _, _ -> },
    onUndoSetRemove: () -> Unit = {},
    onAddExercise: () -> Unit = {},
    onRemoveExercise: (exerciseIndex: Int) -> Unit = {},
    onMoveExercise: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onToggleExpand: (exerciseIndex: Int) -> Unit = {},
    onExerciseNoteChange: (exerciseIndex: Int, note: String?) -> Unit = { _, _ -> },
    onTimerPause: () -> Unit = {},
    onTimerResume: () -> Unit = {},
    onTimerSkip: () -> Unit = {},
    onTimerAddTime: () -> Unit = {},
    onTimerSubtractTime: () -> Unit = {},
    onFinishWorkout: () -> Unit = {},
    onCancelWorkout: () -> Unit = {},
    onExerciseNameClick: (exerciseName: String) -> Unit = {}
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle snackbar events
    LaunchedEffect(snackbarEvent) {
        snackbarEvent?.collect { event ->
            when (event) {
                is SnackbarEvent.ShowUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onUndoSetRemove()
                    }
                }
                is SnackbarEvent.Dismiss -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
    
    // Workout duration timer - updates every second
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(session?.startedAt) {
        session?.startedAt?.let { startTime ->
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }
    
    // Format elapsed time as HH:MM:SS or MM:SS
    val formattedDuration = remember(elapsedSeconds) {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            session?.templateName ?: "Workout",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // Workout duration timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDuration,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showCancelDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel workout"
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showFinishDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Finish",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExercise,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Rest timer display (when active)
            if (timerState.isRunning || timerState.remainingSeconds > 0) {
                RestTimerDisplay(
                    timerState = timerState,
                    onPause = onTimerPause,
                    onResume = onTimerResume,
                    onSkip = onTimerSkip,
                    onAddTime = onTimerAddTime,
                    onSubtractTime = onTimerSubtractTime,
                    startExpanded = timerStartsExpanded,
                    modifier = Modifier.padding(16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (exercises.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add an exercise to get started",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Exercise list
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(exercises) { index, exercise ->
                        ExerciseCard(
                            exerciseName = exercise.exerciseName,
                            sets = exercise.sets.map { set ->
                                SetData(
                                    id = set.setLog.id,
                                    setNumber = set.setNumber,
                                    weight = set.weight,
                                    reps = set.reps,
                                    rpe = set.rpe,
                                    isCompleted = set.isCompleted,
                                    previousWeight = set.previousWeight,
                                    previousReps = set.previousReps,
                                    previousRpe = set.previousRpe,
                                    restSeconds = set.restSeconds,
                                    setType = set.setType
                                )
                            },
                            isExpanded = exercise.isExpanded,
                            isFirst = index == 0,
                            isLast = index == exercises.lastIndex,
                            showRemoveSetButton = true,
                            showRpe = exercise.showRpe,
                            note = exercise.note,
                            onExpandToggle = { onToggleExpand(index) },
                            onSetWeightChange = { setNumber, weight ->
                                onSetWeightChange(index, setNumber, weight)
                            },
                            onSetRepsChange = { setNumber, reps ->
                                onSetRepsChange(index, setNumber, reps)
                            },
                            onSetRestChange = { setNumber, restSeconds ->
                                onSetRestChange(index, setNumber, restSeconds)
                            },
                            onSetTypeChange = { setNumber, setType ->
                                onSetTypeChange(index, setNumber, setType)
                            },
                            onSetRpeChange = { setNumber, rpe ->
                                onSetRpeChange(index, setNumber, rpe)
                            },
                            onSetComplete = { setNumber ->
                                onSetComplete(index, setNumber)
                            },
                            onSetRemove = { setId ->
                                onRemoveSet(index, setId)
                            },
                            onAddSet = { onAddSet(index) },
                            onExerciseNameClick = { onExerciseNameClick(exercise.exerciseName) },
                            onNoteChange = { note -> onExerciseNoteChange(index, note) },
                            onRemoveExercise = { onRemoveExercise(index) },
                            onMoveUp = {
                                if (index > 0) onMoveExercise(index, index - 1)
                            },
                            onMoveDown = {
                                if (index < exercises.lastIndex) onMoveExercise(index, index + 1)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog) {
        ConfirmationDialog(
            title = "Cancel Workout?",
            message = "Your progress will be lost. Are you sure you want to cancel?",
            confirmLabel = "Cancel Workout",
            dismissLabel = "Keep Going",
            isDestructive = true,
            onConfirm = {
                showCancelDialog = false
                onCancelWorkout()
            },
            onDismiss = { showCancelDialog = false }
        )
    }
    
    // Finish confirmation dialog
    if (showFinishDialog) {
        ConfirmationDialog(
            title = "Finish Workout?",
            message = "Mark this workout as complete?",
            confirmLabel = "Finish",
            dismissLabel = "Continue",
            onConfirm = {
                showFinishDialog = false
                onFinishWorkout()
            },
            onDismiss = { showFinishDialog = false }
        )
    }
}
