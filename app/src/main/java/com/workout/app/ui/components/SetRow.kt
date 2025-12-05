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
import com.workout.app.ui.theme.NeonCyan
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
    restSeconds: Int? = null,
    setType: SetType = SetType.REGULAR,
    showRemoveButton: Boolean = false,
    showRpe: Boolean = false,
    rpe: Float? = null,
    onWeightChange: (Int?) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestChange: (Int?) -> Unit = {},
    onSetTypeChange: (SetType) -> Unit = {},
    onRpeChange: (Float?) -> Unit = {},
    onCompleteClick: () -> Unit,
    onRemoveClick: () -> Unit = {},
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
            
            // Weight input - with dark background
            InputField(
                value = weight?.toString() ?: "",
                onValueChange = { onWeightChange(it.toIntOrNull()) },
                enabled = !isCompleted,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reps input - with dark background  
            InputField(
                value = reps?.toString() ?: "",
                onValueChange = { onRepsChange(it.toIntOrNull()) },
                enabled = !isCompleted,
                modifier = Modifier.weight(1f)
            )
            
            // RPE input - only shown if enabled for this exercise
            if (showRpe) {
                Spacer(modifier = Modifier.width(8.dp))
                
                RpeInputField(
                    value = rpe,
                    onValueChange = onRpeChange,
                    enabled = !isCompleted,
                    modifier = Modifier.weight(0.8f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Checkbox for completion - with border when unchecked
            // Clickable to toggle completion (can uncomplete by clicking again)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isCompleted) NeonCyan else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) NeonCyan 
                               else if (weight != null && reps != null) NeonCyan.copy(alpha = 0.5f)
                               else InputFieldBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = (weight != null && reps != null) || isCompleted) {
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
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRestTime(restSeconds ?: 120),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
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

/**
 * Custom input field for RPE (Rate of Perceived Exertion) values 1-10
 */
@Composable
private fun RpeInputField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val displayValue = value?.let { 
        if (it == it.toInt().toFloat()) it.toInt().toString() 
        else String.format("%.1f", it)
    } ?: ""
    
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
            value = displayValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    onValueChange(null)
                } else {
                    // Allow numbers and one decimal point
                    val parsed = newValue.toFloatOrNull()
                    if (parsed != null && parsed >= 1f && parsed <= 10f) {
                        onValueChange(parsed)
                    } else if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        // Allow typing even if not yet valid (e.g., "1.")
                        val partialParsed = newValue.toFloatOrNull()
                        if (partialParsed == null || partialParsed <= 10f) {
                            onValueChange(partialParsed)
                        }
                    }
                }
            },
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (displayValue.isEmpty()) {
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

/**
 * A scrollable rest time picker dialog (like the reference app)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Generate time options from 0:15 to 5:00 in 5-second increments
    val timeOptions = remember {
        (15..300 step 5).toList()
    }
    
    val listState = rememberLazyListState()
    var selectedTime by remember { mutableStateOf(currentSeconds) }
    
    // Find initial index
    val initialIndex = remember(currentSeconds) {
        timeOptions.indexOfFirst { it >= currentSeconds }.coerceAtLeast(0)
    }
    
    LaunchedEffect(Unit) {
        // Scroll to center the current selection
        listState.scrollToItem((initialIndex - 3).coerceAtLeast(0))
    }
    
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
                    text = "Inline Rest Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Scrollable time list
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(timeOptions) { seconds ->
                            val isSelected = seconds == selectedTime
                            
                            Surface(
                                onClick = { selectedTime = seconds },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.surfaceVariant
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
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // OK Button
                Surface(
                    onClick = { onTimeSelected(selectedTime) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
