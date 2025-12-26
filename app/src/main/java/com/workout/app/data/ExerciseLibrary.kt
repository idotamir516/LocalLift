package com.workout.app.data

/**
 * Represents an exercise in the static library.
 * @param name The display name of the exercise
 * @param primaryMuscle The primary muscle group targeted (used for categorization)
 * @param auxiliaryMuscles Additional muscle groups that are also worked
 * @param isBodyweight Whether the exercise uses bodyweight only
 */
data class Exercise(
    val name: String,
    val primaryMuscle: String,
    val auxiliaryMuscles: List<String> = emptyList(),
    val isBodyweight: Boolean = false
) {
    // For backwards compatibility and filtering by category
    val category: String get() = primaryMuscle
    
    // All muscles worked by this exercise (primary + auxiliary)
    val allMuscles: List<String> get() = listOf(primaryMuscle) + auxiliaryMuscles
}

/**
 * Static library of exercises organized by category.
 * Not stored in database - hardcoded for simplicity.
 */
object ExerciseLibrary {
    
    val categories = listOf(
        "Chest",
        "Back",
        "Lower Back",
        "Trapezius",
        "Shoulders",
        "Biceps",
        "Triceps",
        "Forearms",
        "Quads",
        "Hamstrings",
        "Glutes",
        "Calves",
        "Abductors",
        "Adductors",
        "Core",
        "Full Body",
        "Cardio"
    )
    
    val exercises: List<Exercise> = listOf(
        // ===== CHEST =====
        Exercise("Bench Press", "Chest", listOf("Triceps", "Shoulders")),
        Exercise("Incline Bench Press", "Chest", listOf("Shoulders", "Triceps")),
        Exercise("Decline Bench Press", "Chest", listOf("Triceps")),
        Exercise("Dumbbell Fly", "Chest"),
        Exercise("Incline Dumbbell Fly", "Chest", listOf("Shoulders")),
        Exercise("Push-ups", "Chest", listOf("Triceps", "Shoulders", "Core"), isBodyweight = true),
        Exercise("Cable Crossover", "Chest"),
        Exercise("Dips (Chest)", "Chest", listOf("Triceps", "Shoulders"), isBodyweight = true),
        Exercise("Pec Deck", "Chest"),
        Exercise("Dumbbell Bench Press", "Chest", listOf("Triceps", "Shoulders")),
        Exercise("Incline Dumbbell Press", "Chest", listOf("Shoulders", "Triceps")),
        
        // ===== BACK (Lats) =====
        Exercise("Pull-ups", "Back", listOf("Biceps", "Forearms"), isBodyweight = true),
        Exercise("Chin-ups", "Back", listOf("Biceps", "Forearms"), isBodyweight = true),
        Exercise("Lat Pulldown", "Back", listOf("Biceps")),
        Exercise("Close Grip Lat Pulldown", "Back", listOf("Biceps")),
        Exercise("Half-Kneeling Lat Pulldown", "Back", listOf("Biceps")),
        Exercise("Barbell Row", "Back", listOf("Biceps", "Lower Back", "Trapezius")),
        Exercise("Dumbbell Row", "Back", listOf("Biceps", "Lower Back")),
        Exercise("Seated Cable Row", "Back", listOf("Biceps", "Trapezius")),
        Exercise("Chest-Supported Cable Row", "Back", listOf("Biceps")),
        Exercise("T-Bar Row", "Back", listOf("Biceps", "Lower Back", "Trapezius")),
        Exercise("Face Pull", "Back", listOf("Shoulders", "Trapezius")),
        Exercise("Straight Arm Pulldown", "Back"),
        Exercise("Pendlay Row", "Back", listOf("Biceps", "Lower Back")),
        
        // ===== LOWER BACK =====
        Exercise("Back Extension", "Lower Back", listOf("Glutes", "Hamstrings")),
        Exercise("Good Morning", "Lower Back", listOf("Hamstrings", "Glutes")),
        Exercise("Hyperextension", "Lower Back", listOf("Glutes", "Hamstrings")),
        Exercise("Reverse Hyper", "Lower Back", listOf("Glutes", "Hamstrings")),
        Exercise("Superman", "Lower Back", listOf("Glutes"), isBodyweight = true),
        
        // ===== TRAPEZIUS =====
        Exercise("Barbell Shrug", "Trapezius", listOf("Forearms")),
        Exercise("Dumbbell Shrug", "Trapezius", listOf("Forearms")),
        Exercise("Upright Row", "Trapezius", listOf("Shoulders")),
        Exercise("Farmer's Walk", "Trapezius", listOf("Forearms", "Core")),
        Exercise("Rack Pull", "Trapezius", listOf("Lower Back", "Forearms", "Glutes")),
        
        // ===== SHOULDERS =====
        Exercise("Overhead Press", "Shoulders", listOf("Triceps", "Core")),
        Exercise("Dumbbell Shoulder Press", "Shoulders", listOf("Triceps")),
        Exercise("Lateral Raise", "Shoulders"),
        Exercise("Front Raise", "Shoulders"),
        Exercise("Reverse Fly", "Shoulders", listOf("Back", "Trapezius")),
        Exercise("Arnold Press", "Shoulders", listOf("Triceps")),
        Exercise("Cable Lateral Raise", "Shoulders"),
        Exercise("Machine Shoulder Press", "Shoulders", listOf("Triceps")),
        Exercise("Behind the Neck Press", "Shoulders", listOf("Triceps")),
        Exercise("Lu Raise", "Shoulders", listOf("Trapezius")),
        
        // ===== BICEPS =====
        Exercise("Barbell Curl", "Biceps", listOf("Forearms")),
        Exercise("Dumbbell Curl", "Biceps", listOf("Forearms")),
        Exercise("Hammer Curl", "Biceps", listOf("Forearms")),
        Exercise("Preacher Curl", "Biceps"),
        Exercise("Concentration Curl", "Biceps"),
        Exercise("Cable Curl", "Biceps"),
        Exercise("Incline Dumbbell Curl", "Biceps"),
        Exercise("Spider Curl", "Biceps"),
        Exercise("EZ Bar Curl", "Biceps", listOf("Forearms")),
        Exercise("21s", "Biceps"),
        
        // ===== TRICEPS =====
        Exercise("Tricep Pushdown", "Triceps"),
        Exercise("Skull Crushers", "Triceps"),
        Exercise("Overhead Tricep Extension", "Triceps"),
        Exercise("Diamond Push-ups", "Triceps", listOf("Chest", "Shoulders"), isBodyweight = true),
        Exercise("Dips (Triceps)", "Triceps", listOf("Chest", "Shoulders"), isBodyweight = true),
        Exercise("Close Grip Bench Press", "Triceps", listOf("Chest", "Shoulders")),
        Exercise("Tricep Kickback", "Triceps"),
        Exercise("Rope Pushdown", "Triceps"),
        Exercise("JM Press", "Triceps", listOf("Chest")),
        
        // ===== FOREARMS =====
        Exercise("Wrist Curl", "Forearms"),
        Exercise("Reverse Wrist Curl", "Forearms"),
        Exercise("Reverse Curl", "Forearms", listOf("Biceps")),
        Exercise("Wrist Roller", "Forearms"),
        Exercise("Plate Pinch", "Forearms"),
        Exercise("Dead Hang", "Forearms", isBodyweight = true),
        
        // ===== QUADS =====
        Exercise("Squat", "Quads", listOf("Glutes", "Adductors", "Core", "Lower Back")),
        Exercise("Front Squat", "Quads", listOf("Core", "Glutes")),
        Exercise("Leg Press", "Quads", listOf("Glutes")),
        Exercise("Leg Extension", "Quads"),
        Exercise("Hack Squat", "Quads", listOf("Glutes")),
        Exercise("Sissy Squat", "Quads", isBodyweight = true),
        Exercise("Walking Lunges", "Quads", listOf("Glutes", "Hamstrings")),
        Exercise("Step-ups", "Quads", listOf("Glutes")),
        Exercise("Goblet Squat", "Quads", listOf("Glutes", "Core")),
        Exercise("Pause Squat", "Quads", listOf("Glutes", "Core")),
        
        // ===== HAMSTRINGS =====
        Exercise("Romanian Deadlift", "Hamstrings", listOf("Glutes", "Lower Back")),
        Exercise("Lying Leg Curl", "Hamstrings"),
        Exercise("Seated Leg Curl", "Hamstrings"),
        Exercise("Stiff Leg Deadlift", "Hamstrings", listOf("Glutes", "Lower Back")),
        Exercise("Nordic Curl", "Hamstrings", isBodyweight = true),
        Exercise("Glute Ham Raise", "Hamstrings", listOf("Glutes", "Lower Back")),
        Exercise("Deadlift", "Hamstrings", listOf("Lower Back", "Glutes", "Trapezius", "Forearms")),
        Exercise("Single Leg Romanian Deadlift", "Hamstrings", listOf("Glutes", "Core")),
        
        // ===== GLUTES =====
        Exercise("Hip Thrust", "Glutes", listOf("Hamstrings")),
        Exercise("Glute Bridge", "Glutes", listOf("Hamstrings"), isBodyweight = true),
        Exercise("Cable Kickback", "Glutes"),
        Exercise("Sumo Deadlift", "Glutes", listOf("Hamstrings", "Adductors", "Lower Back")),
        Exercise("Bulgarian Split Squat", "Glutes", listOf("Quads")),
        Exercise("Lunges", "Glutes", listOf("Quads", "Hamstrings")),
        Exercise("Single Leg Hip Thrust", "Glutes", listOf("Hamstrings")),
        Exercise("Frog Pump", "Glutes", isBodyweight = true),
        Exercise("Banded Hip Thrust", "Glutes"),
        
        // ===== CALVES =====
        Exercise("Standing Calf Raise", "Calves"),
        Exercise("Seated Calf Raise", "Calves"),
        Exercise("Donkey Calf Raise", "Calves"),
        Exercise("Calf Press (Leg Press)", "Calves"),
        Exercise("Single Leg Calf Raise", "Calves", isBodyweight = true),
        
        // ===== ABDUCTORS =====
        Exercise("Hip Abduction Machine", "Abductors"),
        Exercise("Cable Hip Abduction", "Abductors"),
        Exercise("Side Lying Leg Raise", "Abductors", isBodyweight = true),
        Exercise("Banded Clamshell", "Abductors", listOf("Glutes")),
        Exercise("Banded Lateral Walk", "Abductors", listOf("Glutes")),
        
        // ===== ADDUCTORS =====
        Exercise("Hip Adduction Machine", "Adductors"),
        Exercise("Cable Hip Adduction", "Adductors"),
        Exercise("Copenhagen Plank", "Adductors", listOf("Core"), isBodyweight = true),
        Exercise("Sumo Squat", "Adductors", listOf("Glutes", "Quads")),
        Exercise("Wide Stance Leg Press", "Adductors", listOf("Glutes", "Quads")),
        
        // ===== CORE =====
        Exercise("Plank", "Core", listOf("Shoulders"), isBodyweight = true),
        Exercise("Crunches", "Core", isBodyweight = true),
        Exercise("Hanging Leg Raise", "Core", listOf("Forearms"), isBodyweight = true),
        Exercise("Lying Leg Raise", "Core", isBodyweight = true),
        Exercise("Russian Twist", "Core"),
        Exercise("Cable Crunch", "Core"),
        Exercise("Ab Wheel Rollout", "Core", listOf("Shoulders")),
        Exercise("Dead Bug", "Core", isBodyweight = true),
        Exercise("Pallof Press", "Core"),
        Exercise("Woodchop", "Core", listOf("Shoulders")),
        Exercise("Side Plank", "Core", isBodyweight = true),
        Exercise("Dragon Flag", "Core", isBodyweight = true),
        Exercise("Bicycle Crunch", "Core", isBodyweight = true),
        Exercise("Mountain Climbers", "Core", listOf("Shoulders"), isBodyweight = true),
        
        // ===== FULL BODY =====
        Exercise("Clean and Press", "Full Body", listOf("Shoulders", "Trapezius", "Quads", "Glutes", "Core")),
        Exercise("Thrusters", "Full Body", listOf("Quads", "Shoulders", "Glutes", "Core")),
        Exercise("Burpees", "Full Body", listOf("Chest", "Shoulders", "Quads", "Core"), isBodyweight = true),
        Exercise("Kettlebell Swing", "Full Body", listOf("Glutes", "Hamstrings", "Core", "Shoulders")),
        Exercise("Man Maker", "Full Body", listOf("Chest", "Shoulders", "Back", "Quads", "Core")),
        Exercise("Turkish Get-up", "Full Body", listOf("Shoulders", "Core", "Glutes")),
        Exercise("Power Clean", "Full Body", listOf("Trapezius", "Hamstrings", "Glutes", "Quads")),
        Exercise("Snatch", "Full Body", listOf("Shoulders", "Trapezius", "Quads", "Glutes")),
        
        // ===== CARDIO =====
        Exercise("Treadmill Run", "Cardio", listOf("Quads", "Hamstrings", "Calves"), isBodyweight = true),
        Exercise("Rowing Machine", "Cardio", listOf("Back", "Biceps", "Quads")),
        Exercise("Jump Rope", "Cardio", listOf("Calves", "Shoulders"), isBodyweight = true),
        Exercise("Stair Climber", "Cardio", listOf("Quads", "Glutes", "Calves")),
        Exercise("Battle Ropes", "Cardio", listOf("Shoulders", "Core")),
        Exercise("Cycling", "Cardio", listOf("Quads", "Hamstrings", "Glutes")),
        Exercise("Elliptical", "Cardio", listOf("Quads", "Glutes")),
        Exercise("Box Jumps", "Cardio", listOf("Quads", "Glutes", "Calves"), isBodyweight = true),
        Exercise("Assault Bike", "Cardio", listOf("Quads", "Shoulders"))
    )
    
    /**
     * Get exercises filtered by primary muscle category.
     */
    fun getByCategory(category: String): List<Exercise> {
        return exercises.filter { it.primaryMuscle == category }
    }
    
    /**
     * Get exercises that work a muscle (either primary or auxiliary).
     */
    fun getByMuscle(muscle: String): List<Exercise> {
        return exercises.filter { it.allMuscles.contains(muscle) }
    }
    
    /**
     * Search exercises by name (case-insensitive).
     */
    fun search(query: String): List<Exercise> {
        if (query.isBlank()) return exercises
        val lowerQuery = query.lowercase()
        return exercises.filter { it.name.lowercase().contains(lowerQuery) }
    }
    
    /**
     * Get exercise by exact name (case-insensitive).
     */
    fun getByName(name: String): Exercise? {
        val lowerName = name.lowercase()
        return exercises.find { it.name.lowercase() == lowerName }
    }
    
    /**
     * Get all exercise names for quick lookup.
     */
    val exerciseNames: List<String> = exercises.map { it.name }
}
