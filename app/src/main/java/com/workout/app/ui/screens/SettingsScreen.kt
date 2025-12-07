package com.workout.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.util.PreviousLiftSource
import com.workout.app.util.SettingsManager

/**
 * Settings screen for configuring app defaults and preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // Collect settings state
    val defaultRestSeconds by settingsManager.defaultRestSeconds.collectAsState()
    val defaultSets by settingsManager.defaultSetsPerExercise.collectAsState()
    val showRpeByDefault by settingsManager.showRpeByDefault.collectAsState()
    val timerSoundEnabled by settingsManager.timerSoundEnabled.collectAsState()
    val timerVibrationEnabled by settingsManager.timerVibrationEnabled.collectAsState()
    val previousLiftSource by settingsManager.previousLiftSource.collectAsState()
    val countWarmupAsEffective by settingsManager.countWarmupAsEffective.collectAsState()
    val countDropSetAsEffective by settingsManager.countDropSetAsEffective.collectAsState()
    val estimatedSecondsPerSet by settingsManager.estimatedSecondsPerSet.collectAsState()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Settings",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Defaults Section
            SettingsSection(title = "Exercise Defaults") {
                // Default Rest Time
                NumberSettingRow(
                    label = "Default Rest Time",
                    value = defaultRestSeconds,
                    valueLabel = formatRestTime(defaultRestSeconds),
                    onDecrease = { 
                        if (defaultRestSeconds >= 15) {
                            settingsManager.setDefaultRestSeconds(defaultRestSeconds - 15)
                        }
                    },
                    onIncrease = { 
                        if (defaultRestSeconds <= 585) {
                            settingsManager.setDefaultRestSeconds(defaultRestSeconds + 15)
                        }
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Default Sets per Exercise
                NumberSettingRow(
                    label = "Default Sets",
                    value = defaultSets,
                    valueLabel = "$defaultSets sets",
                    onDecrease = { 
                        if (defaultSets > 1) {
                            settingsManager.setDefaultSetsPerExercise(defaultSets - 1)
                        }
                    },
                    onIncrease = { 
                        if (defaultSets < 10) {
                            settingsManager.setDefaultSetsPerExercise(defaultSets + 1)
                        }
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Show RPE by Default
                SwitchSettingRow(
                    label = "Show RPE by Default",
                    description = "Display RPE input for new exercises",
                    checked = showRpeByDefault,
                    onCheckedChange = { settingsManager.setShowRpeByDefault(it) }
                )
            }
            
            // Timer Section
            SettingsSection(title = "Rest Timer") {
                // Timer Sound
                SwitchSettingRow(
                    label = "Timer Sound",
                    description = "Play sound when rest timer completes",
                    checked = timerSoundEnabled,
                    onCheckedChange = { settingsManager.setTimerSoundEnabled(it) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Timer Vibration
                SwitchSettingRow(
                    label = "Timer Vibration",
                    description = "Vibrate when rest timer completes",
                    checked = timerVibrationEnabled,
                    onCheckedChange = { settingsManager.setTimerVibrationEnabled(it) }
                )
            }
            
            // Workout Display Section
            SettingsSection(title = "Workout Display") {
                // Previous Lift Source
                SelectionSettingRow(
                    label = "Previous Lift Data",
                    description = "How to calculate 'Previous' column during workouts",
                    options = listOf(
                        PreviousLiftSource.BY_TEMPLATE to "Same Template",
                        PreviousLiftSource.BY_EXERCISE to "Any Workout"
                    ),
                    selectedOption = previousLiftSource,
                    onOptionSelected = { settingsManager.setPreviousLiftSource(it) }
                )
            }
            
            // Program Analyzer Section
            SettingsSection(title = "Program Analyzer") {
                // Count Warmup Sets as Effective
                SwitchSettingRow(
                    label = "Warmup Sets Count as Effective",
                    description = "Include warmup sets in effective set calculations",
                    checked = countWarmupAsEffective,
                    onCheckedChange = { settingsManager.setCountWarmupAsEffective(it) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Count Drop Sets as Effective
                SwitchSettingRow(
                    label = "Drop Sets Count as Effective",
                    description = "Include drop sets in effective set calculations",
                    checked = countDropSetAsEffective,
                    onCheckedChange = { settingsManager.setCountDropSetAsEffective(it) }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Estimated Time Per Set
                SelectionSettingRow(
                    label = "Time Per Set",
                    description = "Estimated time to perform each set (for workout time calculation)",
                    options = listOf(
                        30 to "30s",
                        45 to "45s",
                        60 to "1m",
                        75 to "1m 15s",
                        90 to "1m 30s"
                    ),
                    selectedOption = estimatedSecondsPerSet,
                    onOptionSelected = { settingsManager.setEstimatedSecondsPerSet(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun NumberSettingRow(
    label: String,
    value: Int,
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Decrease button
            Surface(
                onClick = onDecrease,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Value display
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(80.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Increase button
            Surface(
                onClick = onIncrease,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun <T> SelectionSettingRow(
    label: String,
    description: String? = null,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (option, optionLabel) ->
                val isSelected = option == selectedOption
                Surface(
                    onClick = { onOptionSelected(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = optionLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

private fun formatRestTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        if (secs > 0) "${minutes}m ${secs}s" else "${minutes}m"
    } else {
        "${secs}s"
    }
}
