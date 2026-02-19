package com.workout.app.ui.components.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.R

/**
 * A reusable rest time picker dialog with quick presets and custom option.
 * Used in SetRow and TemplateEditorScreen for selecting rest times between sets.
 *
 * @param currentSeconds The currently selected rest time in seconds
 * @param onTimeSelected Callback when a time is selected
 * @param onDismiss Callback when the dialog is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomPicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(currentSeconds) }
    
    // Check if current value is a preset or custom
    val isCustomValue = currentSeconds !in RestTimeConstants.Presets
    
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = SharedConstants.DialogShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(SharedConstants.XLargeSpacing)
            ) {
                Text(
                    text = stringResource(R.string.rest_time_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(SharedConstants.LargeSpacing))
                
                if (showCustomPicker) {
                    // Custom time picker with scrollable list
                    CustomTimePicker(
                        currentSeconds = selectedTime,
                        onTimeSelected = { time ->
                            selectedTime = time
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(SharedConstants.StandardSpacing))
                    
                    val backDescription = stringResource(R.string.a11y_go_back)
                    val setTimeDescription = stringResource(R.string.a11y_set_rest_time, formatRestTime(selectedTime))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SharedConstants.MediumSpacing)
                    ) {
                        // Back button
                        Surface(
                            onClick = { showCustomPicker = false },
                            shape = SharedConstants.LargeRoundedShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SharedConstants.SubtleAlpha),
                            modifier = Modifier.semantics { 
                                contentDescription = backDescription
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_back),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = SharedConstants.XLargeSpacing, vertical = SharedConstants.MediumSpacing)
                            )
                        }
                        
                        // Confirm button
                        Surface(
                            onClick = { onTimeSelected(selectedTime) },
                            shape = SharedConstants.LargeRoundedShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { 
                                contentDescription = setTimeDescription
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.rest_time_set, formatRestTime(selectedTime)),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = SharedConstants.XLargeSpacing, vertical = SharedConstants.MediumSpacing)
                            )
                        }
                    }
                } else {
                    // Quick preset chips
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SharedConstants.SmallSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: 30s, 1:00, 1:30
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SharedConstants.SmallSpacing),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RestTimeConstants.Presets.take(3).forEach { seconds ->
                                val chipDescription = stringResource(R.string.a11y_set_rest_time, formatRestTime(seconds))
                                PresetChip(
                                    text = formatRestTime(seconds),
                                    isSelected = currentSeconds == seconds,
                                    onClick = { onTimeSelected(seconds) },
                                    modifier = Modifier.weight(1f),
                                    contentDescription = chipDescription
                                )
                            }
                        }
                        
                        // Row 2: 2:00, 3:00, Custom
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SharedConstants.SmallSpacing),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RestTimeConstants.Presets.drop(3).forEach { seconds ->
                                val chipDescription = stringResource(R.string.a11y_set_rest_time, formatRestTime(seconds))
                                PresetChip(
                                    text = formatRestTime(seconds),
                                    isSelected = currentSeconds == seconds,
                                    onClick = { onTimeSelected(seconds) },
                                    modifier = Modifier.weight(1f),
                                    contentDescription = chipDescription
                                )
                            }
                            
                            // Custom button
                            val customDescription = stringResource(R.string.a11y_open_custom_picker)
                            PresetChip(
                                text = if (isCustomValue) formatRestTime(currentSeconds) else stringResource(R.string.action_custom),
                                isSelected = isCustomValue,
                                onClick = { 
                                    selectedTime = currentSeconds
                                    showCustomPicker = true 
                                },
                                modifier = Modifier.weight(1f),
                                isCustom = true,
                                contentDescription = customDescription
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(SharedConstants.StandardSpacing))
                    
                    // Cancel button
                    val cancelDescription = stringResource(R.string.a11y_cancel_close)
                    Surface(
                        onClick = onDismiss,
                        shape = SharedConstants.LargeRoundedShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SharedConstants.SubtleAlpha),
                        modifier = Modifier.semantics { 
                            contentDescription = cancelDescription
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = SharedConstants.MediumSpacing)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A chip button for selecting a preset rest time.
 * 
 * @param text The text to display on the chip
 * @param isSelected Whether this chip is currently selected
 * @param onClick Callback when the chip is clicked
 * @param modifier Modifier for the chip
 * @param isCustom Whether this is the custom option chip
 * @param contentDescription Accessibility description for screen readers
 */
@Composable
private fun PresetChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCustom: Boolean = false,
    contentDescription: String = text
) {
    Surface(
        onClick = onClick,
        shape = SharedConstants.MediumRoundedShape,
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isCustom -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = modifier.semantics { 
            this.contentDescription = contentDescription 
        }
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
                .padding(vertical = SharedConstants.MediumSpacing)
        )
    }
}

/**
 * A scrollable time picker for selecting custom rest times.
 * Shows time options from 15 seconds to 10 minutes in 15-second increments.
 * 
 * @param currentSeconds The currently selected time in seconds
 * @param onTimeSelected Callback when a time is selected
 */
@Composable
private fun CustomTimePicker(
    currentSeconds: Int,
    onTimeSelected: (Int) -> Unit
) {
    // Generate time options using constants
    val timeOptions = remember {
        (RestTimeConstants.MinRestTimeSeconds..RestTimeConstants.MaxRestTimeSeconds 
            step RestTimeConstants.TimeIncrementSeconds).toList()
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
            .height(RestTimeConstants.PickerHeight)
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(timeOptions) { seconds ->
                val isSelected = seconds == currentSeconds
                val timeText = formatRestTime(seconds)
                
                Surface(
                    onClick = { onTimeSelected(seconds) },
                    shape = SharedConstants.MediumRoundedShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = SharedConstants.StandardSpacing)
                        .semantics { contentDescription = "Select $timeText rest time" }
                ) {
                    Text(
                        text = timeText,
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
                            .padding(vertical = SharedConstants.SmallSpacing)
                    )
                }
            }
        }
    }
}

/**
 * Format seconds as mm:ss (e.g., 90 -> "1:30")
 */
fun formatRestTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
