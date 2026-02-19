package com.workout.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.workout.app.R
import com.workout.app.data.entities.SetType
import com.workout.app.ui.theme.DropSetColor
import com.workout.app.ui.theme.WarmupColor

/**
 * A clickable indicator showing the set type (Regular number, W for Warmup, D for Drop Set).
 * Clicking cycles through: Regular → Warmup → Drop Set → Regular
 *
 * @param setType Current set type
 * @param displayNumber The number to display for regular sets (typically the set's position)
 * @param enabled Whether the indicator is clickable
 * @param onSetTypeChange Callback when user cycles to the next set type
 * @param modifier Modifier for the component
 */
@Composable
fun SetTypeIndicator(
    setType: SetType,
    displayNumber: Int,
    enabled: Boolean = true,
    onSetTypeChange: (SetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeText, typeColor) = getSetTypeDisplay(setType, displayNumber)
    val nextType = cycleSetType(setType)
    val setTypeName = getSetTypeFullName(setType)
    val nextTypeName = getSetTypeFullName(nextType)
    val tapToChangeText = stringResource(R.string.a11y_tap_to_change, nextTypeName)
    val accessibilityDescription = buildString {
        append(setTypeName)
        if (setType == SetType.REGULAR) append(" $displayNumber")
        if (enabled) append(". $tapToChangeText")
    }
    
    Box(
        modifier = modifier
            .size(SharedConstants.SmallIconButtonSize)
            .clip(SharedConstants.SmallRoundedShape)
            .background(typeColor.copy(alpha = 0.15f))
            .border(
                width = SharedConstants.BorderWidth,
                color = typeColor.copy(alpha = 0.4f),
                shape = SharedConstants.SmallRoundedShape
            )
            .clickable(enabled = enabled) {
                onSetTypeChange(nextType)
            }
            .semantics { contentDescription = accessibilityDescription },
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
}

/**
 * Get the full name of a set type for accessibility purposes.
 */
@Composable
private fun getSetTypeFullName(setType: SetType): String = when (setType) {
    SetType.REGULAR -> stringResource(R.string.set_type_regular)
    SetType.WARMUP -> stringResource(R.string.set_type_warmup)
    SetType.DROP_SET -> stringResource(R.string.set_type_drop_set)
}

/**
 * Get the display text and color for a set type.
 *
 * @param setType The set type
 * @param displayNumber The number to show for regular sets
 * @return Pair of (displayText, color)
 */
@Composable
fun getSetTypeDisplay(setType: SetType, displayNumber: Int): Pair<String, Color> {
    return when (setType) {
        SetType.REGULAR -> "$displayNumber" to MaterialTheme.colorScheme.primary
        SetType.WARMUP -> stringResource(R.string.set_type_warmup_short) to WarmupColor
        SetType.DROP_SET -> stringResource(R.string.set_type_drop_short) to DropSetColor
    }
}

/**
 * Cycle to the next set type in order: Regular → Warmup → Drop Set → Regular
 *
 * @param currentType The current set type
 * @return The next set type in the cycle
 */
fun cycleSetType(currentType: SetType): SetType {
    return when (currentType) {
        SetType.REGULAR -> SetType.WARMUP
        SetType.WARMUP -> SetType.DROP_SET
        SetType.DROP_SET -> SetType.REGULAR
    }
}
