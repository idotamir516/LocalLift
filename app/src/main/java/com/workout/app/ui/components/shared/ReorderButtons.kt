package com.workout.app.ui.components.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.workout.app.R

/**
 * Reusable reorder buttons for moving items up/down in a list.
 * Used in ExerciseCard and TemplateEditorScreen for reordering exercises.
 * 
 * @param isFirst Whether this is the first item (disables move up)
 * @param isLast Whether this is the last item (disables move down)
 * @param onMoveUp Callback when move up is clicked
 * @param onMoveDown Callback when move down is clicked
 * @param modifier Modifier for the row containing the buttons
 * @param itemName Optional name of the item being reordered for better accessibility
 */
@Composable
fun ReorderButtons(
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
    itemName: String = "item"
) {
    val moveUpDesc = stringResource(R.string.a11y_move_up, itemName)
    val moveDownDesc = stringResource(R.string.a11y_move_down, itemName)
    val cannotMoveUpDesc = stringResource(R.string.a11y_cannot_move_up)
    val cannotMoveDownDesc = stringResource(R.string.a11y_cannot_move_down)
    
    Row(modifier = modifier) {
        IconButton(
            onClick = onMoveUp,
            enabled = !isFirst,
            modifier = Modifier.size(SharedConstants.SmallIconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = if (!isFirst) moveUpDesc else cannotMoveUpDesc,
                modifier = Modifier.size(SharedConstants.StandardIconSize),
                tint = if (!isFirst) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = SharedConstants.DisabledAlpha)
                }
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = !isLast,
            modifier = Modifier.size(SharedConstants.SmallIconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (!isLast) moveDownDesc else cannotMoveDownDesc,
                modifier = Modifier.size(SharedConstants.StandardIconSize),
                tint = if (!isLast) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = SharedConstants.DisabledAlpha)
                }
            )
        }
    }
}
