package com.workout.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.dao.CompletedSetData
import com.workout.app.data.dao.SessionVolume
import com.workout.app.data.entities.CustomExercise
import com.workout.app.data.entities.PhaseType
import com.workout.app.data.entities.SetType
import com.workout.app.data.entities.TrainingPhase
import com.workout.app.data.entities.WorkoutSession
import com.workout.app.data.Exercise
import com.workout.app.data.ExerciseLibrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Progress screen - Shows workout calendar, history, and stats with tabs.
 * 
 * @param completedSessions Flow of completed workout sessions
 * @param workoutDates Flow of timestamps when workouts were completed (for calendar)
 * @param sessionVolumes Flow of session volumes for the weekly volume chart
 * @param completedSets Flow of completed sets for effective sets calculation
 * @param customExercises List of custom exercises for muscle group lookup
 * @param trainingPhases List of training phases for chart background bands
 * @param countWarmupAsEffective Whether warmup sets count as effective
 * @param countDropSetAsEffective Whether drop sets count as effective
 * @param onWorkoutClick Callback when a workout is clicked to view details
 * @param onShareClick Callback when share option is clicked
 * @param onSaveToFileClick Callback when save to file option is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    completedSessions: Flow<List<WorkoutSession>> = flowOf(emptyList()),
    workoutDates: Flow<List<Long>> = flowOf(emptyList()),
    sessionVolumes: Flow<List<SessionVolume>> = flowOf(emptyList()),
    completedSets: Flow<List<CompletedSetData>> = flowOf(emptyList()),
    customExercises: List<CustomExercise> = emptyList(),
    trainingPhases: List<TrainingPhase> = emptyList(),
    countWarmupAsEffective: Boolean = false,
    countDropSetAsEffective: Boolean = true,
    onWorkoutClick: (sessionId: Long) -> Unit = {},
    onShareClick: () -> Unit = {},
    onSaveToFileClick: () -> Unit = {}
) {
    val sessions by completedSessions.collectAsState(initial = emptyList())
    val dates by workoutDates.collectAsState(initial = emptyList())
    val volumes by sessionVolumes.collectAsState(initial = emptyList())
    val allSets by completedSets.collectAsState(initial = emptyList())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showExportMenu by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Convert workout dates to a set of day identifiers for quick lookup
    val workoutDaysSet by remember(dates) {
        derivedStateOf {
            dates.map { timestamp ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = timestamp
                // Create a unique identifier for each day: YYYYMMDD
                cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH)
            }.toSet()
        }
    }
    
    val tabs = listOf(
        TabItem("Calendar", Icons.Outlined.CalendarMonth),
        TabItem("History", Icons.Outlined.History),
        TabItem("Stats", Icons.Outlined.BarChart)
    )
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Progress",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Export"
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    showExportMenu = false
                                    onShareClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Share, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save to File") },
                                onClick = {
                                    showExportMenu = false
                                    onSaveToFileClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Save, contentDescription = null)
                                }
                            )
                        }
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
        ) {
            // Tab Row
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = null) }
                    )
                }
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> CalendarContent(
                    workoutDaysSet = workoutDaysSet,
                    sessions = sessions,
                    onWorkoutClick = onWorkoutClick
                )
                1 -> {
                    if (sessions.isEmpty()) {
                        EmptyHistoryState()
                    } else {
                        HistoryContent(
                            sessions = sessions,
                            onWorkoutClick = onWorkoutClick,
                            modifier = Modifier
                        )
                    }
                }
                2 -> StatsContent(
                    sessionVolumes = volumes,
                    completedSets = allSets,
                    customExercises = customExercises,
                    trainingPhases = trainingPhases,
                    countWarmupAsEffective = countWarmupAsEffective,
                    countDropSetAsEffective = countDropSetAsEffective
                )
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun CalendarContent(
    workoutDaysSet: Set<Int>,
    sessions: List<WorkoutSession>,
    onWorkoutClick: (sessionId: Long) -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    
    // Get sessions for selected date
    val sessionsForSelectedDate by remember(selectedDate, sessions) {
        derivedStateOf {
            selectedDate?.let { date ->
                val startOfDay = date.clone() as Calendar
                startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                startOfDay.set(Calendar.MINUTE, 0)
                startOfDay.set(Calendar.SECOND, 0)
                startOfDay.set(Calendar.MILLISECOND, 0)
                
                val endOfDay = date.clone() as Calendar
                endOfDay.set(Calendar.HOUR_OF_DAY, 23)
                endOfDay.set(Calendar.MINUTE, 59)
                endOfDay.set(Calendar.SECOND, 59)
                endOfDay.set(Calendar.MILLISECOND, 999)
                
                sessions.filter { session ->
                    session.completedAt?.let { completedAt ->
                        completedAt >= startOfDay.timeInMillis && completedAt <= endOfDay.timeInMillis
                    } ?: false
                }
            } ?: emptyList()
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Month Navigation
                    MonthNavigation(
                        currentMonth = currentMonth,
                        onPreviousMonth = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        },
                        onNextMonth = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Day of week headers
                    DayOfWeekHeaders()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar Grid
                    CalendarGrid(
                        currentMonth = currentMonth,
                        workoutDaysSet = workoutDaysSet,
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = if (selectedDate?.timeInMillis == date.timeInMillis) null else date
                        }
                    )
                }
            }
        }
        
        // Stats summary
        item {
            val workoutsThisMonth = remember(currentMonth, sessions) {
                val startOfMonth = (currentMonth.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endOfMonth = (currentMonth.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                sessions.count { session ->
                    session.completedAt?.let { it in startOfMonth.timeInMillis..endOfMonth.timeInMillis } ?: false
                }
            }
            
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$workoutsThisMonth workouts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "in ${monthFormat.format(currentMonth.time)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Selected date workouts
        if (selectedDate != null && sessionsForSelectedDate.isNotEmpty()) {
            item {
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                Text(
                    text = dateFormat.format(selectedDate!!.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(sessionsForSelectedDate, key = { it.id }) { session ->
                WorkoutHistoryCard(
                    session = session,
                    onClick = { onWorkoutClick(session.id) }
                )
            }
        }
    }
}

@Composable
private fun MonthNavigation(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        
        Text(
            text = monthYearFormat.format(currentMonth.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun DayOfWeekHeaders() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    workoutDaysSet: Set<Int>,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val today = Calendar.getInstance()
    val isCurrentMonth = currentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            currentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH)
    val currentDay = today.get(Calendar.DAY_OF_MONTH)
    
    // Create list of day cells (including empty cells for padding)
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7 // Ceiling division
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (week in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayNumber = cellIndex - firstDayOfWeek + 1
                    
                    if (dayNumber in 1..daysInMonth) {
                        val dayCalendar = (currentMonth.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        val dayId = dayCalendar.get(Calendar.YEAR) * 10000 + 
                                dayCalendar.get(Calendar.MONTH) * 100 + 
                                dayCalendar.get(Calendar.DAY_OF_MONTH)
                        val hasWorkout = workoutDaysSet.contains(dayId)
                        val isToday = isCurrentMonth && dayNumber == currentDay
                        val isSelected = selectedDate?.let {
                            it.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
                            it.get(Calendar.MONTH) == dayCalendar.get(Calendar.MONTH) &&
                            it.get(Calendar.DAY_OF_MONTH) == dayCalendar.get(Calendar.DAY_OF_MONTH)
                        } ?: false
                        
                        CalendarDay(
                            day = dayNumber,
                            hasWorkout = hasWorkout,
                            isToday = isToday,
                            isSelected = isSelected,
                            onClick = { onDateSelected(dayCalendar) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    hasWorkout: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            hasWorkout -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "dayBackground"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            hasWorkout -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayText"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || hasWorkout) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (isToday && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No workouts yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete a workout to see your progress here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistoryContent(
    sessions: List<WorkoutSession>,
    onWorkoutClick: (sessionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions, key = { it.id }) { session ->
            WorkoutHistoryCard(
                session = session,
                onClick = { onWorkoutClick(session.id) }
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    session: WorkoutSession,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    
    val duration = session.completedAt?.let { completedAt ->
        val durationMs = completedAt - session.startedAt
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } ?: "In progress"
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${dateFormat.format(Date(session.startedAt))} • $duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
/**
 * Enum for volume chart time range selection.
 */
private enum class VolumeChartTimeRange(val days: Int?, val label: String) {
    ONE_MONTH(30, "1M"),
    THREE_MONTHS(90, "3M"),
    SIX_MONTHS(180, "6M"),
    ONE_YEAR(365, "1Y"),
    ALL(null, "All")
}

/**
 * Data class representing weekly volume.
 */
private data class WeeklyVolume(
    val weekStart: Long, // Start of the week (Monday)
    val volume: Float
)

/**
 * Data class representing effective sets for a muscle group.
 */
private data class MuscleEffectiveSets(
    val muscleName: String,
    val effectiveSets: Float,
    val primarySets: Int,
    val auxiliarySets: Int
)

@Composable
private fun StatsContent(
    sessionVolumes: List<SessionVolume>,
    completedSets: List<CompletedSetData>,
    customExercises: List<CustomExercise>,
    trainingPhases: List<TrainingPhase>,
    countWarmupAsEffective: Boolean,
    countDropSetAsEffective: Boolean
) {
    // Build custom exercise lookup
    val customExerciseMap = remember(customExercises) {
        customExercises.associateBy { it.name }
    }
    
    // Calculate effective sets by muscle group
    val muscleEffectiveSets = remember(completedSets, countWarmupAsEffective, countDropSetAsEffective) {
        if (completedSets.isEmpty()) {
            emptyList()
        } else {
            val muscleMap = mutableMapOf<String, MutableMap<String, Int>>() // muscle -> (type -> count)
            
            for (set in completedSets) {
                // Check if set type counts
                val counts = when (set.setType) {
                    SetType.WARMUP -> countWarmupAsEffective
                    SetType.DROP_SET -> countDropSetAsEffective
                    SetType.REGULAR -> true
                }
                if (!counts) continue
                
                // Find exercise in library or custom exercises
                val exercise = ExerciseLibrary.exercises.find { it.name == set.exerciseName }
                val customExercise = customExerciseMap[set.exerciseName]
                
                // Get primary and auxiliary muscles
                val primaryMuscle: String?
                val auxiliaryMuscles: List<String>
                
                when {
                    exercise != null -> {
                        primaryMuscle = exercise.primaryMuscle
                        auxiliaryMuscles = exercise.auxiliaryMuscles
                    }
                    customExercise != null -> {
                        primaryMuscle = customExercise.primaryMuscle
                        auxiliaryMuscles = customExercise.auxiliaryMuscles
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    }
                    else -> continue // Unknown exercise - skip
                }
                
                // Add to primary muscle
                primaryMuscle?.let { muscle ->
                    val muscleCounts = muscleMap.getOrPut(muscle) { mutableMapOf("primary" to 0, "auxiliary" to 0) }
                    muscleCounts["primary"] = (muscleCounts["primary"] ?: 0) + 1
                }
                
                // Add to auxiliary muscles
                for (auxMuscle in auxiliaryMuscles) {
                    val muscleCounts = muscleMap.getOrPut(auxMuscle) { mutableMapOf("primary" to 0, "auxiliary" to 0) }
                    muscleCounts["auxiliary"] = (muscleCounts["auxiliary"] ?: 0) + 1
                }
            }
            
            // Convert to MuscleEffectiveSets and calculate effective sets
            // Primary = 1.0, Auxiliary = 0.5 (same formula as ProgramAnalyzer)
            muscleMap.map { (muscle, counts) ->
                val primary = counts["primary"] ?: 0
                val auxiliary = counts["auxiliary"] ?: 0
                MuscleEffectiveSets(
                    muscleName = muscle,
                    effectiveSets = primary + (auxiliary * 0.5f),
                    primarySets = primary,
                    auxiliarySets = auxiliary
                )
            }.sortedByDescending { it.effectiveSets }
        }
    }
    
    // Split into top 3 and bottom 3 (only if we have more than 6)
    val topMuscles = muscleEffectiveSets.take(3)
    val bottomMuscles = if (muscleEffectiveSets.size > 6) {
        muscleEffectiveSets.takeLast(3)
    } else if (muscleEffectiveSets.size > 3) {
        muscleEffectiveSets.drop(3)
    } else {
        emptyList()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Effective Sets by Muscle Group
        item {
            EffectiveSetsCard(
                topMuscles = topMuscles,
                bottomMuscles = bottomMuscles,
                totalMuscleGroups = muscleEffectiveSets.size
            )
        }
        
        item {
            WeeklyVolumeChart(
                sessionVolumes = sessionVolumes,
                phases = trainingPhases
            )
        }
    }
}

/**
 * Card showing effective sets by muscle group with top 3 and bottom 3.
 */
@Composable
private fun EffectiveSetsCard(
    topMuscles: List<MuscleEffectiveSets>,
    bottomMuscles: List<MuscleEffectiveSets>,
    totalMuscleGroups: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Effective Sets by Muscle Group",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Primary = 1 set, Auxiliary = 0.5 sets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            if (totalMuscleGroups == 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Complete some workouts to see your muscle group distribution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Top 3 Section
                if (topMuscles.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Most Trained",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50) // Green
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    topMuscles.forEachIndexed { index, muscle ->
                        MuscleGroupRow(
                            rank = index + 1,
                            muscle = muscle,
                            isTop = true
                        )
                        if (index < topMuscles.lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
                
                // Bottom 3 Section
                if (bottomMuscles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Needs Attention",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF44336) // Red
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    bottomMuscles.forEachIndexed { index, muscle ->
                        val rank = totalMuscleGroups - bottomMuscles.size + index + 1
                        MuscleGroupRow(
                            rank = rank,
                            muscle = muscle,
                            isTop = false
                        )
                        if (index < bottomMuscles.lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single row showing muscle group with rank and effective sets.
 */
@Composable
private fun MuscleGroupRow(
    rank: Int,
    muscle: MuscleEffectiveSets,
    isTop: Boolean,
    modifier: Modifier = Modifier
) {
    val rankColor = if (isTop) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = rankColor
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Muscle name
        Text(
            text = muscle.muscleName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // Effective sets count
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format("%.1f", muscle.effectiveSets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "sets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Weekly volume chart showing total training volume per week over time.
 */
@Composable
private fun WeeklyVolumeChart(
    sessionVolumes: List<SessionVolume>,
    phases: List<TrainingPhase> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(VolumeChartTimeRange.ALL) }
    
    // Group sessions by week and sum volumes, including weeks with zero volume
    val weeklyVolumes = remember(sessionVolumes) {
        if (sessionVolumes.isEmpty()) {
            emptyList()
        } else {
            // Create a map of week start -> volume
            val volumeByWeek = sessionVolumes
                .groupBy { volume ->
                    // Get the start of the week (Monday) for this session
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = volume.completedAt
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                .mapValues { (_, volumes) -> volumes.sumOf { it.volume.toDouble() }.toFloat() }
            
            // Find the earliest and latest weeks
            val minWeek = volumeByWeek.keys.minOrNull() ?: return@remember emptyList()
            val maxWeek = volumeByWeek.keys.maxOrNull() ?: return@remember emptyList()
            
            // Generate all weeks between min and max (inclusive)
            val allWeeks = mutableListOf<WeeklyVolume>()
            val cal = Calendar.getInstance()
            cal.timeInMillis = minWeek
            
            while (cal.timeInMillis <= maxWeek) {
                val weekStart = cal.timeInMillis
                val volume = volumeByWeek[weekStart] ?: 0f
                allWeeks.add(WeeklyVolume(weekStart, volume))
                cal.add(Calendar.WEEK_OF_YEAR, 1)
            }
            
            allWeeks
        }
    }
    
    // Filter based on selected time range
    val filteredVolumes = remember(weeklyVolumes, selectedRange) {
        if (selectedRange.days == null) {
            weeklyVolumes
        } else {
            val cutoffDate = System.currentTimeMillis() - (selectedRange.days!! * 24L * 60 * 60 * 1000)
            weeklyVolumes.filter { it.weekStart >= cutoffDate }
        }
    }
    
    // Need at least 2 weeks to draw a chart
    if (filteredVolumes.size < 2) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Volume",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimeRangeSelectorProgress(
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (sessionVolumes.isEmpty()) 
                        "Complete some workouts to see your volume trends"
                    else
                        "Not enough data for this time range",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        return
    }
    
    val maxVolume = filteredVolumes.maxOfOrNull { it.volume } ?: 0f
    val minVolume = filteredVolumes.minOfOrNull { it.volume } ?: 0f
    val range = (maxVolume - minVolume).coerceAtLeast(1f)
    
    val minDate = filteredVolumes.first().weekStart
    val maxDate = filteredVolumes.last().weekStart
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
    
    val lineColor = Color(0xFF9C27B0) // Purple for volume
    
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
                text = "Weekly Volume",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimeRangeSelectorProgress(
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
                        .padding(start = 49.dp)
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
                        text = formatVolumeLabel(maxVolume),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatVolumeLabel(minVolume),
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
                    val pointSpacing = if (filteredVolumes.size > 1) width / (filteredVolumes.size - 1) else width
                    
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
                    
                    // Draw line connecting points
                    val path = Path()
                    filteredVolumes.forEachIndexed { index, weekVolume ->
                        val x = index * pointSpacing
                        val normalizedY = if (range > 0) (weekVolume.volume - minVolume) / range else 0.5f
                        val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                        
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
                    filteredVolumes.forEachIndexed { index, weekVolume ->
                        val x = index * pointSpacing
                        val normalizedY = if (range > 0) (weekVolume.volume - minVolume) / range else 0.5f
                        val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                        
                        drawCircle(
                            color = lineColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            // X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 49.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())
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
 * Format volume label for Y-axis (e.g., 10K, 50K, 100K).
 */
private fun formatVolumeLabel(volume: Float): String {
    return when {
        volume >= 1_000_000 -> String.format("%.1fM", volume / 1_000_000)
        volume >= 1_000 -> String.format("%.0fK", volume / 1_000)
        else -> String.format("%.0f", volume)
    }
}

/**
 * Compact time range selector using filter chips for Progress screen.
 */
@Composable
private fun TimeRangeSelectorProgress(
    selectedRange: VolumeChartTimeRange,
    onRangeSelected: (VolumeChartTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        VolumeChartTimeRange.entries.forEach { range ->
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