package com.workout.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.SetType
import com.workout.app.ui.components.shared.NumericInputField
import com.workout.app.ui.components.shared.RestTimePicker
import com.workout.app.ui.components.shared.SetTypeIndicator
import com.workout.app.ui.components.shared.formatRestTime
import com.workout.app.ui.theme.DropSetColor
import com.workout.app.ui.theme.InputFieldBackground
import com.workout.app.ui.theme.InputFieldBorder
import com.workout.app.ui.theme.WarmupColor

/**
 * A row representing a single set in an exercise.
 * Shows set type indicator (clickable to cycle through types), previous values, weight input, reps input, and a checkbox.
 * Includes a rest timer row with clickable time picker (like the reference app).
 * 
 * @param setNumber The actual set index in the list
 * @param displayNumber The number to display for regular sets (only counts regular sets, not warmup/drop)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRow(
    setNumber: Int,
    displayNumber: Int,
    weight: Int?,
    reps: Int?,
    isCompleted: Boolean,
    previousWeight: Int? = null,
    previousReps: Int? = null,
    previousRpe: Float? = null,
    restSeconds: Int? = null,
    setType: SetType = SetType.REGULAR,
    @Suppress("UNUSED_PARAMETER") showRemoveButton: Boolean = false, // Deprecated - swipe to delete instead
    showRpe: Boolean = false,
    rpe: Float? = null,
    onWeightChange: (Int?) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestChange: (Int?) -> Unit = {},
    onSetTypeChange: (SetType) -> Unit = {},
    onRpeChange: (Float?) -> Unit = {},
    onCompleteClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onRemoveClick: () -> Unit = {}, // Deprecated - swipe to delete instead
    modifier: Modifier = Modifier
) {
    var showRestPicker by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Main set row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set type indicator - clickable to cycle through types
            SetTypeIndicator(
                setType = setType,
                displayNumber = displayNumber,
                enabled = !isCompleted,
                onSetTypeChange = onSetTypeChange
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Previous values (weight × reps)
            Text(
                text = if (previousWeight != null && previousReps != null) {
                    "$previousWeight × $previousReps"
                } else {
                    "—"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.width(64.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Weight input - with dark background and ghost text from previous
            NumericInputField(
                value = weight?.toString() ?: "",
                onValueChange = { onWeightChange(it.toIntOrNull()) },
                enabled = !isCompleted,
                placeholder = previousWeight?.toString(),
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reps input - with dark background and ghost text from previous
            NumericInputField(
                value = reps?.toString() ?: "",
                onValueChange = { onRepsChange(it.toIntOrNull()) },
                enabled = !isCompleted,
                placeholder = previousReps?.toString(),
                modifier = Modifier.weight(1f)
            )
            
            // RPE input - only shown if enabled for this exercise
            if (showRpe) {
                Spacer(modifier = Modifier.width(8.dp))
                
                RpeInputField(
                    value = rpe,
                    onValueChange = onRpeChange,
                    enabled = !isCompleted,
                    placeholder = previousRpe,
                    modifier = Modifier.weight(0.8f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Checkbox for completion - with border when unchecked
            // Clickable to toggle completion (can uncomplete by clicking again)
            // Can complete if either: has current values OR has previous values to use
            val hasValues = weight != null && reps != null
            val hasPreviousValues = previousWeight != null && previousReps != null
            val canComplete = hasValues || hasPreviousValues
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary 
                               else if (hasValues) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                               else if (hasPreviousValues) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                               else InputFieldBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = canComplete || isCompleted) {
                        onCompleteClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text(
                        text = "✓",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Rest timer row with horizontal line and clickable time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left divider
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = InputFieldBorder
            )
            
            // Rest time chip - clickable
            Surface(
                onClick = { showRestPicker = true },
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRestTime(restSeconds ?: 120),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Right divider
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = InputFieldBorder
            )
        }
    }
    
    // Rest time picker dialog
    if (showRestPicker) {
        RestTimePicker(
            currentSeconds = restSeconds ?: 120,
            onTimeSelected = { seconds ->
                onRestChange(seconds)
                showRestPicker = false
            },
            onDismiss = { showRestPicker = false }
        )
    }
}

/**
 * Custom dropdown picker for RPE (Rate of Perceived Exertion) values 5-10
 */
@Composable
private fun RpeInputField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    enabled: Boolean,
    placeholder: Float? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // RPE options: null (no value), 5, 6, 7, 8, 9, 10
    val rpeOptions = listOf<Float?>(null, 5f, 6f, 7f, 8f, 9f, 10f)
    
    val displayValue = value?.toInt()?.toString() ?: placeholder?.toInt()?.toString() ?: "—"
    val isPlaceholderValue = value == null && placeholder != null
    
    Box(modifier = modifier) {
        // The clickable field
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(InputFieldBackground)
                .border(
                    width = 1.dp,
                    color = InputFieldBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = enabled) { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                color = if (value != null) 
                    MaterialTheme.colorScheme.onSurface 
                else if (isPlaceholderValue)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        
        // Dropdown menu
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            rpeOptions.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = option?.toInt()?.toString() ?: "—",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == value) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
