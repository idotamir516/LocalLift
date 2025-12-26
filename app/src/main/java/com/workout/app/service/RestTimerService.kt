package com.workout.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.workout.app.MainActivity
import com.workout.app.R
import com.workout.app.util.AudioPlayer
import com.workout.app.util.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that runs the rest timer.
 * This allows the timer to continue running and send notifications
 * even when the app is in the background.
 */
class RestTimerService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    private var endTimeMillis: Long = 0
    private var totalSeconds: Int = 0
    
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var notificationManager: NotificationManager
    
    inner class LocalBinder : Binder() {
        fun getService(): RestTimerService = this@RestTimerService
    }
    
    override fun onCreate() {
        super.onCreate()
        audioPlayer = AudioPlayer(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val endTime = intent.getLongExtra(EXTRA_END_TIME_MILLIS, 0L)
                val total = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 0)
                if (endTime > 0 && total > 0) {
                    startTimer(endTime, total)
                }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_SKIP -> cancelTimer()
            ACTION_ADD_TIME -> {
                val seconds = intent.getIntExtra(EXTRA_ADJUST_SECONDS, 0)
                addTime(seconds)
            }
            ACTION_SUBTRACT_TIME -> {
                val seconds = intent.getIntExtra(EXTRA_ADJUST_SECONDS, 0)
                subtractTime(seconds)
            }
        }
        return START_NOT_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rest Timer",
                NotificationManager.IMPORTANCE_LOW // Low importance = no sound from notification itself
            ).apply {
                description = "Shows rest timer countdown"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
            
            // Create a separate channel for timer completion with high importance
            val completionChannel = NotificationChannel(
                CHANNEL_ID_COMPLETION,
                "Timer Complete",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when rest timer completes"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(completionChannel)
        }
    }
    
    private fun buildNotification(remainingSeconds: Int): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = "%d:%02d".format(minutes, seconds)
        
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Timer")
            .setContentText("$timeText remaining")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setSilent(true)
            .build()
    }
    
    private fun startTimer(endTime: Long, total: Int) {
        timerJob?.cancel()
        
        totalSeconds = total
        endTimeMillis = endTime
        
        _timerState.value = TimerState(
            isRunning = true,
            remainingSeconds = total,
            totalSeconds = total,
            endTimeMillis = endTime
        )
        
        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification(total))
        
        // Broadcast initial state
        broadcastTimerUpdate()
        
        timerJob = serviceScope.launch {
            while (true) {
                val remainingMillis = endTimeMillis - System.currentTimeMillis()
                val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)
                
                _timerState.value = _timerState.value.copy(
                    remainingSeconds = remainingSeconds
                )
                
                // Broadcast timer update to UI
                broadcastTimerUpdate()
                
                // Update notification
                notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
                
                if (remainingSeconds <= 0) {
                    onTimerComplete()
                    break
                }
                
                delay(500) // Update twice per second for smoother countdown
            }
        }
    }
    
    private fun pauseTimer() {
        timerJob?.cancel()
        // Store remaining time when paused
        val remainingMillis = endTimeMillis - System.currentTimeMillis()
        val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)
        
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            remainingSeconds = remainingSeconds
        )
        
        broadcastTimerUpdate()
        
        // Update notification to show paused state
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Timer (Paused)")
            .setContentText("${_timerState.value.formattedTime} remaining")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun resumeTimer() {
        if (_timerState.value.remainingSeconds > 0 && !_timerState.value.isRunning) {
            val remaining = _timerState.value.remainingSeconds
            endTimeMillis = System.currentTimeMillis() + (remaining * 1000L)
            
            _timerState.value = _timerState.value.copy(isRunning = true)
            
            broadcastTimerUpdate()
            
            timerJob = serviceScope.launch {
                while (true) {
                    val remainingMillis = endTimeMillis - System.currentTimeMillis()
                    val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)
                    
                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = remainingSeconds
                    )
                    
                    broadcastTimerUpdate()
                    
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
                    
                    if (remainingSeconds <= 0) {
                        onTimerComplete()
                        break
                    }
                    
                    delay(500)
                }
            }
        }
    }
    
    private fun cancelTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState()
        broadcastTimerUpdate()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun addTime(seconds: Int) {
        if (_timerState.value.totalSeconds > 0) {
            endTimeMillis += seconds * 1000L
            totalSeconds += seconds
            
            val remainingMillis = endTimeMillis - System.currentTimeMillis()
            val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)
            
            _timerState.value = _timerState.value.copy(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds
            )
            
            broadcastTimerUpdate()
            
            if (_timerState.value.isRunning) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
            }
        }
    }
    
    fun subtractTime(seconds: Int) {
        if (_timerState.value.totalSeconds > 0) {
            endTimeMillis -= seconds * 1000L
            totalSeconds = (totalSeconds - seconds).coerceAtLeast(1)
            
            val remainingMillis = endTimeMillis - System.currentTimeMillis()
            val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)
            
            _timerState.value = _timerState.value.copy(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds
            )
            
            broadcastTimerUpdate()
            
            if (remainingSeconds <= 0) {
                onTimerComplete()
            } else if (_timerState.value.isRunning) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
            }
        }
    }
    
    private fun broadcastTimerUpdate(isComplete: Boolean = false) {
        val intent = Intent(TIMER_UPDATE_ACTION).apply {
            putExtra(EXTRA_REMAINING_SECONDS, _timerState.value.remainingSeconds)
            putExtra(EXTRA_TOTAL_SECONDS, _timerState.value.totalSeconds)
            putExtra(EXTRA_IS_RUNNING, _timerState.value.isRunning)
            putExtra(EXTRA_IS_COMPLETE, isComplete)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun onTimerComplete() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            remainingSeconds = 0
        )
        
        // Broadcast completion
        broadcastTimerUpdate(isComplete = true)
        
        // Play sound and vibrate
        audioPlayer.playNotificationSound()
        
        // Show completion notification briefly
        val completionNotification = NotificationCompat.Builder(this, CHANNEL_ID_COMPLETION)
            .setContentTitle("Rest Complete!")
            .setContentText("Time to start your next set")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(NOTIFICATION_ID_COMPLETION, completionNotification)
        
        // Stop the foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
    
    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val CHANNEL_ID_COMPLETION = "rest_timer_completion_channel"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_ID_COMPLETION = 1002
        
        // Actions
        const val ACTION_START = "com.workout.app.action.START_TIMER"
        const val ACTION_PAUSE = "com.workout.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "com.workout.app.action.RESUME_TIMER"
        const val ACTION_SKIP = "com.workout.app.action.SKIP_TIMER"
        const val ACTION_ADD_TIME = "com.workout.app.action.ADD_TIME"
        const val ACTION_SUBTRACT_TIME = "com.workout.app.action.SUBTRACT_TIME"
        
        // Extras for starting timer
        const val EXTRA_END_TIME_MILLIS = "extra_end_time_millis"
        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val EXTRA_ADJUST_SECONDS = "extra_adjust_seconds"
        
        // Broadcast action and extras for UI updates
        const val TIMER_UPDATE_ACTION = "com.workout.app.TIMER_UPDATE"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_IS_RUNNING = "extra_is_running"
        const val EXTRA_IS_COMPLETE = "extra_is_complete"
    }
}
