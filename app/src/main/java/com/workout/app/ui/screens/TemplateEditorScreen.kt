package com.workout.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.SetType
import com.workout.app.ui.components.ExercisePicker
import com.workout.app.ui.theme.DarkBackground
import com.workout.app.ui.theme.DropSetColor
import com.workout.app.ui.theme.InputFieldBackground
import com.workout.app.ui.theme.InputFieldBorder
import com.workout.app.ui.theme.NeonCyan
import com.workout.app.ui.theme.WarmupColor
import com.workout.app.util.SettingsManager

/**
 * Data class for a template set being edited.
 */
data class EditableTemplateSet(
    val setNumber: Int,
    val targetWeight: Int? = null,
    val targetReps: Int? = null,
    val restSeconds: Int? = null,
    val setType: SetType = SetType.REGULAR
)

/**
 * Data class for an exercise being edited in the template.
 */
data class EditableExercise(
    val id: Long = 0L,
    val exerciseName: String,
    val targetSets: Int = 3,
    val restSeconds: Int? = null,
    val showRpe: Boolean = false,
    val orderIndex: Int = 0,
    val sets: List<EditableTemplateSet> = (1..3).map { EditableTemplateSet(setNumber = it, restSeconds = restSeconds) }
)

/**
 * Template Editor screen - create or edit a workout template.
 * 
 * @param templateId ID of template being edited (null for new template)
 * @param initialName Initial template name
 * @param initialExercises Initial list of exercises
 * @param onSave Callback when template is saved
 * @param onBack Callback when navigating back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    templateId: Long? = null,
    initialName: String = "",
    initialExercises: List<EditableExercise> = emptyList(),
    onSave: (name: String, exercises: List<EditableExercise>) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    settingsManager: SettingsManager? = null,
    customExercises: kotlinx.coroutines.flow.Flow<List<com.workout.app.data.entities.CustomExercise>> = kotlinx.coroutines.flow.flowOf(emptyList()),
    onCreateCustomExercise: (com.workout.app.data.entities.CustomExercise) -> Unit = {},
    onDeleteCustomExercise: (com.workout.app.data.entities.CustomExercise) -> Unit = {}
) {
    var templateName by remember { mutableStateOf(initialName) }
    val exercises = remember { mutableStateListOf<EditableExercise>().apply { addAll(initialExercises) } }
    var showExercisePicker by remember { mutableStateOf(false) }
    
    // Get defaults from settings
    val defaultRestSeconds = settingsManager?.getDefaultRestSecondsSync() ?: 90
    val defaultSets = settingsManager?.getDefaultSetsPerExerciseSync() ?: 3
    val defaultShowRpe = settingsManager?.getShowRpeByDefaultSync() ?: false
    
    val isNewTemplate = templateId == null
    val canSave = templateName.isNotBlank() && exercises.isNotEmpty()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isNewTemplate) "New Template" else "Edit Template",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSave(templateName, exercises.toList()) },
                        enabled = canSave
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (canSave) {
                                NeonCyan
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showExercisePicker = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Add Exercise",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Template name field
            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("Template Name") },
                placeholder = { Text("e.g., Push Day, Leg Day") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            
            if (exercises.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
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
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No Exercises Yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Tap the button below to add exercises to your template",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
                    itemsIndexed(
                        items = exercises,
                        key = { index, exercise -> "${exercise.exerciseName}_$index" }
                    ) { index, exercise ->
                        EditableExerciseCard(
                            exercise = exercise,
                            isFirst = index == 0,
                            isLast = index == exercises.lastIndex,
                            onMoveUp = {
                                if (index > 0) {
                                    val temp = exercises[index - 1]
                                    exercises[index - 1] = exercise.copy(orderIndex = index - 1)
                                    exercises[index] = temp.copy(orderIndex = index)
                                }
                            },
                            onMoveDown = {
                                if (index < exercises.lastIndex) {
                                    val temp = exercises[index + 1]
                                    exercises[index + 1] = exercise.copy(orderIndex = index + 1)
                                    exercises[index] = temp.copy(orderIndex = index)
                                }
                            },
                            onShowRpeChange = { showRpe ->
                                exercises[index] = exercise.copy(showRpe = showRpe)
                            },
                            onSetChange = { setIndex, updatedSet ->
                                val newSets = exercise.sets.toMutableList()
                                newSets[setIndex] = updatedSet
                                exercises[index] = exercise.copy(sets = newSets)
                            },
                            onAddSet = {
                                val lastSet = exercise.sets.lastOrNull()
                                val newSet = EditableTemplateSet(
                                    setNumber = exercise.sets.size + 1,
                                    restSeconds = lastSet?.restSeconds ?: 90
                                )
                                exercises[index] = exercise.copy(
                                    sets = exercise.sets + newSet,
                                    targetSets = exercise.sets.size + 1
                                )
                            },
                            onRemoveSet = { setIndex ->
                                if (exercise.sets.size > 1) {
                                    val newSets = exercise.sets.toMutableList()
                                    newSets.removeAt(setIndex)
                                    // Renumber sets
                                    val renumberedSets = newSets.mapIndexed { i, set ->
                                        set.copy(setNumber = i + 1)
                                    }
                                    exercises[index] = exercise.copy(
                                        sets = renumberedSets,
                                        targetSets = renumberedSets.size
                                    )
                                }
                            },
                            onDelete = {
                                exercises.removeAt(index)
                                // Update order indices
                                exercises.forEachIndexed { i, ex ->
                                    exercises[i] = ex.copy(orderIndex = i)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Exercise picker bottom sheet
    ExercisePicker(
        isVisible = showExercisePicker,
        onDismiss = { showExercisePicker = false },
        onExerciseSelected = { exercise ->
            exercises.add(
                EditableExercise(
                    exerciseName = exercise.name,
                    targetSets = defaultSets,
                    restSeconds = defaultRestSeconds,
                    showRpe = defaultShowRpe,
                    orderIndex = exercises.size,
                    sets = (1..defaultSets).map { 
                        EditableTemplateSet(
                            setNumber = it,
                            restSeconds = defaultRestSeconds
                        )
                    }
                )
            )
            showExercisePicker = false
        },
        selectedExercises = exercises.map { it.exerciseName },
        customExercises = customExercises,
        onCreateCustomExercise = onCreateCustomExercise,
        onDeleteCustomExercise = onDeleteCustomExercise
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableExerciseCard(
    exercise: EditableExercise,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onShowRpeChange: (Boolean) -> Unit,
    onSetChange: (setIndex: Int, set: EditableTemplateSet) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (setIndex: Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRestPicker by remember { mutableStateOf<Int?>(null) } // setIndex when showing
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // Reorder and delete
                Row {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isFirst,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            modifier = Modifier.size(20.dp),
                            tint = if (!isFirst) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            }
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLast,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            modifier = Modifier.size(20.dp),
                            tint = if (!isLast) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            }
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Set column
                Text(
                    text = "Set",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Weight column
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Reps column
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Remove button spacer
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Set rows
            var regularSetCounter = 0
            exercise.sets.forEachIndexed { setIndex, set ->
                val displayNumber = if (set.setType == SetType.REGULAR) {
                    regularSetCounter++
                    regularSetCounter
                } else {
                    0
                }
                
                TemplateSetRow(
                    set = set,
                    displayNumber = displayNumber,
                    canRemove = exercise.sets.size > 1,
                    onSetTypeChange = { newType ->
                        onSetChange(setIndex, set.copy(setType = newType))
                    },
                    onWeightChange = { weight ->
                        onSetChange(setIndex, set.copy(targetWeight = weight))
                    },
                    onRepsChange = { reps ->
                        onSetChange(setIndex, set.copy(targetReps = reps))
                    },
                    onRestClick = {
                        showRestPicker = setIndex
                    },
                    onRemove = { onRemoveSet(setIndex) },
                    restSeconds = set.restSeconds
                )
            }
            
            // Add set button
            Surface(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Set",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // RPE Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Track RPE",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    onClick = { onShowRpeChange(!exercise.showRpe) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (exercise.showRpe) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .width(52.dp)
                        .height(28.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = if (exercise.showRpe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (exercise.showRpe) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
    
    // Rest time picker dialog
    showRestPicker?.let { setIndex ->
        val currentSet = exercise.sets.getOrNull(setIndex)
        if (currentSet != null) {
            TemplateRestTimePicker(
                currentSeconds = currentSet.restSeconds ?: 90,
                onTimeSelected = { seconds ->
                    onSetChange(setIndex, currentSet.copy(restSeconds = seconds))
                    showRestPicker = null
                },
                onDismiss = { showRestPicker = null }
            )
        }
    }
}

@Composable
private fun TemplateSetRow(
    set: EditableTemplateSet,
    displayNumber: Int,
    canRemove: Boolean,
    onSetTypeChange: (SetType) -> Unit,
    onWeightChange: (Int?) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestClick: () -> Unit,
    onRemove: () -> Unit,
    restSeconds: Int?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Main row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set type indicator - clickable
            val (typeText, typeColor) = when (set.setType) {
                SetType.REGULAR -> "$displayNumber" to NeonCyan
                SetType.WARMUP -> "W" to WarmupColor
                SetType.DROP_SET -> "D" to DropSetColor
            }
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = typeColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        val nextType = when (set.setType) {
                            SetType.REGULAR -> SetType.WARMUP
                            SetType.WARMUP -> SetType.DROP_SET
                            SetType.DROP_SET -> SetType.REGULAR
                        }
                        onSetTypeChange(nextType)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = typeColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Weight input
            TemplateInputField(
                value = set.targetWeight?.toString() ?: "",
                onValueChange = { onWeightChange(it.toIntOrNull()) },
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reps input
            TemplateInputField(
                value = set.targetReps?.toString() ?: "",
                onValueChange = { onRepsChange(it.toIntOrNull()) },
                modifier = Modifier.weight(1f)
            )
            
            // Remove button
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove set",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
        }
        
        // Rest timer row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left divider
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(InputFieldBorder)
            )
            
            // Rest time chip - clickable
            Surface(
                onClick = onRestClick,
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRestTime(restSeconds ?: 90),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                }
            }
            
            // Right divider
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(InputFieldBorder)
            )
        }
    }
}

@Composable
private fun TemplateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(InputFieldBackground)
            .border(
                width = 1.dp,
                color = InputFieldBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

private fun formatRestTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateRestTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTime by remember { mutableStateOf(currentSeconds) }
    
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rest Time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time display
                Text(
                    text = formatRestTime(selectedTime),
                    style = MaterialTheme.typography.displaySmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick select buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(60, 90, 120, 180).forEach { seconds ->
                        Surface(
                            onClick = { selectedTime = seconds },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTime == seconds) {
                                NeonCyan.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = formatRestTime(seconds),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selectedTime == seconds) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onTimeSelected(selectedTime) }) {
                        Text("Set", color = NeonCyan)
                    }
                }
            }
        }
    }
}
