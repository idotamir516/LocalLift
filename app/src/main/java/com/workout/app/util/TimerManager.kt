package com.workout.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.workout.app.service.RestTimerService
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
    val totalSeconds: Int = 0,
    val endTimeMillis: Long = 0L
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
 * Uses a foreground service to ensure timer continues when app is backgrounded.
 * Exposes state as a Flow for reactive UI updates.
 */
class TimerManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onComplete: (() -> Unit)? = null
) {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    private var isRegistered = false
    
    // BroadcastReceiver to listen for timer updates from the foreground service
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                RestTimerService.TIMER_UPDATE_ACTION -> {
                    val remaining = intent.getIntExtra(RestTimerService.EXTRA_REMAINING_SECONDS, 0)
                    val total = intent.getIntExtra(RestTimerService.EXTRA_TOTAL_SECONDS, 0)
                    val isRunning = intent.getBooleanExtra(RestTimerService.EXTRA_IS_RUNNING, false)
                    val isComplete = intent.getBooleanExtra(RestTimerService.EXTRA_IS_COMPLETE, false)
                    
                    _timerState.value = _timerState.value.copy(
                        isRunning = isRunning,
                        remainingSeconds = remaining,
                        totalSeconds = total
                    )
                    
                    if (isComplete) {
                        _timerState.value = TimerState()
                        onComplete?.invoke()
                    }
                }
            }
        }
    }
    
    init {
        registerReceiver()
    }
    
    private fun registerReceiver() {
        if (!isRegistered) {
            val filter = IntentFilter(RestTimerService.TIMER_UPDATE_ACTION)
            LocalBroadcastManager.getInstance(context).registerReceiver(timerReceiver, filter)
            isRegistered = true
        }
    }
    
    fun unregister() {
        if (isRegistered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(timerReceiver)
            isRegistered = false
        }
    }
    
    /**
     * Starts a countdown timer for the specified duration.
     * Uses a foreground service to ensure timer continues when app is backgrounded.
     * 
     * @param seconds The number of seconds to count down from
     */
    fun start(seconds: Int) {
        if (seconds <= 0) return
        
        val endTime = System.currentTimeMillis() + (seconds * 1000L)
        
        _timerState.value = TimerState(
            isRunning = true,
            remainingSeconds = seconds,
            totalSeconds = seconds,
            endTimeMillis = endTime
        )
        
        // Start the foreground service
        val intent = Intent(context, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_START
            putExtra(RestTimerService.EXTRA_END_TIME_MILLIS, endTime)
            putExtra(RestTimerService.EXTRA_TOTAL_SECONDS, seconds)
        }
        context.startForegroundService(intent)
    }
    
    /**
     * Pauses the current timer.
     */
    fun pause() {
        val intent = Intent(context, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_PAUSE
        }
        context.startService(intent)
        
        _timerState.value = _timerState.value.copy(isRunning = false)
    }
    
    /**
     * Resumes a paused timer.
     */
    fun resume() {
        if (_timerState.value.remainingSeconds > 0 && !_timerState.value.isRunning) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_RESUME
            }
            context.startService(intent)
            
            _timerState.value = _timerState.value.copy(isRunning = true)
        }
    }
    
    /**
     * Cancels and resets the timer.
     */
    fun cancel() {
        val intent = Intent(context, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_SKIP
        }
        context.startService(intent)
        
        _timerState.value = TimerState()
    }
    
    /**
     * Adds time to the current timer.
     * 
     * @param seconds Number of seconds to add
     */
    fun addTime(seconds: Int) {
        if (_timerState.value.totalSeconds > 0) {
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_ADD_TIME
                putExtra(RestTimerService.EXTRA_ADJUST_SECONDS, seconds)
            }
            context.startService(intent)
            
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
            val intent = Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_SUBTRACT_TIME
                putExtra(RestTimerService.EXTRA_ADJUST_SECONDS, seconds)
            }
            context.startService(intent)
            
            val newRemaining = (_timerState.value.remainingSeconds - seconds).coerceAtLeast(0)
            val newTotal = (_timerState.value.totalSeconds - seconds).coerceAtLeast(1)
            _timerState.value = _timerState.value.copy(
                remainingSeconds = newRemaining,
                totalSeconds = newTotal
            )
        }
    }
    
    /**
     * Skips the rest of the timer.
     */
    fun skip() {
        cancel()
    }
}
