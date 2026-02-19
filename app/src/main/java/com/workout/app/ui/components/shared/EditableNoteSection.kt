package com.workout.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.workout.app.R

/**
 * Reusable note section that can display a note, edit it, or show an "Add note" button.
 * Used in ExerciseCard and TemplateEditorScreen for exercise notes.
 * 
 * @param note The current note text (null if no note exists)
 * @param onNoteChange Callback when note is saved (null to clear the note)
 * @param modifier Modifier for the section
 */
@Composable
fun EditableNoteSection(
    note: String?,
    onNoteChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteEditor by remember { mutableStateOf(false) }
    var editedNote by remember(note) { mutableStateOf(note ?: "") }
    
    Column(modifier = modifier) {
        if (note != null || showNoteEditor) {
            Spacer(modifier = Modifier.height(8.dp))
            
            if (showNoteEditor) {
                // Editable note field
                NoteEditorField(
                    value = editedNote,
                    onValueChange = { editedNote = it },
                    onSave = {
                        val trimmed = editedNote.trim().takeIf { it.isNotEmpty() }
                        onNoteChange(trimmed)
                        showNoteEditor = false
                    }
                )
            } else {
                // Display note with edit option
                NoteDisplay(
                    note = note ?: "",
                    onClick = { showNoteEditor = true }
                )
            }
        } else {
            // Add note button
            Spacer(modifier = Modifier.height(4.dp))
            AddNoteButton(onClick = { showNoteEditor = true })
        }
    }
}

@Composable
private fun NoteEditorField(
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.add_note_placeholder), style = MaterialTheme.typography.bodySmall) },
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
            onClick = onSave,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.save_note),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NoteDisplay(
    note: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
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
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(R.string.edit_note),
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AddNoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
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
            text = stringResource(R.string.add_note_button),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
