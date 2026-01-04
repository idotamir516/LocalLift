package com.workout.app.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.data.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Progress screen - Shows workout calendar and history with tabs.
 * 
 * @param completedSessions Flow of completed workout sessions
 * @param workoutDates Flow of timestamps when workouts were completed (for calendar)
 * @param onWorkoutClick Callback when a workout is clicked to view details
 * @param onShareClick Callback when share option is clicked
 * @param onSaveToFileClick Callback when save to file option is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    completedSessions: Flow<List<WorkoutSession>> = flowOf(emptyList()),
    workoutDates: Flow<List<Long>> = flowOf(emptyList()),
    onWorkoutClick: (sessionId: Long) -> Unit = {},
    onShareClick: () -> Unit = {},
    onSaveToFileClick: () -> Unit = {}
) {
    val sessions by completedSessions.collectAsState(initial = emptyList())
    val dates by workoutDates.collectAsState(initial = emptyList())
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
        TabItem("History", Icons.Outlined.History)
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
