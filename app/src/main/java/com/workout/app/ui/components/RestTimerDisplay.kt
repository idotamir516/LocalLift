package com.workout.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workout.app.util.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A visual display for the rest timer.
 * Shows a circular progress indicator with time remaining and controls.
 * Can be minimized to show just the time remaining.
 * 
 * @param startExpanded Whether to start in expanded mode (true) or minimized (false)
 */
@Composable
fun RestTimerDisplay(
    timerState: TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onAddTime: () -> Unit,
    onSubtractTime: () -> Unit = {},
    startExpanded: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(startExpanded) }
    
    // Reset expanded state when a new timer starts (totalSeconds changes to a new value)
    LaunchedEffect(timerState.totalSeconds) {
        if (timerState.totalSeconds > 0) {
            isExpanded = startExpanded
        }
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = timerState.progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isExpanded) 16.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row - shows time only when minimized, just expand/collapse when expanded
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = if (isExpanded) Arrangement.End else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer info - only show when minimized
                if (!isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Small circular progress
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(40.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 4.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                        
                        Column {
                            Text(
                                text = "REST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = timerState.formattedTime,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick controls in minimized view
                    if (!isExpanded) {
                        // -5s button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onSubtractTime() },
                                    onPress = {
                                        var job: Job? = null
                                        try {
                                            job = scope.launch {
                                                delay(400)
                                                while (true) {
                                                    onSubtractTime()
                                                    delay(100)
                                                }
                                            }
                                            awaitRelease()
                                        } finally {
                                            job?.cancel()
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                "-5",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // +5s button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onAddTime() },
                                    onPress = {
                                        var job: Job? = null
                                        try {
                                            job = scope.launch {
                                                delay(400)
                                                while (true) {
                                                    onAddTime()
                                                    delay(100)
                                                }
                                            }
                                            awaitRelease()
                                        } finally {
                                            job?.cancel()
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                "+5",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Skip button
                        Surface(
                            onClick = onSkip,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "Skip",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Expand/collapse icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Minimize" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded content - controls
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Large circular progress with time (only in expanded view)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        // Background track
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(140.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            strokeCap = StrokeCap.Round
                        )
                        
                        // Progress indicator
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(140.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            strokeCap = StrokeCap.Round
                        )
                        
                        Text(
                            text = timerState.formattedTime,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
            
                    // Controls row: -5s, Pause/Resume, +5s
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -5 seconds button with long-press repeat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onSubtractTime() },
                                    onPress = {
                                        var job: Job? = null
                                        try {
                                            // Wait for long press threshold
                                            job = scope.launch {
                                                delay(400) // Initial delay before repeat starts
                                                while (true) {
                                                    onSubtractTime()
                                                    delay(100) // Repeat interval
                                                }
                                            }
                                            awaitRelease()
                                        } finally {
                                            job?.cancel()
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                "-5s",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                
                        // Pause/Resume button
                        FilledIconButton(
                            onClick = if (timerState.isRunning) onPause else onResume,
                            modifier = Modifier.size(60.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (timerState.isRunning) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (timerState.isRunning) "Pause" else "Resume",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                
                        // +5 seconds button with long-press repeat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onAddTime() },
                                    onPress = {
                                        var job: Job? = null
                                        try {
                                            // Wait for long press threshold
                                            job = scope.launch {
                                                delay(400) // Initial delay before repeat starts
                                                while (true) {
                                                    onAddTime()
                                                    delay(100) // Repeat interval
                                                }
                                            }
                                            awaitRelease()
                                        } finally {
                                            job?.cancel()
                                        }
                                    }
                                )
                            }
                        ) {
                            Text(
                                "+5s",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
            
                    Spacer(modifier = Modifier.height(12.dp))
            
                    // Skip button at the bottom
                    Surface(
                        onClick = onSkip,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Skip rest",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact version of the rest timer for inline display.
 */
@Composable
fun CompactRestTimer(
    timerState: TimerState,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = timerState.progress,
        animationSpec = tween(durationMillis = 300),
        label = "compact_timer_progress"
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Rest: ${timerState.formattedTime}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Surface(
                onClick = onSkip,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "Skip",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
