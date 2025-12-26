package com.workout.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.data.dao.FullTemplateWithExercisesAndSets
import com.workout.app.data.dao.TemplateWithExercises
import com.workout.app.data.entities.CustomExercise
import com.workout.app.ui.theme.DarkBackground
import com.workout.app.util.MuscleSetAnalysis
import com.workout.app.util.ProgramAnalysis
import com.workout.app.util.ProgramAnalyzer
import com.workout.app.util.SettingsManager
import com.workout.app.util.TemplateTimeAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Program Analyzer screen - select templates and view set volume analysis by muscle group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramAnalyzerScreen(
    templates: Flow<List<TemplateWithExercises>> = flowOf(emptyList()),
    customExercises: Flow<List<CustomExercise>> = flowOf(emptyList()),
    settingsManager: SettingsManager,
    onLoadFullTemplate: suspend (Long) -> FullTemplateWithExercisesAndSets?,
    onBack: () -> Unit = {}
) {
    val templateList by templates.collectAsState(initial = emptyList())
    val customExerciseList by customExercises.collectAsState(initial = emptyList())
    val countWarmupAsEffective by settingsManager.countWarmupAsEffective.collectAsState()
    val countDropSetAsEffective by settingsManager.countDropSetAsEffective.collectAsState()
    val estimatedSecondsPerSet by settingsManager.estimatedSecondsPerSet.collectAsState()
    
    val selectedTemplateIds = remember { mutableStateListOf<Long>() }
    var analysis by remember { mutableStateOf<ProgramAnalysis?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (showResults) "Analysis Results" else "Program Analyzer",
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showResults) {
                            showResults = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (showResults && analysis != null) {
            // Show analysis results
            AnalysisResultsView(
                analysis = analysis!!,
                countWarmupAsEffective = countWarmupAsEffective,
                countDropSetAsEffective = countDropSetAsEffective,
                estimatedSecondsPerSet = estimatedSecondsPerSet,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // Template selection view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Instructions
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Select the templates that make up your weekly program to analyze volume per muscle group.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Template list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templateList, key = { it.template.id }) { template ->
                        val isSelected = selectedTemplateIds.contains(template.template.id)
                        
                        TemplateSelectionItem(
                            template = template,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedTemplateIds.remove(template.template.id)
                                } else {
                                    selectedTemplateIds.add(template.template.id)
                                }
                            }
                        )
                    }
                    
                    // Empty state
                    if (templateList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No templates yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Create some workout templates first",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Analyze button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${selectedTemplateIds.size} template${if (selectedTemplateIds.size != 1) "s" else ""} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Button(
                            onClick = {
                                isAnalyzing = true
                                // Note: In a real implementation, this would be in a coroutine
                                // For now, we'll use a LaunchedEffect pattern in the actual integration
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedTemplateIds.isNotEmpty() && !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAnalyzing) "Analyzing..." else "Analyze Program",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Handle analysis trigger
    if (isAnalyzing && selectedTemplateIds.isNotEmpty()) {
        androidx.compose.runtime.LaunchedEffect(selectedTemplateIds.toList()) {
            val fullTemplates = selectedTemplateIds.mapNotNull { id ->
                onLoadFullTemplate(id)
            }
            analysis = ProgramAnalyzer.analyze(fullTemplates, customExerciseList)
            isAnalyzing = false
            showResults = true
        }
    }
}

@Composable
private fun TemplateSelectionItem(
    template: TemplateWithExercises,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${template.exercises.size} exercise${if (template.exercises.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultsView(
    analysis: ProgramAnalysis,
    countWarmupAsEffective: Boolean,
    countDropSetAsEffective: Boolean,
    estimatedSecondsPerSet: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card
        item {
            SummaryCard(analysis = analysis)
        }
        
        // Section header
        item {
            Text(
                text = "Volume by Muscle Group",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
        
        // Muscle analysis cards - sorted by effective sets with current settings
        items(analysis.sortedByEffectiveSets(countWarmupAsEffective, countDropSetAsEffective)) { muscleAnalysis ->
            MuscleAnalysisCard(
                muscleAnalysis = muscleAnalysis,
                countWarmupAsEffective = countWarmupAsEffective,
                countDropSetAsEffective = countDropSetAsEffective
            )
        }
        
        // Time Analysis Section
        if (analysis.templateTimeAnalysis.isNotEmpty()) {
            item {
                Text(
                    text = "Estimated Workout Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            items(analysis.templateTimeAnalysis) { templateTime ->
                TemplateTimeCard(
                    templateTime = templateTime,
                    estimatedSecondsPerSet = estimatedSecondsPerSet
                )
            }
        }
        
        // Legend
        item {
            LegendCard()
        }
    }
}

@Composable
private fun SummaryCard(
    analysis: ProgramAnalysis,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Program Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Templates analyzed
                Text(
                    text = "Templates: ${analysis.templateNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = analysis.totalExercises.toString(),
                        label = "Exercises"
                    )
                    StatItem(
                        value = analysis.totalSets.toString(),
                        label = "Total Sets"
                    )
                    StatItem(
                        value = analysis.muscleAnalysis.size.toString(),
                        label = "Muscles"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MuscleAnalysisCard(
    muscleAnalysis: MuscleSetAnalysis,
    countWarmupAsEffective: Boolean,
    countDropSetAsEffective: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val effectiveSets = muscleAnalysis.getEffectiveSets(countWarmupAsEffective, countDropSetAsEffective)
    
    // Calculate effective counts for badges (only show sets that count)
    val primaryEffectiveCount = muscleAnalysis.primarySets.getEffectiveCount(countWarmupAsEffective, countDropSetAsEffective)
    val auxiliaryEffectiveCount = muscleAnalysis.auxiliarySets.getEffectiveCount(countWarmupAsEffective, countDropSetAsEffective)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Muscle name and effective sets
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = muscleAnalysis.muscleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", effectiveSets)} effective sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Set counts summary (only show effective counts)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (primaryEffectiveCount > 0) {
                        SetBadge(
                            count = primaryEffectiveCount,
                            label = "P",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (auxiliaryEffectiveCount > 0) {
                        SetBadge(
                            count = auxiliaryEffectiveCount,
                            label = "A",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Primary sets breakdown
                    if (muscleAnalysis.totalPrimarySets > 0) {
                        Text(
                            text = "Primary (direct targeting)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SetBreakdownRow(counts = muscleAnalysis.primarySets)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Auxiliary sets breakdown
                    if (muscleAnalysis.totalAuxiliarySets > 0) {
                        Text(
                            text = "Auxiliary (indirect involvement)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SetBreakdownRow(counts = muscleAnalysis.auxiliarySets)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetBadge(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SetBreakdownRow(
    counts: com.workout.app.util.SetCountsByType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SetTypeItem(count = counts.warmup, label = "Warmup", color = Color(0xFFFFB74D))
        SetTypeItem(count = counts.regular, label = "Working", color = Color(0xFF81C784))
        SetTypeItem(count = counts.dropSet, label = "Drop", color = Color(0xFFE57373))
    }
}

@Composable
private fun SetTypeItem(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemplateTimeCard(
    templateTime: TemplateTimeAnalysis,
    estimatedSecondsPerSet: Int,
    modifier: Modifier = Modifier
) {
    val totalTimeSeconds = templateTime.getEstimatedTotalTimeSeconds(estimatedSecondsPerSet)
    val restTimeMinutes = templateTime.totalRestTimeSeconds / 60
    val setTimeSeconds = templateTime.totalSets * estimatedSecondsPerSet
    val setTimeMinutes = setTimeSeconds / 60
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Template name and total time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = templateTime.templateName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = templateTime.formatTime(totalTimeSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Breakdown row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Sets info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${templateTime.totalSets}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Set time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${setTimeMinutes}m",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "lifting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Rest time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${restTimeMinutes}m",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "rest",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Exercises
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${templateTime.exerciseCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LegendItem(
                badge = "P",
                badgeColor = MaterialTheme.colorScheme.primary,
                description = "Primary sets - muscle is directly targeted"
            )
            LegendItem(
                badge = "A",
                badgeColor = MaterialTheme.colorScheme.tertiary,
                description = "Auxiliary sets - muscle assists the movement"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Effective sets = Primary + (Auxiliary × 0.5)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LegendItem(
    badge: String,
    badgeColor: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetBadge(count = 0, label = badge, color = badgeColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
