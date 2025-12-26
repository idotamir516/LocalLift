package com.workout.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.PhaseType
import com.workout.app.data.entities.TrainingPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts a UTC timestamp from DatePicker to local noon time.
 * DatePicker returns UTC midnight, which can appear as the previous day in local timezone.
 * We convert to local noon to avoid any timezone-related day shifts.
 */
private fun utcToLocalDate(utcMillis: Long): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 12) // Noon to avoid day boundary issues
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return localCalendar.timeInMillis
}

/**
 * Converts a local timestamp to UTC for DatePicker initial value.
 */
private fun localToUtcDate(localMillis: Long): Long {
    val localCalendar = Calendar.getInstance().apply {
        timeInMillis = localMillis
    }
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return utcCalendar.timeInMillis
}

/**
 * Screen for managing training phases.
 * A phase is considered "active" if today's date falls within its date range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPhasesScreen(
    phases: Flow<List<TrainingPhase>> = flowOf(emptyList()),
    activePhase: Flow<TrainingPhase?> = flowOf(null),
    onCreatePhase: (name: String, type: PhaseType, startDate: Long, endDate: Long?, notes: String?) -> Unit = { _, _, _, _, _ -> },
    onUpdatePhase: (TrainingPhase) -> Unit = {},
    onDeletePhase: (Long) -> Unit = {},
    onCheckOverlap: suspend (startDate: Long, endDate: Long?, excludePhaseId: Long) -> TrainingPhase? = { _, _, _ -> null },
    onNavigateBack: () -> Unit = {}
) {
    val phaseList by phases.collectAsState(initial = emptyList())
    val currentPhase by activePhase.collectAsState(initial = null)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var phaseToEdit by remember { mutableStateOf<TrainingPhase?>(null) }
    var phaseToDelete by remember { mutableStateOf<TrainingPhase?>(null) }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Training Phases",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (phaseList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Start New Phase"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (phaseList.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Training Phases",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Start a new phase to track your\nbulk, cut, or training block",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Phase",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active phase section (based on today's date)
                currentPhase?.let { active ->
                    item {
                        Text(
                            text = "Active Phase",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    item {
                        PhaseCard(
                            phase = active,
                            isActive = true,
                            onEdit = { phaseToEdit = active },
                            onDelete = { phaseToDelete = active }
                        )
                    }
                }
                
                // Other phases section (not currently active)
                val otherPhases = phaseList.filter { it.id != currentPhase?.id }
                if (otherPhases.isNotEmpty()) {
                    item {
                        Text(
                            text = if (currentPhase != null) "Other Phases" else "All Phases",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = if (currentPhase != null) 16.dp else 0.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(otherPhases, key = { it.id }) { phase ->
                        PhaseCard(
                            phase = phase,
                            isActive = false,
                            onEdit = { phaseToEdit = phase },
                            onDelete = { phaseToDelete = phase }
                        )
                    }
                }
            }
        }
    }
    
    // Create phase dialog
    if (showCreateDialog) {
        PhaseDialog(
            phase = null,
            onDismiss = { showCreateDialog = false },
            onCheckOverlap = onCheckOverlap,
            onSave = { name, type, startDate, endDate, notes ->
                onCreatePhase(name, type, startDate, endDate, notes)
                showCreateDialog = false
            }
        )
    }
    
    // Edit phase dialog
    phaseToEdit?.let { phase ->
        PhaseDialog(
            phase = phase,
            onDismiss = { phaseToEdit = null },
            onCheckOverlap = onCheckOverlap,
            onSave = { name, type, startDate, endDate, notes ->
                onUpdatePhase(phase.copy(
                    name = name,
                    type = type,
                    startDate = startDate,
                    endDate = endDate,
                    notes = notes
                ))
                phaseToEdit = null
            }
        )
    }
    
    // Delete confirmation dialog
    phaseToDelete?.let { phase ->
        AlertDialog(
            onDismissRequest = { phaseToDelete = null },
            title = { Text("Delete Phase?") },
            text = { 
                Text("Are you sure you want to delete \"${phase.name}\"? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePhase(phase.id)
                        phaseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { phaseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PhaseCard(
    phase: TrainingPhase,
    isActive: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getPhaseColor(phase.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = getPhaseColor(phase.type),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = phase.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = phase.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = getPhaseColor(phase.type)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val startDateStr = dateFormat.format(Date(phase.startDate))
                val today = System.currentTimeMillis()
                val dateText = if (phase.endDate != null) {
                    val endDateStr = dateFormat.format(Date(phase.endDate))
                    "$startDateStr - $endDateStr"
                } else if (phase.startDate > today) {
                    // Future phase without end date
                    "Starts $startDateStr"
                } else {
                    // Current/past phase without end date
                    "$startDateStr - present"
                }
                
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                phase.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 2
                        )
                    }
                }
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhaseDialog(
    phase: TrainingPhase?,
    onDismiss: () -> Unit,
    onCheckOverlap: suspend (startDate: Long, endDate: Long?, excludePhaseId: Long) -> TrainingPhase?,
    onSave: (name: String, type: PhaseType, startDate: Long, endDate: Long?, notes: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf(phase?.name ?: "") }
    var selectedType by remember { mutableStateOf(phase?.type ?: PhaseType.BULK) }
    var startDate by remember { mutableStateOf(phase?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(phase?.endDate) }
    var notes by remember { mutableStateOf(phase?.notes ?: "") }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    var overlapError by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    
    // Clear overlap error whenever dates change
    LaunchedEffect(startDate, endDate) {
        overlapError = null
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (phase == null) "New Phase" else "Edit Phase",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show overlap error if any
                overlapError?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // Show date range error
                val endDateForValidation = endDate
                if (endDateForValidation != null && startDate >= endDateForValidation) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Start date must be before the end date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Phase Name") },
                    placeholder = { Text("e.g., Winter Bulk 2025") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Phase type selector
                Column {
                    Text(
                        text = "Phase Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhaseTypeSelector(
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it }
                    )
                }
                
                // Start date
                Surface(
                    onClick = { showStartDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(startDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // End date (optional - leave empty for open-ended phase)
                Surface(
                    onClick = { showEndDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End Date (Optional)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = endDate?.let { dateFormat.format(Date(it)) } ?: "Not set",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (endDate != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        if (endDate != null) {
                            IconButton(
                                onClick = { endDate = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "×",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Goals, targets, etc.") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            // Validate that start date is before end date
            val endDateVal = endDate
            val isDateRangeValid = endDateVal == null || startDate < endDateVal
            
            TextButton(
                onClick = { 
                    scope.launch {
                        isChecking = true
                        overlapError = null
                        
                        // Check for overlap with existing phases
                        val excludeId = phase?.id ?: 0L
                        
                        // For open-ended phases, use far future date for overlap check
                        val effectiveEndDate = endDate ?: Long.MAX_VALUE
                        
                        val overlapping = onCheckOverlap(startDate, effectiveEndDate, excludeId)
                        
                        if (overlapping != null) {
                            val overlapDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            val overlapStart = overlapDateFormat.format(Date(overlapping.startDate))
                            val overlapEnd = overlapping.endDate?.let { overlapDateFormat.format(Date(it)) } ?: "no end date"
                            overlapError = "Overlaps with \"${overlapping.name}\" ($overlapStart - $overlapEnd)"
                            isChecking = false
                        } else {
                            isChecking = false
                            onSave(
                                name.trim(), 
                                selectedType, 
                                startDate, 
                                endDate,
                                notes.trim().ifBlank { null }
                            )
                        }
                    }
                },
                enabled = name.isNotBlank() && !isChecking && isDateRangeValid
            ) {
                Text(if (isChecking) "Checking..." else if (phase == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Start date picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = localToUtcDate(startDate))
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { startDate = utcToLocalDate(it) }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End date picker
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localToUtcDate(endDate ?: (startDate + 90L * 24 * 60 * 60 * 1000))
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { endDate = utcToLocalDate(it) }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PhaseTypeSelector(
    selectedType: PhaseType,
    onTypeSelected: (PhaseType) -> Unit
) {
    val types = PhaseType.entries.toTypedArray()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.take(4).forEach { type ->
                PhaseTypeChip(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.drop(4).forEach { type ->
                PhaseTypeChip(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
            // Add spacer for alignment if odd number
            if (types.size % 4 != 0) {
                repeat(4 - (types.size % 4)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PhaseTypeChip(
    type: PhaseType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getPhaseColor(type)
    
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, color)
        } else null
    ) {
        Text(
            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun getPhaseColor(type: PhaseType): Color {
    return when (type) {
        PhaseType.BULK -> Color(0xFF4CAF50) // Green
        PhaseType.CUT -> Color(0xFFE91E63) // Pink
        PhaseType.MAINTENANCE -> Color(0xFF2196F3) // Blue
        PhaseType.STRENGTH -> Color(0xFFFF5722) // Deep Orange
        PhaseType.HYPERTROPHY -> Color(0xFF9C27B0) // Purple
        PhaseType.DELOAD -> Color(0xFF607D8B) // Blue Grey
        PhaseType.CUSTOM -> MaterialTheme.colorScheme.primary
    }
}
