package com.workout.app.ui.components

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Data class representing a set within an exercise.
 */
import com.workout.app.data.entities.SetType

/**
 * Data class representing a set within an exercise.
 */
data class SetData(
    val id: Long,  // Stable database ID for composition key
    val setNumber: Int,
    val weight: Int? = null,
    val reps: Int? = null,
    val rpe: Float? = null,
    val isCompleted: Boolean = false,
    val previousWeight: Int? = null,
    val previousReps: Int? = null,
    val previousRpe: Float? = null,
    val restSeconds: Int? = null,
    val setType: SetType = SetType.REGULAR
)

/**
 * A card component displaying an exercise with its sets.
 * Can be expanded/collapsed to show/hide sets.
 * 
 * @param exerciseName Name of the exercise
 * @param sets List of sets for this exercise (each set has its own rest time)
 * @param isExpanded Whether the card is expanded to show sets
 * @param isFirst Whether this is the first exercise (for move up)
 * @param isLast Whether this is the last exercise (for move down)
 * @param showRemoveSetButton Whether to show remove button on sets
 * @param note Optional note for this exercise
 * @param onExpandToggle Callback when expand/collapse is toggled
 * @param onSetWeightChange Callback when a set's weight changes
 * @param onSetRepsChange Callback when a set's reps changes
 * @param onSetRestChange Callback when a set's rest time changes
 * @param onSetTypeChange Callback when a set's type changes (Regular/Warmup/Drop)
 * @param onSetComplete Callback when a set is marked complete
 * @param onSetRemove Callback when a set is removed (by stable id)
 * @param onAddSet Callback to add a new set
 * @param onExerciseNameClick Callback when exercise name is clicked (for details popup)
 * @param onNoteChange Callback when note is changed
 * @param onMoveUp Callback to move exercise up
 * @param onMoveDown Callback to move exercise down
 * @param modifier Optional modifier for the card
 */
@Composable
fun ExerciseCard(
    exerciseName: String,
    sets: List<SetData>,
    isExpanded: Boolean = true,
    isFirst: Boolean = true,
    isLast: Boolean = true,
    showRemoveSetButton: Boolean = false,
    showRpe: Boolean = false,
    note: String? = null,
    onExpandToggle: () -> Unit = {},
    onSetWeightChange: (setNumber: Int, weight: Int?) -> Unit,
    onSetRepsChange: (setNumber: Int, reps: Int?) -> Unit,
    onSetRestChange: (setNumber: Int, restSeconds: Int?) -> Unit = { _, _ -> },
    onSetTypeChange: (setNumber: Int, setType: SetType) -> Unit = { _, _ -> },
    onSetRpeChange: (setNumber: Int, rpe: Float?) -> Unit = { _, _ -> },
    onSetComplete: (setNumber: Int) -> Unit,
    onSetRemove: (setId: Long) -> Unit = {},
    onAddSet: () -> Unit,
    onExerciseNameClick: () -> Unit = {},
    onNoteChange: (String?) -> Unit = {},
    onRemoveExercise: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedSets = sets.count { it.isCompleted }
    val totalSets = sets.size
    val progress = if (totalSets > 0) completedSets.toFloat() / totalSets else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with exercise name and progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise icon with gradient background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (completedSets == totalSets && totalSets > 0) {
                                    listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (completedSets == totalSets && totalSets > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onExerciseNameClick() }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Progress bar with text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (completedSets == totalSets && totalSets > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        
                        Text(
                            text = "$completedSets/$totalSets",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (completedSets == totalSets && totalSets > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Expand/collapse and reorder
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
                    
                    // Delete exercise button
                    IconButton(
                        onClick = onRemoveExercise,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove exercise",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Note section (expandable)
            var showNoteEditor by remember { mutableStateOf(false) }
            var editedNote by remember(note) { mutableStateOf(note ?: "") }
            
            if (note != null || showNoteEditor) {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (showNoteEditor) {
                    // Editable note field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedNote,
                            onValueChange = { editedNote = it },
                            placeholder = { Text("Add a note...", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall,
                            minLines = 1,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        IconButton(
                            onClick = {
                                val trimmed = editedNote.trim().takeIf { it.isNotEmpty() }
                                onNoteChange(trimmed)
                                showNoteEditor = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save note",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Display note with edit option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .clickable { showNoteEditor = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = note ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit note",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // Add note button
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showNoteEditor = true }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add note",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Expanded content with sets
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Column headers matching reference design
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
                    // Previous column
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.width(80.dp),
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
                    
                    // RPE column (only if enabled)
                    if (showRpe) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "RPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Checkmark column
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Calculate display numbers for regular sets only
                // Warmup and drop sets don't count toward the regular set number
                var regularSetCounter = 0
                
                // Set rows with swipe-to-delete
                sets.forEach { setData ->
                    // Calculate display number: only increment for regular sets
                    val displayNumber = if (setData.setType == SetType.REGULAR) {
                        regularSetCounter++
                        regularSetCounter
                    } else {
                        0 // Won't be displayed for W/D sets
                    }
                    
                    // Use stable database ID as key to ensure each set has its own independent dismiss state
                    // This prevents issues when sets are renumbered after deletion
                    key(setData.id) {
                        SwipeableSetRow(
                            setData = setData,
                            displayNumber = displayNumber,
                            showRpe = showRpe,
                            onSetWeightChange = onSetWeightChange,
                            onSetRepsChange = onSetRepsChange,
                            onSetRestChange = onSetRestChange,
                            onSetTypeChange = onSetTypeChange,
                            onSetRpeChange = onSetRpeChange,
                            onSetComplete = onSetComplete,
                            onSetRemove = onSetRemove
                        )
                    }
                }
                
                // Add set button - more minimal
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
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Add Set",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * A swipeable wrapper for SetRow that allows swipe-to-delete functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableSetRow(
    setData: SetData,
    displayNumber: Int,
    showRpe: Boolean,
    onSetWeightChange: (setNumber: Int, weight: Int?) -> Unit,
    onSetRepsChange: (setNumber: Int, reps: Int?) -> Unit,
    onSetRestChange: (setNumber: Int, restSeconds: Int?) -> Unit,
    onSetTypeChange: (setNumber: Int, setType: SetType) -> Unit,
    onSetRpeChange: (setNumber: Int, rpe: Float?) -> Unit,
    onSetComplete: (setNumber: Int) -> Unit,
    onSetRemove: (setId: Long) -> Unit
) {
    Log.d("SwipeDebug", "SwipeableSetRow composing: id=${setData.id}, setNumber=${setData.setNumber}")
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            Log.d("SwipeDebug", "confirmValueChange called: id=${setData.id}, setNumber=${setData.setNumber}, dismissValue=$dismissValue")
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                Log.d("SwipeDebug", "Calling onSetRemove for id=${setData.id}")
                onSetRemove(setData.id)
            }
            // Always return false so the UI resets to non-dismissed state
            // The actual deletion happens via onSetRemove callback above
            Log.d("SwipeDebug", "confirmValueChange returning false")
            false
        }
    )
    
    // Log the current dismiss state
    Log.d("SwipeDebug", "Current dismissState: id=${setData.id}, currentValue=${dismissState.currentValue}, targetValue=${dismissState.targetValue}")
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
                // Delete background shown when swiping
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete set",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            SetRow(
                setNumber = setData.setNumber,
                displayNumber = displayNumber,
                weight = setData.weight,
                reps = setData.reps,
                isCompleted = setData.isCompleted,
                previousWeight = setData.previousWeight,
                previousReps = setData.previousReps,
                previousRpe = setData.previousRpe,
                restSeconds = setData.restSeconds,
                setType = setData.setType,
                showRemoveButton = false,
                showRpe = showRpe,
                rpe = setData.rpe,
                onWeightChange = { weight ->
                    onSetWeightChange(setData.setNumber, weight)
                },
                onRepsChange = { reps ->
                    onSetRepsChange(setData.setNumber, reps)
                },
                onRestChange = { restSeconds ->
                    onSetRestChange(setData.setNumber, restSeconds)
                },
                onSetTypeChange = { newType ->
                    onSetTypeChange(setData.setNumber, newType)
                },
                onRpeChange = { rpe ->
                    onSetRpeChange(setData.setNumber, rpe)
                },
                onCompleteClick = {
                    onSetComplete(setData.setNumber)
                },
                onRemoveClick = { },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 8.dp)
            )
        }
    }
