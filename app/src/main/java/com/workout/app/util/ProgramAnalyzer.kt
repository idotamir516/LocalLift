package com.workout.app.util

import com.workout.app.data.Exercise
import com.workout.app.data.ExerciseLibrary
import com.workout.app.data.dao.FullTemplateWithExercisesAndSets
import com.workout.app.data.entities.CustomExercise
import com.workout.app.data.entities.SetType

/**
 * Represents set counts broken down by type
 */
data class SetCountsByType(
    val warmup: Int = 0,
    val regular: Int = 0,
    val dropSet: Int = 0
) {
    val total: Int get() = warmup + regular + dropSet
    
    /**
     * Get the count of sets that count as "effective" based on settings.
     * Regular sets always count. Warmup and drop sets are configurable.
     */
    fun getEffectiveCount(
        countWarmupAsEffective: Boolean = false,
        countDropSetAsEffective: Boolean = true
    ): Int {
        var count = regular
        if (countWarmupAsEffective) count += warmup
        if (countDropSetAsEffective) count += dropSet
        return count
    }
    
    operator fun plus(other: SetCountsByType): SetCountsByType = SetCountsByType(
        warmup = warmup + other.warmup,
        regular = regular + other.regular,
        dropSet = dropSet + other.dropSet
    )
}

/**
 * Represents set counts for a muscle group, separated by primary and auxiliary targeting
 */
data class MuscleSetAnalysis(
    val muscleName: String,
    val primarySets: SetCountsByType = SetCountsByType(),
    val auxiliarySets: SetCountsByType = SetCountsByType()
) {
    val totalPrimarySets: Int get() = primarySets.total
    val totalAuxiliarySets: Int get() = auxiliarySets.total
    val totalSets: Int get() = totalPrimarySets + totalAuxiliarySets
    
    /**
     * Calculate effective sets based on settings.
     * Primary sets count as 1, auxiliary sets count as 0.5 (common heuristic).
     * Warmup and drop sets can be optionally excluded from effective count.
     */
    fun getEffectiveSets(
        countWarmupAsEffective: Boolean = false,
        countDropSetAsEffective: Boolean = true
    ): Float {
        val primaryEffective = primarySets.getEffectiveCount(countWarmupAsEffective, countDropSetAsEffective)
        val auxiliaryEffective = auxiliarySets.getEffectiveCount(countWarmupAsEffective, countDropSetAsEffective)
        return primaryEffective + (auxiliaryEffective * 0.5f)
    }
    
    // Legacy property for backward compatibility (excludes warmup, includes drop sets)
    val effectiveSets: Float get() = getEffectiveSets()
    
    operator fun plus(other: MuscleSetAnalysis): MuscleSetAnalysis {
        require(muscleName == other.muscleName) { "Cannot combine different muscles" }
        return MuscleSetAnalysis(
            muscleName = muscleName,
            primarySets = primarySets + other.primarySets,
            auxiliarySets = auxiliarySets + other.auxiliarySets
        )
    }
}

/**
 * Full program analysis result
 */
data class ProgramAnalysis(
    val templateNames: List<String>,
    val muscleAnalysis: Map<String, MuscleSetAnalysis>,
    val totalExercises: Int,
    val totalSets: Int,
    val templateTimeAnalysis: List<TemplateTimeAnalysis> = emptyList()
) {
    // Get analysis sorted by total sets (descending)
    val sortedByTotalSets: List<MuscleSetAnalysis>
        get() = muscleAnalysis.values.sortedByDescending { it.totalSets }
    
    // Get analysis sorted by effective sets (descending) with settings
    fun sortedByEffectiveSets(
        countWarmupAsEffective: Boolean = false,
        countDropSetAsEffective: Boolean = true
    ): List<MuscleSetAnalysis> {
        return muscleAnalysis.values.sortedByDescending { 
            it.getEffectiveSets(countWarmupAsEffective, countDropSetAsEffective) 
        }
    }
    
    // Legacy property for backward compatibility
    val sortedByEffectiveSets: List<MuscleSetAnalysis>
        get() = sortedByEffectiveSets()
    
    // Get analysis sorted by muscle name (alphabetically)
    val sortedByName: List<MuscleSetAnalysis>
        get() = muscleAnalysis.values.sortedBy { it.muscleName }
}

/**
 * Time analysis for a single template
 */
data class TemplateTimeAnalysis(
    val templateName: String,
    val totalSets: Int,
    val totalRestTimeSeconds: Int,
    val exerciseCount: Int
) {
    /**
     * Calculate estimated total workout time
     * @param secondsPerSet Estimated time to perform each set
     * @return Total estimated time in seconds
     */
    fun getEstimatedTotalTimeSeconds(secondsPerSet: Int): Int {
        return (totalSets * secondsPerSet) + totalRestTimeSeconds
    }
    
    /**
     * Format time as human-readable string (e.g., "45m" or "1h 15m")
     */
    fun formatTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}

/**
 * Analyzes workout templates to calculate volume per muscle group
 */
object ProgramAnalyzer {
    
    /**
     * Analyze multiple templates to get set counts per muscle group
     */
    fun analyze(
        templates: List<FullTemplateWithExercisesAndSets>,
        customExercises: List<CustomExercise> = emptyList()
    ): ProgramAnalysis {
        val muscleMap = mutableMapOf<String, MuscleSetAnalysis>()
        var totalExercises = 0
        var totalSets = 0
        val templateTimeAnalysisList = mutableListOf<TemplateTimeAnalysis>()
        
        // Build custom exercise lookup
        val customExerciseMap = customExercises.associateBy { it.name }
        
        for (template in templates) {
            // Track per-template stats
            var templateSetCount = 0
            var templateRestTimeSeconds = 0
            var templateExerciseCount = 0
            
            for (exerciseWithSets in template.exercises) {
                val exerciseName = exerciseWithSets.exercise.exerciseName
                totalExercises++
                templateExerciseCount++
                
                // Find exercise in library or custom exercises
                val exercise = ExerciseLibrary.exercises.find { it.name == exerciseName }
                val customExercise = customExerciseMap[exerciseName]
                
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
                    else -> {
                        // Unknown exercise - skip
                        continue
                    }
                }
                
                // Count sets by type
                for (set in exerciseWithSets.sets) {
                    totalSets++
                    templateSetCount++
                    
                    // Add rest time (use set-specific rest time or exercise default, fallback to 90s)
                    val restSeconds = set.restSeconds ?: exerciseWithSets.exercise.restSeconds ?: 90
                    templateRestTimeSeconds += restSeconds
                    
                    // Add to primary muscle
                    val currentPrimaryAnalysis = muscleMap.getOrPut(primaryMuscle) {
                        MuscleSetAnalysis(primaryMuscle)
                    }
                    muscleMap[primaryMuscle] = currentPrimaryAnalysis.copy(
                        primarySets = currentPrimaryAnalysis.primarySets.addSet(set.setType)
                    )
                    
                    // Add to auxiliary muscles
                    for (auxMuscle in auxiliaryMuscles) {
                        val currentAuxAnalysis = muscleMap.getOrPut(auxMuscle) {
                            MuscleSetAnalysis(auxMuscle)
                        }
                        muscleMap[auxMuscle] = currentAuxAnalysis.copy(
                            auxiliarySets = currentAuxAnalysis.auxiliarySets.addSet(set.setType)
                        )
                    }
                }
            }
            
            // Add template time analysis
            templateTimeAnalysisList.add(
                TemplateTimeAnalysis(
                    templateName = template.template.name,
                    totalSets = templateSetCount,
                    totalRestTimeSeconds = templateRestTimeSeconds,
                    exerciseCount = templateExerciseCount
                )
            )
        }
        
        return ProgramAnalysis(
            templateNames = templates.map { it.template.name },
            muscleAnalysis = muscleMap,
            totalExercises = totalExercises,
            totalSets = totalSets,
            templateTimeAnalysis = templateTimeAnalysisList
        )
    }
    
    private fun SetCountsByType.addSet(setType: SetType): SetCountsByType {
        return when (setType) {
            SetType.WARMUP -> copy(warmup = warmup + 1)
            SetType.REGULAR -> copy(regular = regular + 1)
            SetType.DROP_SET -> copy(dropSet = dropSet + 1)
        }
    }
}
