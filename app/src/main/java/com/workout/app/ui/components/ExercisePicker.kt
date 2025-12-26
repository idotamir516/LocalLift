package com.workout.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workout.app.data.AppDatabase
import com.workout.app.data.Exercise
import com.workout.app.data.ExerciseLibrary
import com.workout.app.data.entities.CustomExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Calculates a fuzzy match score for a search query against a target string.
 * Returns a score where lower is better, or null if no match.
 * 
 * Matching strategies (in order of priority):
 * 1. Exact match (score: 0)
 * 2. Starts with query (score: 1)
 * 3. Contains query as substring (score: 2)
 * 4. Word initials match (e.g., "bp" matches "Bench Press") (score: 3)
 * 5. All query chars appear in order (e.g., "bnchprs" matches "Bench Press") (score: 4 + gap penalty)
 */
private fun fuzzyMatchScore(query: String, target: String): Int? {
    val queryLower = query.lowercase()
    val targetLower = target.lowercase()
    
    // Exact match
    if (queryLower == targetLower) return 0
    
    // Starts with
    if (targetLower.startsWith(queryLower)) return 1
    
    // Contains as substring
    if (targetLower.contains(queryLower)) return 2
    
    // Word initials match (e.g., "bp" matches "Bench Press")
    val words = targetLower.split(" ", "-", "_")
    val initials = words.mapNotNull { it.firstOrNull() }.joinToString("")
    if (initials.startsWith(queryLower)) return 3
    
    // Check if initials contain the query
    if (queryLower.length <= initials.length && initials.contains(queryLower)) return 3
    
    // Fuzzy match: all query chars appear in order
    var queryIndex = 0
    var gapPenalty = 0
    var lastMatchIndex = -1
    
    for ((i, char) in targetLower.withIndex()) {
        if (queryIndex < queryLower.length && char == queryLower[queryIndex]) {
            // Add gap penalty for non-consecutive matches
            if (lastMatchIndex >= 0 && i > lastMatchIndex + 1) {
                gapPenalty += min(i - lastMatchIndex - 1, 3) // Cap gap penalty
            }
            lastMatchIndex = i
            queryIndex++
        }
    }
    
    // All query characters found in order
    if (queryIndex == queryLower.length) {
        return 4 + gapPenalty
    }
    
    // No match
    return null
}

/**
 * A bottom sheet component for selecting exercises from the library.
 * Supports search and filtering by category.
 * 
 * @param isVisible Whether the picker is visible
 * @param onDismiss Callback when the picker is dismissed
 * @param onExerciseSelected Callback when an exercise is selected
 * @param selectedExercises List of already selected exercise names (to show checkmarks)
 * @param customExercises Flow of custom exercises from database
 * @param onCreateCustomExercise Callback to create a new custom exercise
 * @param onDeleteCustomExercise Callback to delete a custom exercise
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePicker(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit,
    selectedExercises: List<String> = emptyList(),
    customExercises: Flow<List<CustomExercise>> = flowOf(emptyList()),
    onCreateCustomExercise: (CustomExercise) -> Unit = {},
    onDeleteCustomExercise: (CustomExercise) -> Unit = {}
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var showCreateDialog by remember { mutableStateOf(false) }
        
        val customExerciseList by customExercises.collectAsState(initial = emptyList())
        
        val categories = ExerciseLibrary.categories
        
        // Combine library exercises with custom exercises
        val allExercises = remember(searchQuery, selectedCategory, customExerciseList) {
            // Convert custom exercises to Exercise objects
            val customAsExercises = customExerciseList.map { custom ->
                Exercise(
                    name = custom.name,
                    primaryMuscle = custom.primaryMuscle,
                    auxiliaryMuscles = custom.getAuxiliaryMusclesList(),
                    isBodyweight = custom.isBodyweight
                )
            }
            
            // Combine with library exercises
            val combined = ExerciseLibrary.exercises + customAsExercises
            
            var result = if (selectedCategory != null) {
                combined.filter { it.primaryMuscle == selectedCategory }
            } else {
                combined
            }
            
            if (searchQuery.isNotBlank()) {
                // Use fuzzy search and sort by match quality
                result = result
                    .mapNotNull { exercise ->
                        fuzzyMatchScore(searchQuery, exercise.name)?.let { score ->
                            exercise to score
                        }
                    }
                    .sortedWith(compareBy({ it.second }, { it.first.name }))
                    .map { it.first }
            } else {
                result = result.sortedBy { it.name }
            }
            
            result
        }
        
        // Track which exercises are custom (for showing delete option)
        val customExerciseNames = remember(customExerciseList) {
            customExerciseList.map { it.name }.toSet()
        }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier.heightIn(max = 600.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search exercises...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category filter chips - horizontally scrollable
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                
                // Exercise list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Create Custom Exercise button at top
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { showCreateDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Create Custom Exercise",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    if (allExercises.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No exercises found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(allExercises) { exercise ->
                            val isAlreadyAdded = selectedExercises.contains(exercise.name)
                            val isCustom = customExerciseNames.contains(exercise.name)
                            
                            ExerciseListItem(
                                exercise = exercise,
                                isAlreadyAdded = isAlreadyAdded,
                                isCustom = isCustom,
                                onClick = { onExerciseSelected(exercise) },
                                onDelete = if (isCustom) {
                                    {
                                        customExerciseList.find { it.name == exercise.name }?.let {
                                            onDeleteCustomExercise(it)
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Create Custom Exercise Dialog
        if (showCreateDialog) {
            CreateCustomExerciseDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { customExercise ->
                    onCreateCustomExercise(customExercise)
                    showCreateDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    isAlreadyAdded: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Custom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Row {
                    Text(
                        text = exercise.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (exercise.isBodyweight) {
                        Text(
                            text = " • Bodyweight",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Delete button for custom exercises
            if (isCustom && onDelete != null) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Show "Added" text for exercises already in the workout
            if (isAlreadyAdded) {
                Text(
                    text = "Added",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm && onDelete != null) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteConfirm = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Delete Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Are you sure you want to delete \"${exercise.name}\"? This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onDelete()
                                showDeleteConfirm = false
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for creating a new custom exercise.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateCustomExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (CustomExercise) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("Chest") }
    var selectedAuxiliaryMuscles by remember { mutableStateOf(setOf<String>()) }
    var isBodyweight by remember { mutableStateOf(false) }
    var muscleDropdownExpanded by remember { mutableStateOf(false) }
    var showAuxiliaryPicker by remember { mutableStateOf(false) }
    
    val muscles = ExerciseLibrary.categories
    val isValid = name.isNotBlank()
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Create Custom Exercise",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Exercise name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g., Cable Hammer Curls") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Primary muscle dropdown
                ExposedDropdownMenuBox(
                    expanded = muscleDropdownExpanded,
                    onExpandedChange = { muscleDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMuscle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Muscle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = muscleDropdownExpanded,
                        onDismissRequest = { muscleDropdownExpanded = false }
                    ) {
                        muscles.forEach { muscle ->
                            DropdownMenuItem(
                                text = { Text(muscle) },
                                onClick = {
                                    selectedMuscle = muscle
                                    // Remove from auxiliary if selected as primary
                                    selectedAuxiliaryMuscles = selectedAuxiliaryMuscles - muscle
                                    muscleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Auxiliary muscles section
                Text(
                    text = "Auxiliary Muscles (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selected auxiliary muscles display / add button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { showAuxiliaryPicker = true }
                ) {
                    if (selectedAuxiliaryMuscles.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add auxiliary muscles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Display selected muscles as chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedAuxiliaryMuscles.forEach { muscle ->
                                    AuxiliaryMuscleChip(
                                        muscle = muscle,
                                        onRemove = {
                                            selectedAuxiliaryMuscles = selectedAuxiliaryMuscles - muscle
                                        }
                                    )
                                }
                                // Add more button
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    onClick = { showAuxiliaryPicker = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add more",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bodyweight toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBodyweight = !isBodyweight }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bodyweight Exercise",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    androidx.compose.material3.Switch(
                        checked = isBodyweight,
                        onCheckedChange = { isBodyweight = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (isValid) {
                                onCreate(
                                    CustomExercise(
                                        name = name.trim(),
                                        primaryMuscle = selectedMuscle,
                                        auxiliaryMuscles = selectedAuxiliaryMuscles.joinToString(","),
                                        isBodyweight = isBodyweight
                                    )
                                )
                            }
                        },
                        enabled = isValid
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
    
    // Auxiliary muscle picker dialog
    if (showAuxiliaryPicker) {
        AuxiliaryMusclePickerDialog(
            availableMuscles = muscles.filter { it != selectedMuscle },
            selectedMuscles = selectedAuxiliaryMuscles,
            onDismiss = { showAuxiliaryPicker = false },
            onConfirm = { selected ->
                selectedAuxiliaryMuscles = selected
                showAuxiliaryPicker = false
            }
        )
    }
}

@Composable
private fun AuxiliaryMuscleChip(
    muscle: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = muscle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove $muscle",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuxiliaryMusclePickerDialog(
    availableMuscles: List<String>,
    selectedMuscles: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var localSelection by remember { mutableStateOf(selectedMuscles) }
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Auxiliary Muscles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Muscles that assist in the movement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scrollable list of muscle checkboxes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableMuscles) { muscle ->
                        val isSelected = localSelection.contains(muscle)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                Color.Transparent
                            },
                            onClick = {
                                localSelection = if (isSelected) {
                                    localSelection - muscle
                                } else {
                                    localSelection + muscle
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        localSelection = if (checked) {
                                            localSelection + muscle
                                        } else {
                                            localSelection - muscle
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = muscle,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(localSelection) }) {
                        Text("Done (${localSelection.size})")
                    }
                }
            }
        }
    }
}
