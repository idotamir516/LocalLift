package com.workout.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.InputFieldBackground
import com.workout.app.ui.theme.InputFieldBorder

/**
 * A reusable numeric input field with consistent styling.
 * Used for weight, reps, and other numeric inputs throughout the app.
 *
 * @param value Current value as string (empty string for no value)
 * @param onValueChange Callback when value changes, receives the new string value
 * @param enabled Whether the field is editable
 * @param placeholder Optional placeholder text shown when value is empty (e.g., previous value)
 * @param height Height of the field (default 48.dp for active workout, use 44.dp for templates)
 * @param accessibilityLabel Description of what this field is for (e.g., "Weight in pounds", "Number of reps")
 * @param maxDigits Maximum number of digits allowed (default 4, e.g., for weights up to 9999)
 * @param allowDecimal Whether to allow decimal input (default false for integers)
 * @param modifier Modifier for the component
 */
@Composable
fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String? = null,
    height: Dp = 48.dp,
    accessibilityLabel: String = "Numeric input",
    maxDigits: Int = 4,
    allowDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    val accessibilityDescription = buildString {
        append(accessibilityLabel)
        if (value.isNotEmpty()) {
            append(", current value $value")
        } else if (placeholder != null) {
            append(", previous value $placeholder")
        }
        if (!enabled) append(", disabled")
    }
    
    Box(
        modifier = modifier
            .height(height)
            .clip(SharedConstants.SmallRoundedShape)
            .background(InputFieldBackground)
            .border(
                width = SharedConstants.BorderWidth,
                color = InputFieldBorder,
                shape = SharedConstants.SmallRoundedShape
            )
            .semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                // Filter input based on whether decimals are allowed
                val filtered = if (allowDecimal) {
                    InputValidation.filterDecimalInput(newValue)
                } else {
                    InputValidation.filterIntegerInput(newValue, maxDigits)
                }
                // Only update if filtering changed something or input is valid
                if (filtered != value) {
                    onValueChange(filtered)
                }
            },
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SharedConstants.SmallSpacing),
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
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SharedConstants.SubtleAlpha)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SharedConstants.PlaceholderAlpha),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
