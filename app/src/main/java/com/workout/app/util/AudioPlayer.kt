package com.workout.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Audio player that plays sounds through the media stream and handles vibration.
 * Using the media stream ensures sounds play even when the phone is on
 * Do Not Disturb or vibrate mode.
 * Used for rest timer completion alerts.
 */
class AudioPlayer(private val context: Context) {
    
    private var soundUri: Uri? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val settingsManager = SettingsManager.getInstance(context)
    
    // Maximum duration for the sound (2.5 seconds)
    private val maxDurationMs = 2500L
    
    // Vibration pattern: wait 0ms, vibrate 200ms, wait 100ms, vibrate 200ms
    private val vibrationPattern = longArrayOf(0, 200, 100, 200)
    
    init {
        // Use notification sound - it's short and attention-grabbing
        // Falls back to ringtone if notification sound is not available
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
    
    /**
     * Plays a sound and/or vibrates based on user settings.
     * This bypasses Do Not Disturb and vibrate mode settings.
     * Used to alert the user when rest timer completes.
     */
    fun playNotificationSound() {
        if (settingsManager.getTimerSoundEnabledSync()) {
            playSound()
        }
        if (settingsManager.getTimerVibrationEnabledSync()) {
            vibrate()
        }
    }
    
    /**
     * Plays the alert sound through the media stream.
     */
    fun playAlertSound() {
        playNotificationSound()
    }
    
    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, -1)
            }
        } catch (e: Exception) {
            // Silently fail if vibration is not available
        }
    }
    
    private fun playSound() {
        // Release any existing player
        releaseMediaPlayer()
        
        soundUri?.let { uri ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = false // Ensure it doesn't loop
                    setOnCompletionListener { mp ->
                        handler.removeCallbacksAndMessages(null)
                        mp.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, _, _ ->
                        handler.removeCallbacksAndMessages(null)
                        mp.release()
                        mediaPlayer = null
                        true
                    }
                    prepare()
                    start()
                    
                    // Safety timeout - stop after max duration even if sound is longer
                    handler.postDelayed({
                        releaseMediaPlayer()
                    }, maxDurationMs)
                }
            } catch (e: Exception) {
                // Silently fail if sound cannot be played
                // This shouldn't break the workout flow
                releaseMediaPlayer()
            }
        }
    }
    
    private fun releaseMediaPlayer() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
        mediaPlayer = null
    }
    
    /**
     * Clean up resources when no longer needed.
     */
    fun release() {
        releaseMediaPlayer()
    }
}
