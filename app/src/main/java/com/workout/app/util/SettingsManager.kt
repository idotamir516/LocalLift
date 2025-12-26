package com.workout.app.util

import android.content.Context
import android.content.SharedPreferences
import com.workout.app.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Determines how "previous lift" data is calculated for sets during workouts.
 */
enum class PreviousLiftSource {
    /** Match based on the last time this template was used */
    BY_TEMPLATE,
    /** Match based on the last time this exercise was performed (any template) */
    BY_EXERCISE
}

/**
 * Manages app settings using SharedPreferences.
 * Provides reactive access to settings via StateFlows.
 */
class SettingsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    // Settings state flows
    private val _defaultRestSeconds = MutableStateFlow(prefs.getInt(KEY_DEFAULT_REST_SECONDS, DEFAULT_REST_SECONDS))
    val defaultRestSeconds: StateFlow<Int> = _defaultRestSeconds.asStateFlow()
    
    private val _defaultSetsPerExercise = MutableStateFlow(prefs.getInt(KEY_DEFAULT_SETS, DEFAULT_SETS))
    val defaultSetsPerExercise: StateFlow<Int> = _defaultSetsPerExercise.asStateFlow()
    
    private val _showRpeByDefault = MutableStateFlow(prefs.getBoolean(KEY_SHOW_RPE_DEFAULT, DEFAULT_SHOW_RPE))
    val showRpeByDefault: StateFlow<Boolean> = _showRpeByDefault.asStateFlow()
    
    private val _timerSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_TIMER_SOUND_ENABLED, DEFAULT_TIMER_SOUND))
    val timerSoundEnabled: StateFlow<Boolean> = _timerSoundEnabled.asStateFlow()
    
    private val _timerVibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_TIMER_VIBRATION_ENABLED, DEFAULT_TIMER_VIBRATION))
    val timerVibrationEnabled: StateFlow<Boolean> = _timerVibrationEnabled.asStateFlow()
    
    private val _previousLiftSource = MutableStateFlow(
        PreviousLiftSource.valueOf(prefs.getString(KEY_PREVIOUS_LIFT_SOURCE, DEFAULT_PREVIOUS_LIFT_SOURCE.name) ?: DEFAULT_PREVIOUS_LIFT_SOURCE.name)
    )
    val previousLiftSource: StateFlow<PreviousLiftSource> = _previousLiftSource.asStateFlow()
    
    private val _countWarmupAsEffective = MutableStateFlow(prefs.getBoolean(KEY_COUNT_WARMUP_EFFECTIVE, DEFAULT_COUNT_WARMUP_EFFECTIVE))
    val countWarmupAsEffective: StateFlow<Boolean> = _countWarmupAsEffective.asStateFlow()
    
    private val _countDropSetAsEffective = MutableStateFlow(prefs.getBoolean(KEY_COUNT_DROPSET_EFFECTIVE, DEFAULT_COUNT_DROPSET_EFFECTIVE))
    val countDropSetAsEffective: StateFlow<Boolean> = _countDropSetAsEffective.asStateFlow()
    
    private val _estimatedSecondsPerSet = MutableStateFlow(prefs.getInt(KEY_ESTIMATED_SECONDS_PER_SET, DEFAULT_ESTIMATED_SECONDS_PER_SET))
    val estimatedSecondsPerSet: StateFlow<Int> = _estimatedSecondsPerSet.asStateFlow()
    
    private val _timerStartsMinimized = MutableStateFlow(prefs.getBoolean(KEY_TIMER_STARTS_MINIMIZED, DEFAULT_TIMER_STARTS_MINIMIZED))
    val timerStartsMinimized: StateFlow<Boolean> = _timerStartsMinimized.asStateFlow()
    
    private val _appTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString(KEY_APP_THEME, DEFAULT_APP_THEME.name) ?: DEFAULT_APP_THEME.name)
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()
    
    private val _vibrantColors = MutableStateFlow(prefs.getBoolean(KEY_VIBRANT_COLORS, DEFAULT_VIBRANT_COLORS))
    val vibrantColors: StateFlow<Boolean> = _vibrantColors.asStateFlow()
    
    // Setters
    fun setDefaultRestSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_DEFAULT_REST_SECONDS, seconds).apply()
        _defaultRestSeconds.value = seconds
    }
    
    fun setDefaultSetsPerExercise(sets: Int) {
        prefs.edit().putInt(KEY_DEFAULT_SETS, sets).apply()
        _defaultSetsPerExercise.value = sets
    }
    
    fun setShowRpeByDefault(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RPE_DEFAULT, show).apply()
        _showRpeByDefault.value = show
    }
    
    fun setTimerSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_SOUND_ENABLED, enabled).apply()
        _timerSoundEnabled.value = enabled
    }
    
    fun setTimerVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_VIBRATION_ENABLED, enabled).apply()
        _timerVibrationEnabled.value = enabled
    }
    
    fun setPreviousLiftSource(source: PreviousLiftSource) {
        prefs.edit().putString(KEY_PREVIOUS_LIFT_SOURCE, source.name).apply()
        _previousLiftSource.value = source
    }
    
    fun setCountWarmupAsEffective(count: Boolean) {
        prefs.edit().putBoolean(KEY_COUNT_WARMUP_EFFECTIVE, count).apply()
        _countWarmupAsEffective.value = count
    }
    
    fun setCountDropSetAsEffective(count: Boolean) {
        prefs.edit().putBoolean(KEY_COUNT_DROPSET_EFFECTIVE, count).apply()
        _countDropSetAsEffective.value = count
    }
    
    fun setEstimatedSecondsPerSet(seconds: Int) {
        prefs.edit().putInt(KEY_ESTIMATED_SECONDS_PER_SET, seconds).apply()
        _estimatedSecondsPerSet.value = seconds
    }
    
    fun setTimerStartsMinimized(minimized: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_STARTS_MINIMIZED, minimized).apply()
        _timerStartsMinimized.value = minimized
    }
    
    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
        _appTheme.value = theme
    }
    
    fun setVibrantColors(vibrant: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRANT_COLORS, vibrant).apply()
        _vibrantColors.value = vibrant
    }
    
    // Synchronous getters for non-reactive use
    fun getDefaultRestSecondsSync(): Int = prefs.getInt(KEY_DEFAULT_REST_SECONDS, DEFAULT_REST_SECONDS)
    fun getDefaultSetsPerExerciseSync(): Int = prefs.getInt(KEY_DEFAULT_SETS, DEFAULT_SETS)
    fun getShowRpeByDefaultSync(): Boolean = prefs.getBoolean(KEY_SHOW_RPE_DEFAULT, DEFAULT_SHOW_RPE)
    fun getTimerSoundEnabledSync(): Boolean = prefs.getBoolean(KEY_TIMER_SOUND_ENABLED, DEFAULT_TIMER_SOUND)
    fun getTimerVibrationEnabledSync(): Boolean = prefs.getBoolean(KEY_TIMER_VIBRATION_ENABLED, DEFAULT_TIMER_VIBRATION)
    fun getPreviousLiftSourceSync(): PreviousLiftSource = PreviousLiftSource.valueOf(
        prefs.getString(KEY_PREVIOUS_LIFT_SOURCE, DEFAULT_PREVIOUS_LIFT_SOURCE.name) ?: DEFAULT_PREVIOUS_LIFT_SOURCE.name
    )
    fun getCountWarmupAsEffectiveSync(): Boolean = prefs.getBoolean(KEY_COUNT_WARMUP_EFFECTIVE, DEFAULT_COUNT_WARMUP_EFFECTIVE)
    fun getCountDropSetAsEffectiveSync(): Boolean = prefs.getBoolean(KEY_COUNT_DROPSET_EFFECTIVE, DEFAULT_COUNT_DROPSET_EFFECTIVE)
    fun getEstimatedSecondsPerSetSync(): Int = prefs.getInt(KEY_ESTIMATED_SECONDS_PER_SET, DEFAULT_ESTIMATED_SECONDS_PER_SET)
    fun getTimerStartsMinimizedSync(): Boolean = prefs.getBoolean(KEY_TIMER_STARTS_MINIMIZED, DEFAULT_TIMER_STARTS_MINIMIZED)
    fun getAppThemeSync(): AppTheme = AppTheme.valueOf(
        prefs.getString(KEY_APP_THEME, DEFAULT_APP_THEME.name) ?: DEFAULT_APP_THEME.name
    )
    fun getVibrantColorsSync(): Boolean = prefs.getBoolean(KEY_VIBRANT_COLORS, DEFAULT_VIBRANT_COLORS)
    
    companion object {
        private const val PREFS_NAME = "workout_app_settings"
        
        // Keys
        private const val KEY_DEFAULT_REST_SECONDS = "default_rest_seconds"
        private const val KEY_DEFAULT_SETS = "default_sets_per_exercise"
        private const val KEY_SHOW_RPE_DEFAULT = "show_rpe_by_default"
        private const val KEY_TIMER_SOUND_ENABLED = "timer_sound_enabled"
        private const val KEY_TIMER_VIBRATION_ENABLED = "timer_vibration_enabled"
        private const val KEY_PREVIOUS_LIFT_SOURCE = "previous_lift_source"
        private const val KEY_COUNT_WARMUP_EFFECTIVE = "count_warmup_effective"
        private const val KEY_COUNT_DROPSET_EFFECTIVE = "count_dropset_effective"
        private const val KEY_ESTIMATED_SECONDS_PER_SET = "estimated_seconds_per_set"
        private const val KEY_TIMER_STARTS_MINIMIZED = "timer_starts_minimized"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_VIBRANT_COLORS = "vibrant_colors"
        
        // Defaults
        const val DEFAULT_REST_SECONDS = 90
        const val DEFAULT_SETS = 3
        const val DEFAULT_SHOW_RPE = false
        const val DEFAULT_TIMER_SOUND = true
        const val DEFAULT_TIMER_VIBRATION = true
        val DEFAULT_PREVIOUS_LIFT_SOURCE = PreviousLiftSource.BY_TEMPLATE
        const val DEFAULT_COUNT_WARMUP_EFFECTIVE = false
        const val DEFAULT_COUNT_DROPSET_EFFECTIVE = true
        const val DEFAULT_ESTIMATED_SECONDS_PER_SET = 45
        const val DEFAULT_TIMER_STARTS_MINIMIZED = false
        val DEFAULT_APP_THEME = AppTheme.CYAN
        const val DEFAULT_VIBRANT_COLORS = true
        
        @Volatile
        private var instance: SettingsManager? = null
        
        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
