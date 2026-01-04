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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.workout.app.ui.theme.DropSetColor
import com.workout.app.ui.theme.InputFieldBackground
import com.workout.app.ui.theme.InputFieldBorder
import com.workout.app.ui.theme.WarmupColor

/**
 * Format seconds to mm:ss display
 */
private fun formatRestTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

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
            val (typeText, typeColor) = when (setType) {
                SetType.REGULAR -> "$displayNumber" to MaterialTheme.colorScheme.primary
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
                    .clickable(enabled = !isCompleted) {
                        // Cycle through set types: Regular -> Warmup -> Drop Set -> Regular
                        val nextType = when (setType) {
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
            InputField(
                value = weight?.toString() ?: "",
                onValueChange = { onWeightChange(it.toIntOrNull()) },
                enabled = !isCompleted,
                placeholder = previousWeight?.toString(),
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reps input - with dark background and ghost text from previous
            InputField(
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
 * Custom input field with dark background for better contrast
 */
@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
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
                // Only allow numbers
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                            text = placeholder ?: "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (placeholder != null) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
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

/**
 * A rest time picker dialog with quick presets and custom option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Quick preset options (most common rest times)
    val presets = listOf(30, 60, 90, 120, 180)
    
    var showCustomPicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(currentSeconds) }
    
    // Check if current value is a preset or custom
    val isCustomValue = currentSeconds !in presets
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Rest Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                if (showCustomPicker) {
                    // Custom time picker with scrollable list
                    CustomTimePicker(
                        currentSeconds = selectedTime,
                        onTimeSelected = { time ->
                            selectedTime = time
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Back button
                        Surface(
                            onClick = { showCustomPicker = false },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                        
                        // Confirm button
                        Surface(
                            onClick = { onTimeSelected(selectedTime) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Set ${formatRestTime(selectedTime)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    // Quick preset chips
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: 30s, 1:00, 1:30
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presets.take(3).forEach { seconds ->
                                PresetChip(
                                    text = formatRestTime(seconds),
                                    isSelected = currentSeconds == seconds,
                                    onClick = { onTimeSelected(seconds) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        // Row 2: 2:00, 3:00
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presets.drop(3).forEach { seconds ->
                                PresetChip(
                                    text = formatRestTime(seconds),
                                    isSelected = currentSeconds == seconds,
                                    onClick = { onTimeSelected(seconds) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Custom button
                            PresetChip(
                                text = if (isCustomValue) formatRestTime(currentSeconds) else "Custom",
                                isSelected = isCustomValue,
                                onClick = { 
                                    selectedTime = currentSeconds
                                    showCustomPicker = true 
                                },
                                modifier = Modifier.weight(1f),
                                isCustom = true
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Cancel button
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCustom: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isCustom -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isCustom -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun CustomTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit
) {
    // Generate time options from 0:15 to 10:00 in 15-second increments
    val timeOptions = remember {
        (15..600 step 15).toList()
    }
    
    val listState = rememberLazyListState()
    
    // Find initial index
    val initialIndex = remember(currentSeconds) {
        timeOptions.indexOfFirst { it >= currentSeconds }.coerceAtLeast(0)
    }
    
    LaunchedEffect(Unit) {
        // Scroll to center the current selection
        listState.scrollToItem((initialIndex - 2).coerceAtLeast(0))
    }
    
    Box(
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(timeOptions) { seconds ->
                val isSelected = seconds == currentSeconds
                
                Surface(
                    onClick = { onTimeSelected(seconds) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = formatRestTime(seconds),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
