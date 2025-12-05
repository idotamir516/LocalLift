package com.workout.app.ui.navigation

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workout.app.WorkoutApp
import com.workout.app.data.entities.TemplateExercise
import com.workout.app.data.entities.TemplateSet
import com.workout.app.data.entities.WorkoutTemplate
import com.workout.app.ui.components.ExercisePicker
import com.workout.app.ui.screens.ActiveWorkoutScreen
import com.workout.app.ui.screens.ActiveWorkoutState
import com.workout.app.ui.screens.EditableExercise
import com.workout.app.ui.screens.EditableTemplateSet
import com.workout.app.ui.screens.HistoryScreen
import com.workout.app.ui.screens.StartWorkoutScreen
import com.workout.app.ui.screens.TemplateEditorScreen
import com.workout.app.ui.screens.TemplatesScreen
import com.workout.app.ui.screens.WorkoutDetailScreen
import com.workout.app.util.WorkoutExporter
import kotlinx.coroutines.launch

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    // Bottom nav tabs
    object StartWorkout : Screen("start_workout")
    object Templates : Screen("templates")
    object History : Screen("history")
    
    // Detail screens
    object ActiveWorkout : Screen("active_workout/{sessionId}") {
        fun createRoute(sessionId: Long) = "active_workout/$sessionId"
    }
    object TemplateEditor : Screen("template_editor/{templateId}") {
        fun createRoute(templateId: Long?) = "template_editor/${templateId ?: "new"}"
    }
    object WorkoutDetail : Screen("workout_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout_detail/$sessionId"
    }
}

/**
 * Bottom navigation tab configuration.
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.StartWorkout,
        label = "Workout",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    ),
    BottomNavItem(
        screen = Screen.Templates,
        label = "Templates",
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List
    ),
    BottomNavItem(
        screen = Screen.History,
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val database = remember { WorkoutApp.getDatabase(application) }
    val scope = rememberCoroutineScope()
    
    // Get data from database
    val templates = remember { database.templateDao().getAllTemplates() }
    val completedSessions = remember { database.sessionDao().getCompletedSessions() }
    val activeSession by database.sessionDao().getActiveSession().collectAsState(initial = null)
    
    Scaffold(
        bottomBar = {
            // Hide bottom bar during active workout
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Screen.ActiveWorkout.route) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.StartWorkout.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Start Workout screen
            composable(Screen.StartWorkout.route) {
                StartWorkoutScreen(
                    templates = templates,
                    activeSession = activeSession,
                    onStartWorkout = { templateId ->
                        scope.launch {
                            // Create a new workout session from template (with full set details)
                            val fullTemplate = database.templateDao().getFullTemplateWithExercisesAndSets(templateId)
                            if (fullTemplate != null) {
                                val sessionId = database.sessionDao().insertSession(
                                    com.workout.app.data.entities.WorkoutSession(
                                        templateId = templateId,
                                        templateName = fullTemplate.template.name,
                                        startedAt = System.currentTimeMillis()
                                    )
                                )
                                
                                // Create exercise logs and set logs from template
                                fullTemplate.exercises.forEachIndexed { index, exerciseWithSets ->
                                    val exercise = exerciseWithSets.exercise
                                    val exerciseLogId = database.sessionDao().saveExerciseLog(
                                        com.workout.app.data.entities.ExerciseLog(
                                            sessionId = sessionId,
                                            exerciseName = exercise.exerciseName,
                                            orderIndex = index,
                                            showRpe = exercise.showRpe
                                        )
                                    )
                                    
                                    // Create sets from template sets (with pre-filled values)
                                    if (exerciseWithSets.sets.isNotEmpty()) {
                                        exerciseWithSets.sets.sortedBy { it.setNumber }.forEach { templateSet ->
                                            database.sessionDao().saveSetLog(
                                                com.workout.app.data.entities.SetLog(
                                                    exerciseLogId = exerciseLogId,
                                                    setNumber = templateSet.setNumber,
                                                    weightLbs = templateSet.targetWeight?.toFloat(),
                                                    reps = templateSet.targetReps,
                                                    restSeconds = templateSet.restSeconds ?: exercise.restSeconds,
                                                    setType = templateSet.setType
                                                )
                                            )
                                        }
                                    } else {
                                        // Fallback for old templates without sets
                                        repeat(exercise.targetSets) { setIndex ->
                                            database.sessionDao().saveSetLog(
                                                com.workout.app.data.entities.SetLog(
                                                    exerciseLogId = exerciseLogId,
                                                    setNumber = setIndex + 1,
                                                    restSeconds = exercise.restSeconds
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                            }
                        }
                    },
                    onStartEmptyWorkout = {
                        scope.launch {
                            // Create an empty workout session (no template, no exercises)
                            val sessionId = database.sessionDao().insertSession(
                                com.workout.app.data.entities.WorkoutSession(
                                    templateId = null,
                                    templateName = null,
                                    startedAt = System.currentTimeMillis()
                                )
                            )
                            navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                        }
                    },
                    onResumeWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    },
                    onCreateTemplate = {
                        navController.navigate(Screen.TemplateEditor.createRoute(null))
                    }
                )
            }
            
            // Templates screen
            composable(Screen.Templates.route) {
                TemplatesScreen(
                    templates = templates,
                    onCreateTemplate = {
                        navController.navigate(Screen.TemplateEditor.createRoute(null))
                    },
                    onEditTemplate = { templateId ->
                        navController.navigate(Screen.TemplateEditor.createRoute(templateId))
                    },
                    onDeleteTemplate = { templateId ->
                        scope.launch {
                            val template = database.templateDao().getTemplateWithExercisesSync(templateId)
                            if (template != null) {
                                database.templateDao().deleteTemplate(template.template)
                            }
                        }
                    }
                )
            }
            
            // History screen
            composable(Screen.History.route) {
                // State to hold CSV content for saving to file
                var pendingCsvContent by remember { mutableStateOf<String?>(null) }
                
                // Launcher for creating a document (save to file)
                val createDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    uri?.let {
                        pendingCsvContent?.let { csvContent ->
                            try {
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    outputStream.write(csvContent.toByteArray())
                                }
                                Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    pendingCsvContent = null
                }
                
                HistoryScreen(
                    completedSessions = completedSessions,
                    onWorkoutClick = { sessionId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                    },
                    onShareClick = {
                        scope.launch {
                            val sessions = database.sessionDao().getAllCompletedSessionsWithDetails()
                            val csvContent = WorkoutExporter.generateCsv(sessions)
                            val file = WorkoutExporter.createCsvFile(context, csvContent)
                            val shareIntent = WorkoutExporter.createShareIntent(context, file)
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Workouts"))
                        }
                    },
                    onSaveToFileClick = {
                        scope.launch {
                            val sessions = database.sessionDao().getAllCompletedSessionsWithDetails()
                            val csvContent = WorkoutExporter.generateCsv(sessions)
                            pendingCsvContent = csvContent
                            val fileName = WorkoutExporter.generateFileName()
                            createDocumentLauncher.launch(fileName)
                        }
                    }
                )
            }
            
            // Active workout screen
            composable(
                route = Screen.ActiveWorkout.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                
                // Create state holder for this workout session
                val workoutState = remember(sessionId) {
                    ActiveWorkoutState(
                        sessionId = sessionId,
                        database = database,
                        scope = scope,
                        context = context
                    )
                }
                
                // Collect state
                val session by workoutState.session.collectAsState()
                val exercises by workoutState.exercises.collectAsState()
                val timerState by workoutState.timerState.collectAsState()
                val showExercisePicker by workoutState.showExercisePicker.collectAsState()
                
                ActiveWorkoutScreen(
                    session = session,
                    exercises = exercises,
                    timerState = timerState,
                    onSetWeightChange = { exerciseIndex, setNumber, weight ->
                        workoutState.updateSetWeight(exerciseIndex, setNumber, weight)
                    },
                    onSetRepsChange = { exerciseIndex, setNumber, reps ->
                        workoutState.updateSetReps(exerciseIndex, setNumber, reps)
                    },
                    onSetRestChange = { exerciseIndex, setNumber, restSeconds ->
                        workoutState.updateSetRest(exerciseIndex, setNumber, restSeconds)
                    },
                    onSetTypeChange = { exerciseIndex, setNumber, setType ->
                        workoutState.updateSetType(exerciseIndex, setNumber, setType)
                    },
                    onSetRpeChange = { exerciseIndex, setNumber, rpe ->
                        workoutState.updateSetRpe(exerciseIndex, setNumber, rpe)
                    },
                    onSetComplete = { exerciseIndex, setNumber ->
                        workoutState.completeSet(exerciseIndex, setNumber)
                    },
                    onAddSet = { exerciseIndex ->
                        workoutState.addSet(exerciseIndex)
                    },
                    onRemoveSet = { exerciseIndex, setNumber ->
                        workoutState.removeSet(exerciseIndex, setNumber)
                    },
                    onAddExercise = {
                        workoutState.showExercisePicker()
                    },
                    onMoveExercise = { fromIndex, toIndex ->
                        workoutState.reorderExercises(fromIndex, toIndex)
                    },
                    onToggleExpand = { exerciseIndex ->
                        workoutState.toggleExpand(exerciseIndex)
                    },
                    onTimerPause = { workoutState.pauseTimer() },
                    onTimerResume = { workoutState.resumeTimer() },
                    onTimerSkip = { workoutState.skipTimer() },
                    onTimerAddTime = { workoutState.addTimerTime() },
                    onTimerSubtractTime = { workoutState.subtractTimerTime() },
                    onFinishWorkout = {
                        scope.launch {
                            workoutState.finishWorkout()
                            // Clear entire backstack and go to History
                            // This ensures no stale ActiveWorkout state remains
                            navController.navigate(Screen.History.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onCancelWorkout = {
                        scope.launch {
                            workoutState.cancelWorkout()
                            navController.popBackStack()
                        }
                    }
                )
                
                // Exercise picker bottom sheet
                ExercisePicker(
                    isVisible = showExercisePicker,
                    onDismiss = { workoutState.hideExercisePicker() },
                    onExerciseSelected = { exercise ->
                        workoutState.addExercise(exercise)
                    },
                    selectedExercises = exercises.map { it.exerciseName }
                )
            }
            
            // Template editor screen
            composable(
                route = Screen.TemplateEditor.route,
                arguments = listOf(
                    navArgument("templateId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val templateIdStr = backStackEntry.arguments?.getString("templateId")
                val templateId = if (templateIdStr == "new") null else templateIdStr?.toLongOrNull()
                
                // Load existing template with exercises and sets if editing
                var loadedExercises by remember { mutableStateOf<List<EditableExercise>?>(null) }
                var loadedTemplateName by remember { mutableStateOf("") }
                
                LaunchedEffect(templateId) {
                    if (templateId != null) {
                        val fullTemplate = database.templateDao().getFullTemplateWithExercisesAndSets(templateId)
                        if (fullTemplate != null) {
                            loadedTemplateName = fullTemplate.template.name
                            loadedExercises = fullTemplate.exercises.mapIndexed { index, exerciseWithSets ->
                                EditableExercise(
                                    id = exerciseWithSets.exercise.id,
                                    exerciseName = exerciseWithSets.exercise.exerciseName,
                                    targetSets = exerciseWithSets.sets.size.coerceAtLeast(exerciseWithSets.exercise.targetSets),
                                    restSeconds = exerciseWithSets.exercise.restSeconds,
                                    orderIndex = index,
                                    showRpe = exerciseWithSets.exercise.showRpe,
                                    sets = if (exerciseWithSets.sets.isNotEmpty()) {
                                        exerciseWithSets.sets.sortedBy { it.setNumber }.map { set ->
                                            EditableTemplateSet(
                                                setNumber = set.setNumber,
                                                targetWeight = set.targetWeight,
                                                targetReps = set.targetReps,
                                                restSeconds = set.restSeconds,
                                                setType = set.setType
                                            )
                                        }
                                    } else {
                                        // Migrate old templates: create sets from targetSets count
                                        (1..exerciseWithSets.exercise.targetSets).map { num ->
                                            EditableTemplateSet(
                                                setNumber = num,
                                                restSeconds = exerciseWithSets.exercise.restSeconds
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        loadedExercises = emptyList()
                    }
                }
                
                // Only show editor once loaded
                if (loadedExercises != null) {
                    TemplateEditorScreen(
                        templateId = templateId,
                        initialName = loadedTemplateName,
                        initialExercises = loadedExercises!!,
                        onSave = { name, exercises ->
                            scope.launch {
                                if (templateId != null) {
                                    // Update existing template
                                    database.templateDao().updateTemplate(
                                        WorkoutTemplate(
                                            id = templateId,
                                            name = name,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                    // Delete old exercises (sets cascade delete)
                                    database.templateDao().deleteExercisesForTemplate(templateId)
                                    
                                    // Insert new exercises and sets
                                    exercises.forEachIndexed { index, exercise ->
                                        val exerciseId = database.templateDao().insertTemplateExercise(
                                            TemplateExercise(
                                                templateId = templateId,
                                                exerciseName = exercise.exerciseName,
                                                targetSets = exercise.sets.size,
                                                restSeconds = exercise.sets.firstOrNull()?.restSeconds,
                                                orderIndex = index,
                                                showRpe = exercise.showRpe
                                            )
                                        )
                                        // Insert sets for this exercise
                                        val templateSets = exercise.sets.map { set ->
                                            com.workout.app.data.entities.TemplateSet(
                                                templateExerciseId = exerciseId,
                                                setNumber = set.setNumber,
                                                targetWeight = set.targetWeight,
                                                targetReps = set.targetReps,
                                                restSeconds = set.restSeconds,
                                                setType = set.setType
                                            )
                                        }
                                        database.templateDao().insertTemplateSets(templateSets)
                                    }
                                } else {
                                    // Create new template
                                    val newTemplateId = database.templateDao().insertTemplate(
                                        WorkoutTemplate(name = name)
                                    )
                                    exercises.forEachIndexed { index, exercise ->
                                        val exerciseId = database.templateDao().insertTemplateExercise(
                                            TemplateExercise(
                                                templateId = newTemplateId,
                                                exerciseName = exercise.exerciseName,
                                                targetSets = exercise.sets.size,
                                                restSeconds = exercise.sets.firstOrNull()?.restSeconds,
                                                orderIndex = index,
                                                showRpe = exercise.showRpe
                                            )
                                        )
                                        // Insert sets for this exercise
                                        val templateSets = exercise.sets.map { set ->
                                            com.workout.app.data.entities.TemplateSet(
                                                templateExerciseId = exerciseId,
                                                setNumber = set.setNumber,
                                                targetWeight = set.targetWeight,
                                                targetReps = set.targetReps,
                                                restSeconds = set.restSeconds,
                                                setType = set.setType
                                            )
                                        }
                                        database.templateDao().insertTemplateSets(templateSets)
                                    }
                                }
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            
            // Workout detail screen
            composable(
                route = Screen.WorkoutDetail.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                val sessionWithDetails by remember(sessionId) {
                    database.sessionDao().getSessionWithDetails(sessionId)
                }.collectAsState(initial = null)
                
                WorkoutDetailScreen(
                    sessionWithDetails = sessionWithDetails,
                    onBack = { navController.popBackStack() },
                    onSaveAsTemplate = {
                        scope.launch {
                            sessionWithDetails?.let { session ->
                                // Create a new template from the workout
                                val templateName = session.session.templateName ?: "Workout ${java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(session.session.startedAt))}"
                                
                                val templateId = database.templateDao().insertTemplate(
                                    WorkoutTemplate(name = templateName)
                                )
                                
                                // Create template exercises and sets from the workout
                                session.exercises.forEachIndexed { index, exerciseWithSets ->
                                    val exerciseId = database.templateDao().insertTemplateExercise(
                                        TemplateExercise(
                                            templateId = templateId,
                                            exerciseName = exerciseWithSets.exerciseLog.exerciseName,
                                            targetSets = exerciseWithSets.sets.size,
                                            restSeconds = exerciseWithSets.sets.firstOrNull()?.restSeconds ?: 90,
                                            orderIndex = index,
                                            showRpe = exerciseWithSets.exerciseLog.showRpe
                                        )
                                    )
                                    
                                    // Create template sets from the actual sets
                                    val templateSets = exerciseWithSets.sets.map { setLog ->
                                        TemplateSet(
                                            templateExerciseId = exerciseId,
                                            setNumber = setLog.setNumber,
                                            targetWeight = setLog.weightLbs?.toInt(),
                                            targetReps = setLog.reps,
                                            restSeconds = setLog.restSeconds,
                                            setType = setLog.setType
                                        )
                                    }
                                    database.templateDao().insertTemplateSets(templateSets)
                                }
                                
                                // Navigate to template editor to let user customize the name
                                navController.navigate(Screen.TemplateEditor.createRoute(templateId))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { 
                it.route == item.screen.route 
            } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                onClick = {
                    navController.navigate(item.screen.route) {
                        // Pop up to the start destination to avoid building up a stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
