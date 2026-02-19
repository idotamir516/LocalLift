package com.workout.app.ui.components.shared

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shared UI constants for consistent styling across components.
 */
object SharedConstants {
    // Corner radii
    val SmallCornerRadius = 8.dp
    val MediumCornerRadius = 12.dp
    val LargeCornerRadius = 20.dp
    val DialogCornerRadius = 24.dp
    
    // Shapes
    val SmallRoundedShape = RoundedCornerShape(SmallCornerRadius)
    val MediumRoundedShape = RoundedCornerShape(MediumCornerRadius)
    val LargeRoundedShape = RoundedCornerShape(LargeCornerRadius)
    val DialogShape = RoundedCornerShape(DialogCornerRadius)
    
    // Icon button sizes
    val SmallIconButtonSize = 32.dp
    val MediumIconButtonSize = 36.dp
    
    // Icon sizes
    val TinyIconSize = 14.dp
    val SmallIconSize = 16.dp
    val MediumIconSize = 18.dp
    val StandardIconSize = 20.dp
    
    // Alpha values
    const val DisabledAlpha = 0.2f
    const val PlaceholderAlpha = 0.3f
    const val SubtleAlpha = 0.5f
    const val BackgroundAlpha = 0.3f
    
    // Border
    val BorderWidth = 1.dp
    
    // Spacing
    val TinySpacing = 4.dp
    val SmallSpacing = 8.dp
    val MediumSpacing = 12.dp
    val StandardSpacing = 16.dp
    val LargeSpacing = 20.dp
    val XLargeSpacing = 24.dp
}

/**
 * Rest time picker constants.
 */
object RestTimeConstants {
    /** Common rest time presets in seconds */
    val Presets = listOf(30, 60, 90, 120, 180)
    
    /** Minimum rest time in seconds */
    const val MinRestTimeSeconds = 15
    
    /** Maximum rest time in seconds */
    const val MaxRestTimeSeconds = 600
    
    /** Increment step for custom time picker */
    const val TimeIncrementSeconds = 15
    
    /** Height of the custom time picker scroll list */
    val PickerHeight = 180.dp
}
