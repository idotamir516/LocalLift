package com.workout.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.workout.app.ui.navigation.AppNavigation
import com.workout.app.ui.theme.WorkoutTrackerTheme
import com.workout.app.util.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val settingsManager = SettingsManager.getInstance(applicationContext)
        
        setContent {
            val appTheme by settingsManager.appTheme.collectAsState()
            val vibrantColors by settingsManager.vibrantColors.collectAsState()
            
            WorkoutTrackerTheme(appTheme = appTheme, vibrantColors = vibrantColors) {
                AppNavigation()
            }
        }
    }
}
