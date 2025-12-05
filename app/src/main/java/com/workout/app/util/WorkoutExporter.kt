package com.workout.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.workout.app.data.ExerciseLibrary
import com.workout.app.data.dao.SessionWithDetails
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting workout data to CSV format.
 * Exports denormalized data with one row per set for easy analysis.
 */
object WorkoutExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Generate a filename for the export.
     */
    fun generateFileName(): String {
        return "workout_export_${fileNameDateFormat.format(Date())}.csv"
    }
    
    /**
     * Generate CSV content from workout sessions.
     * Format: date,workout_name,exercise,primary_muscle,auxiliary_muscles,set_number,weight,reps,rpe,set_type
     */
    fun generateCsv(sessions: List<SessionWithDetails>): String {
        val sb = StringBuilder()
        
        // Header row
        sb.appendLine("date,workout_name,exercise,primary_muscle,auxiliary_muscles,set_number,weight,reps,rpe,set_type")
        
        // Data rows - one per set
        for (sessionWithDetails in sessions) {
            val session = sessionWithDetails.session
            val workoutDate = dateFormat.format(Date(session.startedAt))
            val workoutName = escapeCSV(session.templateName ?: "Quick Workout")
            
            for (exerciseWithSets in sessionWithDetails.exercises) {
                val exerciseLog = exerciseWithSets.exerciseLog
                val exerciseName = escapeCSV(exerciseLog.exerciseName)
                
                // Look up exercise in library to get muscle data
                val exercise = ExerciseLibrary.getByName(exerciseLog.exerciseName)
                val primaryMuscle = exercise?.primaryMuscle ?: "Unknown"
                val auxiliaryMuscles = exercise?.auxiliaryMuscles?.joinToString("|") ?: ""
                
                for (setLog in exerciseWithSets.sets) {
                    val setNumber = setLog.setNumber
                    val weight = setLog.weightLbs?.toString() ?: ""
                    val reps = setLog.reps?.toString() ?: ""
                    val rpe = setLog.rpe?.toString() ?: ""
                    val setType = setLog.setType.name
                    
                    sb.appendLine("$workoutDate,$workoutName,$exerciseName,$primaryMuscle,$auxiliaryMuscles,$setNumber,$weight,$reps,$rpe,$setType")
                }
            }
        }
        
        return sb.toString()
    }
    
    /**
     * Create a temporary CSV file and return it.
     */
    fun createCsvFile(context: Context, csvContent: String): File {
        val fileName = "workout_export_${fileNameDateFormat.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csvContent)
        return file
    }
    
    /**
     * Create an intent to share the CSV file.
     */
    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Workout Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Escape a string for CSV format.
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
