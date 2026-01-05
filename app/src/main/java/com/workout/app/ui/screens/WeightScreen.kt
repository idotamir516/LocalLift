package com.workout.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.PhaseType
import com.workout.app.data.entities.TrainingPhase
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
    trainingPhases: List<TrainingPhase> = emptyList(),
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
                
                // Weight chart (show if at least 2 entries)
                if (entries.size >= 2) {
                    item {
                        WeightChart(
                            entries = entries.sortedBy { it.date },
                            phases = trainingPhases,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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

/**
 * Time range options for chart display.
 */
private enum class ChartTimeRange(val label: String, val days: Int?) {
    ONE_MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    SIX_MONTHS("6M", 180),
    ONE_YEAR("1Y", 365),
    ALL("All", null)
}

/**
 * Weight chart showing body weight over time.
 */
@Composable
private fun WeightChart(
    entries: List<WeightEntry>,
    phases: List<TrainingPhase> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (entries.size < 2) return
    
    var selectedRange by remember { mutableStateOf(ChartTimeRange.ALL) }
    
    // Filter entries based on selected time range
    val filteredEntries = remember(entries, selectedRange) {
        if (selectedRange.days == null) {
            entries
        } else {
            val cutoffDate = System.currentTimeMillis() - (selectedRange.days!! * 24L * 60 * 60 * 1000)
            entries.filter { it.date >= cutoffDate }
        }
    }
    
    // Need at least 2 entries to draw a chart
    if (filteredEntries.size < 2) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weight Over Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimeRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Not enough data for this time range",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    // Convert all weights to the same unit (use the most recent entry's unit)
    val displayUnit = filteredEntries.lastOrNull()?.unit ?: WeightUnit.KG
    val weights = filteredEntries.map { 
        if (displayUnit == WeightUnit.KG) it.toKg() else it.toLbs() 
    }
    
    val maxWeight = weights.maxOrNull() ?: 0.0
    val minWeight = weights.minOrNull() ?: 0.0
    val range = (maxWeight - minWeight).coerceAtLeast(0.1)
    
    val minDate = filteredEntries.first().date
    val maxDate = filteredEntries.last().date
    val dateRange = (maxDate - minDate).coerceAtLeast(1L)
    
    // Filter phases that overlap with the chart's date range
    val relevantPhases = phases.filter { phase ->
        val phaseEnd = phase.endDate ?: System.currentTimeMillis()
        phase.startDate <= maxDate && phaseEnd >= minDate
    }
    
    // Color mapping for phase types
    fun getPhaseColor(phaseType: PhaseType): Color {
        return when (phaseType) {
            PhaseType.BULK -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            PhaseType.CUT -> Color(0xFFF44336).copy(alpha = 0.15f)
            PhaseType.MAINTENANCE -> Color(0xFF9E9E9E).copy(alpha = 0.15f)
            PhaseType.STRENGTH -> Color(0xFF2196F3).copy(alpha = 0.15f)
            PhaseType.HYPERTROPHY -> Color(0xFF9C27B0).copy(alpha = 0.15f)
            PhaseType.DELOAD -> Color(0xFFFF9800).copy(alpha = 0.15f)
            PhaseType.CUSTOM -> Color(0xFF607D8B).copy(alpha = 0.15f)
        }
    }
    
    fun getPhaseLabelColor(phaseType: PhaseType): Color {
        return when (phaseType) {
            PhaseType.BULK -> Color(0xFF388E3C)
            PhaseType.CUT -> Color(0xFFD32F2F)
            PhaseType.MAINTENANCE -> Color(0xFF757575)
            PhaseType.STRENGTH -> Color(0xFF1976D2)
            PhaseType.HYPERTROPHY -> Color(0xFF7B1FA2)
            PhaseType.DELOAD -> Color(0xFFF57C00)
            PhaseType.CUSTOM -> Color(0xFF455A64)
        }
    }
    
    val unitStr = if (displayUnit == WeightUnit.KG) "kg" else "lbs"
    val lineColor = Color(0xFF2196F3) // Blue for weight
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weight Over Time",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Phase labels row (if we have phases)
            if (relevantPhases.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 49.dp) // Account for Y-axis width
                        .height(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    relevantPhases.forEach { phase ->
                        val phaseEnd = phase.endDate ?: System.currentTimeMillis()
                        val startX = ((phase.startDate.coerceAtLeast(minDate) - minDate).toFloat() / dateRange).coerceIn(0f, 1f)
                        val endX = ((phaseEnd.coerceAtMost(maxDate) - minDate).toFloat() / dateRange).coerceIn(0f, 1f)
                        val width = (endX - startX).coerceAtLeast(0f)
                        
                        if (width > 0.05f) {
                            Spacer(modifier = Modifier.weight(startX.coerceAtLeast(0.001f)))
                            Text(
                                text = phase.name.take(10) + if (phase.name.length > 10) "…" else "",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = getPhaseLabelColor(phase.type),
                                modifier = Modifier.weight(width.coerceAtLeast(0.001f)),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight((1f - endX).coerceAtLeast(0.001f)))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .width(45.dp)
                        .height(100.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%.1f", maxWeight),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.1f", minWeight),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Chart canvas
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingTop = height * 0.1f
                    val paddingBottom = height * 0.1f
                    val usableHeight = height - paddingTop - paddingBottom
                    
                    // Draw phase background bands first
                    relevantPhases.forEach { phase ->
                        val phaseEnd = phase.endDate ?: System.currentTimeMillis()
                        val startX = ((phase.startDate.coerceAtLeast(minDate) - minDate).toFloat() / dateRange) * width
                        val endX = ((phaseEnd.coerceAtMost(maxDate) - minDate).toFloat() / dateRange) * width
                        
                        if (endX > startX) {
                            drawRect(
                                color = getPhaseColor(phase.type),
                                topLeft = Offset(startX, 0f),
                                size = Size(endX - startX, height)
                            )
                        }
                    }
                    
                    // Draw line path
                    val path = Path()
                    filteredEntries.forEachIndexed { index, entry ->
                        val weight = if (displayUnit == WeightUnit.KG) entry.toKg() else entry.toLbs()
                        val x = ((entry.date - minDate).toFloat() / dateRange) * width
                        val normalizedY = (weight - minWeight) / range
                        val y = height - paddingBottom - (normalizedY.toFloat() * usableHeight)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                    
                    // Draw points
                    filteredEntries.forEach { entry ->
                        val weight = if (displayUnit == WeightUnit.KG) entry.toKg() else entry.toLbs()
                        val x = ((entry.date - minDate).toFloat() / dateRange) * width
                        val normalizedY = (weight - minWeight) / range
                        val y = height - paddingBottom - (normalizedY.toFloat() * usableHeight)
                        
                        drawCircle(
                            color = lineColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            // X-axis date labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 49.dp), // Offset for Y-axis
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                Text(
                    text = dateFormat.format(Date(minDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = dateFormat.format(Date(maxDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Compact time range selector using filter chips.
 */
@Composable
private fun TimeRangeSelector(
    selectedRange: ChartTimeRange,
    onRangeSelected: (ChartTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        ChartTimeRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.height(28.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
