package com.workout.app.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.WeightEntry
import com.workout.app.data.entities.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Weight tracking screen - Log and view body weight history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    weightEntries: Flow<List<WeightEntry>> = flowOf(emptyList()),
    onNavigateBack: () -> Unit = {},
    onAddWeight: (weight: Double, unit: WeightUnit, date: Long, notes: String?) -> Unit = { _, _, _, _ -> },
    onUpdateWeight: (WeightEntry) -> Unit = {},
    onDeleteWeight: (WeightEntry) -> Unit = {}
) {
    val entries by weightEntries.collectAsState(initial = emptyList())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    
    var showAddSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
    var deleteConfirmEntry by remember { mutableStateOf<WeightEntry?>(null) }
    
    // Calculate stats
    val latestWeight by remember(entries) {
        derivedStateOf { entries.firstOrNull() }
    }
    
    val weightChange by remember(entries) {
        derivedStateOf {
            if (entries.size >= 2) {
                val latest = entries.first().weight
                val previous = entries[1].weight
                latest - previous
            } else null
        }
    }
    
    // Get entries from last 30 days for trend
    val thirtyDayEntries by remember(entries) {
        derivedStateOf {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            entries.filter { it.date >= thirtyDaysAgo }
        }
    }
    
    val thirtyDayChange by remember(thirtyDayEntries) {
        derivedStateOf {
            if (thirtyDayEntries.size >= 2) {
                val latest = thirtyDayEntries.first().weight
                val oldest = thirtyDayEntries.last().weight
                latest - oldest
            } else null
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Weight",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            FloatingActionButton(
                onClick = { showAddSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add weight")
            }
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Scale,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No weight entries yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to log your first weight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current weight card
                item {
                    CurrentWeightCard(
                        latestWeight = latestWeight,
                        weightChange = weightChange,
                        thirtyDayChange = thirtyDayChange
                    )
                }
                
                // History section header
                item {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                
                // Weight entries list
                items(entries, key = { it.id }) { entry ->
                    WeightEntryCard(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { deleteConfirmEntry = entry }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }
    
    // Add/Edit bottom sheet
    if (showAddSheet || editingEntry != null) {
        WeightEntrySheet(
            existingEntry = editingEntry,
            defaultUnit = latestWeight?.unit ?: WeightUnit.KG,
            onDismiss = {
                showAddSheet = false
                editingEntry = null
            },
            onSave = { weight, unit, date, notes ->
                if (editingEntry != null) {
                    onUpdateWeight(editingEntry!!.copy(
                        weight = weight,
                        unit = unit,
                        date = date,
                        notes = notes
                    ))
                } else {
                    onAddWeight(weight, unit, date, notes)
                }
                showAddSheet = false
                editingEntry = null
            }
        )
    }
    
    // Delete confirmation dialog
    deleteConfirmEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEntry = null },
            title = { Text("Delete Entry") },
            text = { 
                Text("Delete weight entry from ${
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(entry.date))
                }?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteWeight(entry)
                        deleteConfirmEntry = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CurrentWeightCard(
    latestWeight: WeightEntry?,
    weightChange: Double?,
    thirtyDayChange: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Current Weight",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = latestWeight?.formatted() ?: "--",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                weightChange?.let { change ->
                    Spacer(modifier = Modifier.width(16.dp))
                    WeightChangeBadge(
                        change = change,
                        unit = latestWeight?.unit ?: WeightUnit.KG,
                        label = "vs last"
                    )
                }
            }
            
            thirtyDayChange?.let { change ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val trendIcon = when {
                        change > 0.1 -> Icons.AutoMirrored.Filled.TrendingUp
                        change < -0.1 -> Icons.AutoMirrored.Filled.TrendingDown
                        else -> Icons.AutoMirrored.Filled.TrendingFlat
                    }
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val unit = latestWeight?.unit ?: WeightUnit.KG
                    val unitStr = if (unit == WeightUnit.KG) "kg" else "lbs"
                    Text(
                        text = "${if (change >= 0) "+" else ""}${String.format("%.1f", change)} $unitStr in 30 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            latestWeight?.let { entry ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last updated: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(entry.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun WeightChangeBadge(
    change: Double,
    unit: WeightUnit,
    label: String
) {
    val isPositive = change > 0
    val backgroundColor by animateColorAsState(
        targetValue = if (isPositive) 
            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        else 
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
        label = "changeBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isPositive) 
            MaterialTheme.colorScheme.error
        else 
            MaterialTheme.colorScheme.tertiary,
        label = "changeText"
    )
    
    val unitStr = if (unit == WeightUnit.KG) "kg" else "lbs"
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${if (isPositive) "+" else ""}${String.format("%.1f", change)} $unitStr $label",
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightEntryCard(
    entry: WeightEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.formatted(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(entry.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightEntrySheet(
    existingEntry: WeightEntry?,
    defaultUnit: WeightUnit,
    onDismiss: () -> Unit,
    onSave: (weight: Double, unit: WeightUnit, date: Long, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var weightText by remember(existingEntry) { 
        mutableStateOf(existingEntry?.weight?.toString() ?: "") 
    }
    var selectedUnit by remember(existingEntry, defaultUnit) { 
        mutableStateOf(existingEntry?.unit ?: defaultUnit) 
    }
    var selectedDate by remember(existingEntry) { 
        mutableStateOf(existingEntry?.date ?: getTodayStartOfDay())
    }
    var notes by remember(existingEntry) { 
        mutableStateOf(existingEntry?.notes ?: "") 
    }
    
    val isValid by remember(weightText) {
        derivedStateOf { weightText.toDoubleOrNull() != null && weightText.toDoubleOrNull()!! > 0 }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (existingEntry != null) "Edit Weight" else "Log Weight",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weight input
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text(if (selectedUnit == WeightUnit.KG) "kg" else "lbs") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Unit selector
            Text(
                text = "Unit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedUnit == WeightUnit.KG,
                    onClick = { selectedUnit = WeightUnit.KG },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Kilograms (kg)")
                }
                SegmentedButton(
                    selected = selectedUnit == WeightUnit.LBS,
                    onClick = { selectedUnit = WeightUnit.LBS },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Pounds (lbs)")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date selector chips
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val today = getTodayStartOfDay()
            val yesterday = today - (24 * 60 * 60 * 1000)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDate == today,
                    onClick = { selectedDate = today },
                    label = { Text("Today") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = selectedDate == yesterday,
                    onClick = { selectedDate = yesterday },
                    label = { Text("Yesterday") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            
            Text(
                text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
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
                        val weight = weightText.toDoubleOrNull()
                        if (weight != null && weight > 0) {
                            onSave(weight, selectedUnit, selectedDate, notes.ifBlank { null })
                        }
                    },
                    enabled = isValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

private fun getTodayStartOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
