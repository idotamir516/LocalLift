package com.workout.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workout.app.data.dao.ExerciseWithSets
import com.workout.app.data.dao.SessionWithDetails
import com.workout.app.data.entities.CustomExercise
import com.workout.app.data.entities.ExerciseLog
import com.workout.app.data.entities.SetLog
import com.workout.app.data.entities.SetType
import com.workout.app.ui.components.ExercisePicker
import com.workout.app.ui.theme.DarkBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Mutable state holder for editing a set
 */
data class EditableSetLog(
    val id: Long = 0,
    val exerciseLogId: Long = 0,
    val setNumber: Int,
    var reps: Int? = null,
    var weightLbs: Float? = null,
    var restSeconds: Int? = null,
    var rpe: Float? = null,
    var setType: SetType = SetType.REGULAR,
    val completedAt: Long? = null
)

/**
 * Mutable state holder for editing an exercise
 */
class EditableExerciseLog(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseName: String,
    val showRpe: Boolean = false,
    var orderIndex: Int,
    sets: List<EditableSetLog> = emptyList()
) {
    val sets = mutableStateListOf<EditableSetLog>().apply { addAll(sets) }
}

/**
 * Edit Workout Screen - allows editing past workout data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    sessionWithDetails: SessionWithDetails?,
    customExercises: Flow<List<CustomExercise>> = flowOf(emptyList()),
    onCreateCustomExercise: (CustomExercise) -> Unit = {},
    onDeleteCustomExercise: (CustomExercise) -> Unit = {},
    onSave: (exercises: List<EditableExerciseLog>, startedAt: Long, completedAt: Long?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit = {}
) {
    // Editable time state
    var startedAt by remember(sessionWithDetails) { 
        mutableStateOf(sessionWithDetails?.session?.startedAt ?: System.currentTimeMillis()) 
    }
    var completedAt by remember(sessionWithDetails) { 
        mutableStateOf(sessionWithDetails?.session?.completedAt) 
    }
    
    // Convert to editable state
    val exercises = remember(sessionWithDetails) {
        mutableStateListOf<EditableExerciseLog>().apply {
            sessionWithDetails?.exercises?.forEach { exerciseWithSets ->
                add(
                    EditableExerciseLog(
                        id = exerciseWithSets.exerciseLog.id,
                        sessionId = exerciseWithSets.exerciseLog.sessionId,
                        exerciseName = exerciseWithSets.exerciseLog.exerciseName,
                        showRpe = exerciseWithSets.exerciseLog.showRpe,
                        orderIndex = exerciseWithSets.exerciseLog.orderIndex,
                        sets = exerciseWithSets.sets.map { setLog ->
                            EditableSetLog(
                                id = setLog.id,
                                exerciseLogId = setLog.exerciseLogId,
                                setNumber = setLog.setNumber,
                                reps = setLog.reps,
                                weightLbs = setLog.weightLbs,
                                restSeconds = setLog.restSeconds,
                                rpe = setLog.rpe,
                                setType = setLog.setType,
                                completedAt = setLog.completedAt
                            )
                        }
                    )
                )
            }
        }
    }
    
    var showExercisePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Edit Workout",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSave(exercises.toList(), startedAt, completedAt) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (sessionWithDetails == null) {
            // Loading state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date and time editor card
                item {
                    WorkoutDateTimeCard(
                        startedAt = startedAt,
                        completedAt = completedAt,
                        onEditDate = { showDatePicker = true },
                        onEditStartTime = { showStartTimePicker = true },
                        onEditEndTime = { showEndTimePicker = true }
                    )
                }
                
                // Exercises
                itemsIndexed(
                    items = exercises,
                    key = { _, exercise -> exercise.id }
                ) { index, exercise ->
                    EditableExerciseCard(
                        exercise = exercise,
                        onDeleteExercise = {
                            exercises.removeAt(index)
                            // Re-index remaining exercises
                            exercises.forEachIndexed { i, ex -> ex.orderIndex = i }
                        },
                        onAddSet = {
                            val newSetNumber = exercise.sets.size + 1
                            val lastSet = exercise.sets.lastOrNull()
                            exercise.sets.add(
                                EditableSetLog(
                                    setNumber = newSetNumber,
                                    weightLbs = lastSet?.weightLbs,
                                    reps = lastSet?.reps,
                                    restSeconds = lastSet?.restSeconds ?: 90,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        },
                        onDeleteSet = { setIndex ->
                            exercise.sets.removeAt(setIndex)
                            // Renumber remaining sets
                            exercise.sets.forEachIndexed { i, set ->
                                exercise.sets[i] = set.copy(setNumber = i + 1)
                            }
                        },
                        onUpdateSet = { setIndex, updatedSet ->
                            exercise.sets[setIndex] = updatedSet
                        }
                    )
                }
                
                // Add exercise button
                item {
                    Button(
                        onClick = { showExercisePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Exercise")
                    }
                }
            }
        }
    }
    
    // Exercise picker
    ExercisePicker(
        isVisible = showExercisePicker,
        onDismiss = { showExercisePicker = false },
        onExerciseSelected = { selectedExercise ->
            val newOrderIndex = exercises.size
            exercises.add(
                EditableExerciseLog(
                    sessionId = sessionWithDetails?.session?.id ?: 0,
                    exerciseName = selectedExercise.name,
                    showRpe = false,
                    orderIndex = newOrderIndex,
                    sets = listOf(
                        EditableSetLog(
                            setNumber = 1,
                            reps = null,
                            weightLbs = null,
                            restSeconds = 90,
                            completedAt = System.currentTimeMillis()
                        )
                    )
                )
            )
            showExercisePicker = false
        },
        selectedExercises = exercises.map { it.exerciseName },
        customExercises = customExercises,
        onCreateCustomExercise = onCreateCustomExercise,
        onDeleteCustomExercise = onDeleteCustomExercise
    )
    
    // Date picker dialog
    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = startedAt }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startedAt
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        // Keep the time of day, just change the date
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = startedAt }
                        val newCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                        
                        val timeDiff = startedAt - (completedAt ?: startedAt)
                        startedAt = newCalendar.timeInMillis
                        completedAt = completedAt?.let { newCalendar.timeInMillis - timeDiff }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Start time picker dialog
    if (showStartTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = startedAt }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val newCalendar = Calendar.getInstance().apply { timeInMillis = startedAt }
                newCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                newCalendar.set(Calendar.MINUTE, timePickerState.minute)
                startedAt = newCalendar.timeInMillis
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
    
    // End time picker dialog
    if (showEndTimePicker) {
        val endTime = completedAt ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = endTime }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val newCalendar = Calendar.getInstance().apply { timeInMillis = startedAt }
                newCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                newCalendar.set(Calendar.MINUTE, timePickerState.minute)
                completedAt = newCalendar.timeInMillis
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/**
 * Card showing workout date and time with edit buttons
 */
@Composable
private fun WorkoutDateTimeCard(
    startedAt: Long,
    completedAt: Long?,
    onEditDate: () -> Unit,
    onEditStartTime: () -> Unit,
    onEditEndTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditDate() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(startedAt)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            // Times row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start time
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = onEditStartTime
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Started",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeFormat.format(Date(startedAt)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // End time
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = onEditEndTime
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Finished",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = completedAt?.let { timeFormat.format(Date(it)) } ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Time Picker Dialog wrapper
 */
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableExerciseCard(
    exercise: EditableExerciseLog,
    onDeleteExercise: () -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onUpdateSet: (Int, EditableSetLog) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Exercise header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDeleteExercise,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exercise",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = "Weight (lbs)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                // Space for delete button
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Set rows
            exercise.sets.forEachIndexed { index, set ->
                EditableSetRow(
                    set = set,
                    onUpdate = { updatedSet -> onUpdateSet(index, updatedSet) },
                    onDelete = { onDeleteSet(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Add set button
            Button(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun EditableSetRow(
    set: EditableSetLog,
    onUpdate: (EditableSetLog) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weightText by remember(set) { mutableStateOf(set.weightLbs?.toString() ?: "") }
    var repsText by remember(set) { mutableStateOf(set.reps?.toString() ?: "") }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set number
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${set.setNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Weight input
            OutlinedTextField(
                value = weightText,
                onValueChange = { newValue ->
                    weightText = newValue
                    val weight = newValue.toFloatOrNull()
                    onUpdate(set.copy(weightLbs = weight))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reps input
            OutlinedTextField(
                value = repsText,
                onValueChange = { newValue ->
                    repsText = newValue
                    val reps = newValue.toIntOrNull()
                    onUpdate(set.copy(reps = reps))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Set",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
