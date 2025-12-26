package com.workout.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.Exercise
import com.workout.app.data.ExerciseLibrary
import com.workout.app.data.entities.SetType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a workout session's lift data for an exercise.
 */
data class SessionLiftData(
    val sessionDate: Long,
    val sets: List<LiftSetData>
)

/**
 * Data class representing a single set's lift data.
 */
data class LiftSetData(
    val setNumber: Int,
    val setType: SetType,
    val weightLbs: Float?,
    val reps: Int?,
    val rpe: Float?
)

/**
 * Data class representing a historical 1RM entry.
 */
data class OneRepMaxEntry(
    val date: Long,
    val estimatedMax: Float
)

/**
 * Popup dialog showing detailed information about an exercise.
 * Includes: exercise name, targeted muscles, recent lift history, 1RM, and percentages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailPopup(
    exerciseName: String,
    isLoading: Boolean = false,
    recentSessions: List<SessionLiftData> = emptyList(),
    estimatedOneRepMax: Float? = null,
    oneRepMaxHistory: List<OneRepMaxEntry> = emptyList(),
    onDismiss: () -> Unit
) {
    // Find exercise details from library
    val exercise = remember(exerciseName) {
        ExerciseLibrary.exercises.find { it.name.equals(exerciseName, ignoreCase = true) }
            ?: Exercise(exerciseName, "Unknown")
    }
    
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with exercise name and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Exercise icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = exerciseName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Targeted muscles section
                MuscleTargetsSection(exercise = exercise)
                
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // 1RM Section
                    OneRepMaxSection(
                        estimatedOneRepMax = estimatedOneRepMax,
                        oneRepMaxHistory = oneRepMaxHistory
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Recent lift history
                    RecentLiftsSection(recentSessions = recentSessions)
                }
            }
        }
    }
}

/**
 * Section showing targeted muscles (primary and auxiliary).
 */
@Composable
private fun MuscleTargetsSection(exercise: Exercise) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Targeted Muscles",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Primary muscle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = exercise.primaryMuscle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Primary",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Auxiliary muscles
        if (exercise.auxiliaryMuscles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            exercise.auxiliaryMuscles.forEach { muscle ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = muscle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section showing estimated 1RM and percentage table.
 */
@Composable
private fun OneRepMaxSection(
    estimatedOneRepMax: Float?,
    oneRepMaxHistory: List<OneRepMaxEntry>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Estimated 1 Rep Max",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (estimatedOneRepMax != null && estimatedOneRepMax > 0) {
            // Large 1RM display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${estimatedOneRepMax.toInt()} lbs",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Estimated 1RM",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Percentage table
            Text(
                text = "Percentages of 1RM",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PercentageTable(oneRepMax = estimatedOneRepMax)
            
            // 1RM History Graph (if we have history)
            if (oneRepMaxHistory.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "1RM Over Time",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OneRepMaxGraph(history = oneRepMaxHistory)
            }
        } else {
            // No data available
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Complete a workout with this exercise to see your estimated 1RM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Table showing weight at various percentages of 1RM.
 */
@Composable
private fun PercentageTable(oneRepMax: Float) {
    val percentages = listOf(100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "~Reps",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            percentages.forEach { percent ->
                val weight = (oneRepMax * percent / 100).toInt()
                val estimatedReps = getEstimatedRepsForPercentage(percent)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (percent == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (percent == 100) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$weight lbs",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (percent == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (percent == 100) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = estimatedReps,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Get estimated reps for a given percentage of 1RM.
 * Based on the Epley formula inverse.
 */
private fun getEstimatedRepsForPercentage(percentage: Int): String {
    return when (percentage) {
        100 -> "1"
        95 -> "2"
        90 -> "3-4"
        85 -> "5-6"
        80 -> "7-8"
        75 -> "9-10"
        70 -> "11-12"
        65 -> "13-15"
        60 -> "16-18"
        55 -> "19-22"
        50 -> "23+"
        else -> "-"
    }
}

/**
 * Simple line graph showing 1RM over time.
 */
@Composable
private fun OneRepMaxGraph(history: List<OneRepMaxEntry>) {
    val sortedHistory = history.sortedBy { it.date }
    val maxValue = sortedHistory.maxOfOrNull { it.estimatedMax } ?: 0f
    val minValue = sortedHistory.minOfOrNull { it.estimatedMax } ?: 0f
    val range = (maxValue - minValue).coerceAtLeast(10f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointSpacing = if (sortedHistory.size > 1) width / (sortedHistory.size - 1) else width
                
                // Draw line connecting points
                val path = androidx.compose.ui.graphics.Path()
                sortedHistory.forEachIndexed { index, entry ->
                    val x = index * pointSpacing
                    val normalizedY = (entry.estimatedMax - minValue) / range
                    val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = Color(0xFF4CAF50),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
                
                // Draw points
                sortedHistory.forEachIndexed { index, entry ->
                    val x = index * pointSpacing
                    val normalizedY = (entry.estimatedMax - minValue) / range
                    val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                    
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 5.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }
            
            // Show latest value
            Column(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                val latest = sortedHistory.lastOrNull()
                if (latest != null) {
                    Text(
                        text = "${latest.estimatedMax.toInt()} lbs",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

/**
 * Section showing recent lift history (last 2 sessions).
 */
@Composable
private fun RecentLiftsSection(recentSessions: List<SessionLiftData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Recent Lifts",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (recentSessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "No previous data for this exercise",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            recentSessions.take(2).forEachIndexed { index, session ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                SessionLiftCard(session = session)
            }
        }
    }
}

/**
 * Card showing lift data for a single session.
 */
@Composable
private fun SessionLiftCard(session: SessionLiftData) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val workingSets = session.sets.filter { it.setType == SetType.REGULAR }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Date header
            Text(
                text = dateFormat.format(Date(session.sessionDate)),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sets header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "RPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Working sets only
            workingSets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = set.weightLbs?.let { "${it.toInt()} lbs" } ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = set.reps?.toString() ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = set.rpe?.let { String.format("%.1f", it) } ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Summary row
            if (workingSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                val totalSets = workingSets.size
                val maxWeight = workingSets.mapNotNull { it.weightLbs }.maxOrNull()
                val totalReps = workingSets.mapNotNull { it.reps }.sum()
                
                Text(
                    text = "$totalSets working sets • Max: ${maxWeight?.toInt() ?: 0} lbs • Total: $totalReps reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Reps adjustment table for 1RM calculation.
 * Maps effective reps to percentage multiplier.
 */
private val repsAdjustmentTable = mapOf(
    1 to 1.0f,
    2 to 0.94f,
    3 to 0.91f,
    4 to 0.88f,
    5 to 0.86f,
    6 to 0.83f,
    7 to 0.82f,
    8 to 0.78f,
    9 to 0.77f,
    10 to 0.75f,
    11 to 0.73f,
    12 to 0.72f
)

/**
 * Calculate estimated 1RM using RPE-based formula.
 * effectiveReps = reps + RPE - 10
 * 1RM = weight / repsAdjustment(effectiveReps)
 * 
 * @param weight The weight lifted
 * @param reps The number of reps performed
 * @param rpe The RPE (Rate of Perceived Exertion), defaults to 10 if null
 */
fun calculateOneRepMax(weight: Float, reps: Int, rpe: Float? = null): Float {
    if (reps <= 0 || weight <= 0) return 0f
    
    val effectiveRpe = rpe ?: 10f
    val effectiveReps = (reps + effectiveRpe - 10).toInt().coerceIn(1, 12)
    
    val adjustment = repsAdjustmentTable[effectiveReps] ?: 0.72f
    return weight / adjustment
}

/**
 * Find the best estimated 1RM from a list of sets.
 */
fun findBestOneRepMax(sets: List<LiftSetData>): Float {
    return sets
        .filter { it.setType == SetType.REGULAR && it.weightLbs != null && it.reps != null && it.reps > 0 }
        .maxOfOrNull { calculateOneRepMax(it.weightLbs!!, it.reps!!, it.rpe) }
        ?: 0f
}
