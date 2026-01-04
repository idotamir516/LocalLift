package com.workout.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.TrainingPhase
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.util.HomeStatType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Data class holding all stat values for the home screen.
 */
data class HomeStats(
    val workoutsThisWeek: Int = 0,
    val currentStreak: Int = 0,
    val weeklyAverage: Float = 0f,
    val totalWorkouts: Int = 0,
    val workoutsThisMonth: Int = 0,
    val workoutsThisYear: Int = 0,
    val volumeThisWeek: Float = 0f,
    val volumeLastWeek: Float = 0f,
    val volumeThisMonth: Float = 0f
)

/**
 * Home screen - Dashboard showing active phase, quick stats, and navigation to settings.
 * 
 * @param activePhase Currently active training phase, if any
 * @param recentWorkout Most recent completed workout
 * @param stats All home screen statistics
 * @param leftStatType Currently selected left stat type
 * @param rightStatType Currently selected right stat type
 * @param onSettingsClick Callback when settings icon is clicked
 * @param onPhaseClick Callback when active phase card is clicked
 * @param onRecentWorkoutClick Callback when recent workout card is clicked
 * @param onStartWorkoutClick Callback when start workout button is clicked
 * @param onLeftStatClick Callback when left stat card is tapped (cycles to next stat)
 * @param onRightStatClick Callback when right stat card is tapped (cycles to next stat)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activePhase: TrainingPhase? = null,
    recentWorkout: WorkoutSession? = null,
    stats: HomeStats = HomeStats(),
    leftStatType: HomeStatType = HomeStatType.THIS_WEEK,
    rightStatType: HomeStatType = HomeStatType.TOTAL,
    onSettingsClick: () -> Unit = {},
    onPhaseClick: () -> Unit = {},
    onRecentWorkoutClick: (Long) -> Unit = {},
    onStartWorkoutClick: () -> Unit = {},
    onLeftStatClick: () -> Unit = {},
    onRightStatClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val today = dateFormat.format(Date())
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Home",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Today's date
            Text(
                text = today,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Active Phase Card
            ActivePhaseCard(
                phase = activePhase,
                onClick = onPhaseClick
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CyclingStatCard(
                    statType = leftStatType,
                    stats = stats,
                    onClick = onLeftStatClick,
                    modifier = Modifier.weight(1f)
                )
                CyclingStatCard(
                    statType = rightStatType,
                    stats = stats,
                    onClick = onRightStatClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Recent Workout Card
            if (recentWorkout != null) {
                Text(
                    text = "Recent Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                RecentWorkoutCard(
                    session = recentWorkout,
                    onClick = { onRecentWorkoutClick(recentWorkout.id) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivePhaseCard(
    phase: TrainingPhase?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (phase != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (phase != null) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    if (phase != null) {
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        val phaseInfo = buildPhaseInfoText(phase)
                        Text(
                            text = phaseInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "No Active Phase",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to set up a training phase",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (phase != null) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
            
            // Progress bar for phases with end date
            if (phase?.endDate != null) {
                val progress = calculatePhaseProgress(phase)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 500),
                    label = "phase_progress"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * A tappable stat card that displays different statistics based on the selected type.
 * Animates when the stat type changes.
 */
@Composable
private fun CyclingStatCard(
    statType: HomeStatType,
    stats: HomeStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, value, label) = getStatDisplayInfo(statType, stats)
    
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AnimatedContent(
            targetState = Triple(icon, value, label),
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "stat_content"
        ) { (animIcon, animValue, animLabel) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = animIcon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = animValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = animLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Returns the icon, formatted value, and label for a given stat type.
 */
private fun getStatDisplayInfo(
    statType: HomeStatType,
    stats: HomeStats
): Triple<ImageVector, String, String> {
    return when (statType) {
        HomeStatType.THIS_WEEK -> Triple(
            Icons.Outlined.LocalFireDepartment,
            stats.workoutsThisWeek.toString(),
            "This Week"
        )
        HomeStatType.CURRENT_STREAK -> Triple(
            Icons.Outlined.LocalFireDepartment,
            "${stats.currentStreak}",
            "Day Streak"
        )
        HomeStatType.WEEKLY_AVG -> Triple(
            Icons.Outlined.ShowChart,
            String.format("%.1f", stats.weeklyAverage),
            "Weekly Avg"
        )
        HomeStatType.TOTAL -> Triple(
            Icons.Outlined.CalendarMonth,
            stats.totalWorkouts.toString(),
            "Total"
        )
        HomeStatType.THIS_MONTH -> Triple(
            Icons.Outlined.Today,
            stats.workoutsThisMonth.toString(),
            "This Month"
        )
        HomeStatType.THIS_YEAR -> Triple(
            Icons.Outlined.CalendarMonth,
            stats.workoutsThisYear.toString(),
            "This Year"
        )
        HomeStatType.VOLUME_THIS_WEEK -> Triple(
            Icons.Outlined.FitnessCenter,
            formatVolume(stats.volumeThisWeek),
            "Vol. This Week"
        )
        HomeStatType.VOLUME_THIS_MONTH -> Triple(
            Icons.Outlined.FitnessCenter,
            formatVolume(stats.volumeThisMonth),
            "Vol. This Month"
        )
    }
}

/**
 * Formats volume in lbs to a compact, readable format.
 * e.g., 45230 -> "45.2K", 1234567 -> "1.2M"
 */
private fun formatVolume(volume: Float): String {
    return when {
        volume >= 1_000_000 -> String.format("%.1fM", volume / 1_000_000)
        volume >= 1_000 -> String.format("%.1fK", volume / 1_000)
        else -> String.format("%.0f", volume)
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentWorkoutCard(
    session: WorkoutSession,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    val duration = session.completedAt?.let { completedAt ->
        val durationMs = completedAt - session.startedAt
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } ?: "In progress"
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.templateName ?: "Empty Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${dateFormat.format(Date(session.startedAt))} • $duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View workout",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun buildPhaseInfoText(phase: TrainingPhase): String {
    val today = System.currentTimeMillis()
    val startDate = phase.startDate
    
    // Calculate days in phase
    val daysInPhase = TimeUnit.MILLISECONDS.toDays(today - startDate).toInt() + 1
    val typeName = phase.type.name.lowercase().replaceFirstChar { it.uppercase() }
    
    return if (phase.endDate != null) {
        val totalDays = TimeUnit.MILLISECONDS.toDays(phase.endDate - startDate).toInt() + 1
        "$typeName • Day $daysInPhase of $totalDays"
    } else {
        "$typeName • Day $daysInPhase"
    }
}

private fun calculatePhaseProgress(phase: TrainingPhase): Float {
    val today = System.currentTimeMillis()
    val startDate = phase.startDate
    val endDate = phase.endDate ?: return 0f
    
    val totalDuration = endDate - startDate
    val elapsed = today - startDate
    
    return (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
}
