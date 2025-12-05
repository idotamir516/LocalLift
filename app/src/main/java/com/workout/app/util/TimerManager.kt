package com.workout.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State representing the current timer status.
 */
data class TimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            remainingSeconds.toFloat() / totalSeconds.toFloat()
        } else {
            0f
        }
    
    val isComplete: Boolean
        get() = remainingSeconds <= 0 && totalSeconds > 0
    
    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

/**
 * Manages a countdown timer for rest periods between sets.
 * Exposes state as a Flow for reactive UI updates.
 */
class TimerManager(
    private val scope: CoroutineScope,
    private val onComplete: (() -> Unit)? = null
) {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    
    /**
     * Starts a countdown timer for the specified duration.
     * If a timer is already running, it will be cancelled and replaced.
     * 
     * @param seconds The number of seconds to count down from
     */
    fun start(seconds: Int) {
        // Cancel any existing timer
        timerJob?.cancel()
        
        if (seconds <= 0) return
        
        _timerState.value = TimerState(
            isRunning = true,
            remainingSeconds = seconds,
            totalSeconds = seconds
        )
        
        timerJob = scope.launch {
            while (_timerState.value.remainingSeconds > 0) {
                delay(1000)
                _timerState.value = _timerState.value.copy(
                    remainingSeconds = _timerState.value.remainingSeconds - 1
                )
            }
            
            // Timer complete
            _timerState.value = _timerState.value.copy(isRunning = false)
            onComplete?.invoke()
        }
    }
    
    /**
     * Pauses the current timer.
     */
    fun pause() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }
    
    /**
     * Resumes a paused timer.
     */
    fun resume() {
        if (_timerState.value.remainingSeconds > 0 && !_timerState.value.isRunning) {
            val remaining = _timerState.value.remainingSeconds
            timerJob = scope.launch {
                _timerState.value = _timerState.value.copy(isRunning = true)
                while (_timerState.value.remainingSeconds > 0) {
                    delay(1000)
                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = _timerState.value.remainingSeconds - 1
                    )
                }
                _timerState.value = _timerState.value.copy(isRunning = false)
                onComplete?.invoke()
            }
        }
    }
    
    /**
     * Cancels and resets the timer.
     */
    fun cancel() {
        timerJob?.cancel()
        _timerState.value = TimerState()
    }
    
    /**
     * Adds time to the current timer.
     * 
     * @param seconds Number of seconds to add
     */
    fun addTime(seconds: Int) {
        if (_timerState.value.totalSeconds > 0) {
            _timerState.value = _timerState.value.copy(
                remainingSeconds = _timerState.value.remainingSeconds + seconds,
                totalSeconds = _timerState.value.totalSeconds + seconds
            )
        }
    }
    
    /**
     * Subtracts time from the current timer.
     * Will not go below 0 seconds.
     * 
     * @param seconds Number of seconds to subtract
     */
    fun subtractTime(seconds: Int) {
        if (_timerState.value.totalSeconds > 0) {
            val newRemaining = (_timerState.value.remainingSeconds - seconds).coerceAtLeast(0)
            val newTotal = (_timerState.value.totalSeconds - seconds).coerceAtLeast(1)
            _timerState.value = _timerState.value.copy(
                remainingSeconds = newRemaining,
                totalSeconds = newTotal
            )
            // If we hit 0, complete the timer
            if (newRemaining <= 0) {
                timerJob?.cancel()
                _timerState.value = _timerState.value.copy(isRunning = false)
                onComplete?.invoke()
            }
        }
    }
    
    /**
     * Skips the rest of the timer.
     */
    fun skip() {
        timerJob?.cancel()
        _timerState.value = TimerState()
    }
}
