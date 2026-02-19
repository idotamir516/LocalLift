package com.workout.app.ui.components.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A reusable confirmation dialog for destructive or important actions.
 *
 * @param title The title of the dialog
 * @param message The message/description to display
 * @param confirmLabel The text for the confirm button (default: "Confirm")
 * @param dismissLabel The text for the dismiss button (default: "Cancel")
 * @param isDestructive Whether this is a destructive action (shows confirm in red)
 * @param onConfirm Called when the user confirms the action
 * @param onDismiss Called when the user dismisses the dialog
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        }
    )
}
