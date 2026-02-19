# LLM Agent Guide: Workout Tracking App

> **Purpose**: This document provides comprehensive context for LLM agents to quickly understand and contribute to this codebase. It covers architecture, patterns, data models, conventions, and common tasks.

---

## 1. Project Overview

This is a **native Android workout tracking app** built with modern Android development practices:

| Aspect | Technology |
|--------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose with Material 3 |
| Database | Room (SQLite) with migrations |
| State Management | Kotlin StateFlow + Compose state |
| Navigation | Jetpack Navigation Compose |
| Min SDK | 26 (Android 8.0) |

### Core Functionality
- Create workout templates with exercises, sets, rest times
- Log workouts with weight, reps, RPE, set types
- Track progress via charts, calendar, 1RM estimates
- Analyze training volume by muscle group
- Track body weight over time
- Manage training phases (bulk, cut, etc.)

---

## 2. Project Structure

```
app/src/main/java/com/workout/app/
├── MainActivity.kt              # Single activity, hosts Compose
├── WorkoutApp.kt               # Application class with DB singleton
│
├── data/
│   ├── AppDatabase.kt          # Room database + all migrations
│   ├── ExerciseLibrary.kt      # Static list of ~100 exercises
│   ├── entities/               # Room @Entity classes
│   │   ├── WorkoutTemplate.kt
│   │   ├── TemplateExercise.kt
│   │   ├── TemplateSet.kt
│   │   ├── WorkoutSession.kt
│   │   ├── ExerciseLog.kt
│   │   ├── SetLog.kt
│   │   ├── CustomExercise.kt
│   │   ├── TrainingPhase.kt
│   │   ├── WeightEntry.kt
│   │   └── WorkoutFolder.kt
│   └── dao/                    # Room @Dao interfaces
│       ├── TemplateDao.kt
│       ├── SessionDao.kt
│       ├── CustomExerciseDao.kt
│       ├── PhaseDao.kt
│       ├── WeightDao.kt
│       └── FolderDao.kt
│
├── ui/
│   ├── theme/                  # Material 3 theme colors, typography
│   ├── navigation/
│   │   └── AppNavigation.kt    # All routes + NavHost (~1400 lines)
│   ├── screens/                # Full-screen composables
│   │   ├── HomeScreen.kt
│   │   ├── WorkoutScreen.kt         # Template list
│   │   ├── ActiveWorkoutScreen.kt   # Live workout logging
│   │   ├── ActiveWorkoutState.kt    # State management for active workout
│   │   ├── TemplateEditorScreen.kt  # Create/edit templates
│   │   ├── WorkoutDetailScreen.kt   # View completed workout
│   │   ├── EditWorkoutScreen.kt     # Edit completed workout
│   │   ├── ProgressScreen.kt        # Charts, calendar, analytics
│   │   ├── BodyScreen.kt            # Body metrics hub
│   │   ├── WeightScreen.kt          # Body weight tracking
│   │   ├── ProgramAnalyzerScreen.kt # Muscle volume analysis
│   │   ├── TrainingPhasesScreen.kt  # Bulk/cut phases
│   │   └── SettingsScreen.kt
│   └── components/
│       ├── ExerciseCard.kt          # Exercise with sets in workout
│       ├── ExercisePicker.kt        # Exercise selection dialog
│       ├── SetRow.kt                # Single set row (weight/reps/checkbox)
│       ├── RestTimerDisplay.kt      # Countdown timer overlay
│       ├── FolderComponents.kt      # Folder management UI
│       └── shared/                  # Reusable shared components
│           ├── SharedConstants.kt       # UI constants (spacing, shapes, etc.)
│           ├── InputValidation.kt       # Validation utilities
│           ├── NumericInputField.kt     # Styled number input
│           ├── SetTypeIndicator.kt      # Set type badge (Regular/W/D)
│           ├── RestTimePicker.kt        # Rest time selection popup
│           ├── ReorderButtons.kt        # Up/down arrows
│           ├── EditableNoteSection.kt   # Note editor
│           └── ConfirmationDialog.kt    # Reusable alert dialog
│
├── util/
│   ├── AudioPlayer.kt          # Timer completion sound
│   ├── TimerManager.kt         # Countdown timer state machine
│   ├── WorkoutExporter.kt      # CSV export
│   ├── ProgramAnalyzer.kt      # Muscle group volume analysis
│   ├── SettingsManager.kt      # SharedPreferences wrapper
│   └── DateFormatUtils.kt      # Date formatting utilities
│
└── service/
    └── RestTimerService.kt     # Foreground service for timer
```

---

## 3. Data Model

### Entity Relationship Diagram

```
WorkoutFolder (optional)
    │
    └── WorkoutTemplate (1)
            │
            ├── TemplateExercise (*)
            │       │
            │       └── TemplateSet (*)
            │
            └── WorkoutSession (*)  [when started from template]
                    │
                    └── ExerciseLog (*)
                            │
                            └── SetLog (*)

CustomExercise (standalone - user-created exercises)
TrainingPhase (standalone - bulk/cut tracking)
WeightEntry (standalone - body weight logs)
```

### Key Entities

#### WorkoutTemplate
```kotlin
data class WorkoutTemplate(
    val id: Long,
    val name: String,
    val folderId: Long?,      // null = uncategorized
    val createdAt: Long,
    val updatedAt: Long
)
```

#### TemplateExercise
```kotlin
data class TemplateExercise(
    val id: Long,
    val templateId: Long,
    val exerciseName: String,
    val targetSets: Int,      // Default number of sets
    val restSeconds: Int?,    // Rest time between sets
    val showRpe: Boolean,     // Show RPE input
    val orderIndex: Int,      // Position in template
    val note: String?         // Exercise-specific note
)
```

#### TemplateSet
```kotlin
data class TemplateSet(
    val id: Long,
    val templateExerciseId: Long,
    val setNumber: Int,
    val targetWeight: Int?,
    val targetReps: Int?,
    val restSeconds: Int?,
    val setType: SetType      // REGULAR, WARMUP, DROP_SET
)
```

#### WorkoutSession
```kotlin
data class WorkoutSession(
    val id: Long,
    val templateId: Long?,    // null for quick workout
    val templateName: String?,
    val startedAt: Long,
    val completedAt: Long?,
    val isCompleted: Boolean
)
```

#### ExerciseLog
```kotlin
data class ExerciseLog(
    val id: Long,
    val sessionId: Long,
    val exerciseName: String,
    val showRpe: Boolean,
    val orderIndex: Int
)
```

#### SetLog
```kotlin
data class SetLog(
    val id: Long,
    val exerciseLogId: Long,
    val setNumber: Int,
    val reps: Int?,
    val weightLbs: Float?,    // Weight in pounds
    val restSeconds: Int?,
    val rpe: Float?,          // 1-10 scale
    val setType: SetType,
    val completedAt: Long?
)

enum class SetType {
    REGULAR,   // Normal working set (shows number: 1, 2, 3...)
    WARMUP,    // Warmup set (shows "W")
    DROP_SET   // Drop set (shows "D")
}
```

### Important: Template vs Session Data

- **Templates** define the *plan* (what exercises, how many sets, target weight/reps)
- **Sessions** capture *what actually happened* (logged weight, reps, RPE)
- When starting a workout from a template, the app creates a new `WorkoutSession` and copies exercises/sets from the template into `ExerciseLog`/`SetLog` entities

---

## 4. Navigation Routes

All navigation is defined in `AppNavigation.kt`:

```kotlin
sealed class Screen(val route: String) {
    // Bottom navigation tabs
    object Home : Screen("home")
    object StartWorkout : Screen("start_workout")
    object Progress : Screen("progress")
    object Body : Screen("body")
    
    // Detail screens with parameters
    object ActiveWorkout : Screen("active_workout/{sessionId}")
    object TemplateEditor : Screen("template_editor/{templateId}")
    object WorkoutDetail : Screen("workout_detail/{sessionId}")
    object EditWorkout : Screen("edit_workout/{sessionId}")
    
    // Modal screens
    object ProgramAnalyzer : Screen("program_analyzer")
    object Settings : Screen("settings")
    object TrainingPhases : Screen("training_phases")
    object Weight : Screen("weight")
}
```

### Navigation Patterns

```kotlin
// Navigate with parameter
navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))

// Pop back
navController.popBackStack()

// Navigate and clear back stack
navController.navigate(Screen.Home.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
}
```

---

## 5. UI Patterns and Conventions

### Composable Structure

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreen(
    // Required parameters first
    data: SomeData,
    // Callbacks
    onAction: () -> Unit,
    onNavigateBack: () -> Unit,
    // Optional with defaults last
    modifier: Modifier = Modifier
) {
    // Local state
    var showDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = { /* TopAppBar */ },
        floatingActionButton = { /* FAB if needed */ }
    ) { paddingValues ->
        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // List items
        }
    }
    
    // Dialogs rendered outside Scaffold
    if (showDialog) {
        ConfirmationDialog(...)
    }
}
```

### Shared Components (in `ui/components/shared/`)

Always check for existing shared components before creating new UI:

| Component | Purpose |
|-----------|---------|
| `NumericInputField` | Styled number input with validation |
| `SetTypeIndicator` | Clickable badge showing set type (1, 2, W, D) |
| `RestTimePicker` | Modal for selecting rest time |
| `ReorderButtons` | Up/down arrows for reordering |
| `EditableNoteSection` | Inline note editor |
| `ConfirmationDialog` | Reusable alert dialog |
| `SharedConstants` | UI constants (spacing, shapes, alpha values) |
| `InputValidation` | Validation helpers for weight, reps, RPE |

### Using SharedConstants

```kotlin
import com.workout.app.ui.components.shared.SharedConstants

// Spacing
Modifier.padding(SharedConstants.StandardSpacing)  // 16.dp

// Shapes
.clip(SharedConstants.MediumRoundedShape)  // 12.dp corner radius

// Alpha values
.copy(alpha = SharedConstants.SubtleAlpha)  // 0.5f
```

### Using InputValidation

```kotlin
import com.workout.app.ui.components.shared.InputValidation

// Filter input
val filtered = InputValidation.filterIntegerInput(text, maxDigits = 3)

// Validate
val result = InputValidation.validateWeight(input, isKg = true)
when (result) {
    is InputValidation.ValidationResult.Valid -> { /* proceed */ }
    is InputValidation.ValidationResult.Empty -> { /* optional */ }
    is InputValidation.ValidationResult.Invalid -> { /* show error */ }
}

// Parse safely
val reps: Int? = InputValidation.parseReps(input)
```

### String Resources

Localized strings are in `res/values/strings.xml`. Use them via:

```kotlin
import androidx.compose.ui.res.stringResource
import com.workout.app.R

Text(stringResource(R.string.action_cancel))
Text(stringResource(R.string.a11y_move_up, itemName))  // With format args
```

**Note**: `stringResource()` is a @Composable function. Cannot be called inside `Modifier.semantics { }` blocks - extract to a variable first:

```kotlin
val description = stringResource(R.string.some_string)
Modifier.semantics { contentDescription = description }
```

---

## 6. Database Operations

### Accessing the Database

```kotlin
// In a Composable (via WorkoutApp singleton)
val context = LocalContext.current
val database = remember { WorkoutApp.getDatabase(context.applicationContext as Application) }

// Get DAO
val templateDao = database.templateDao()
val sessionDao = database.sessionDao()
```

### Common DAO Patterns

```kotlin
// DAOs return Flow for reactive updates
@Query("SELECT * FROM workout_templates ORDER BY updatedAt DESC")
fun getAllTemplates(): Flow<List<WorkoutTemplate>>

// Suspend functions for mutations
@Insert
suspend fun insertTemplate(template: WorkoutTemplate): Long

@Update
suspend fun updateTemplate(template: WorkoutTemplate)

@Delete
suspend fun deleteTemplate(template: WorkoutTemplate)
```

### Using in Composables

```kotlin
// Collect Flow as State
val templates by templateDao.getAllTemplates().collectAsState(initial = emptyList())

// Launch coroutine for mutations
val scope = rememberCoroutineScope()
scope.launch {
    templateDao.insertTemplate(newTemplate)
}
```

### Database Migrations

When modifying schema, add a migration in `AppDatabase.kt`:

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQL statements to transform schema
        database.execSQL("ALTER TABLE ... ADD COLUMN ...")
    }
}

// Add to database builder
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_X_Y)
    .build()
```

**Current version**: 12

---

## 7. Exercise Library

The app has a static library of ~100 exercises in `ExerciseLibrary.kt`:

```kotlin
data class Exercise(
    val name: String,
    val primaryMuscle: String,        // Main muscle group
    val auxiliaryMuscles: List<String>,  // Secondary muscles
    val isBodyweight: Boolean
)

// Access
ExerciseLibrary.exercises  // List<Exercise>
ExerciseLibrary.categories // List<String> - all muscle groups
```

### Muscle Categories
Chest, Back, Lower Back, Trapezius, Shoulders, Biceps, Triceps, Forearms, Quads, Hamstrings, Glutes, Calves, Abductors, Adductors, Core, Full Body, Cardio

### Custom Exercises
Users can create custom exercises stored in `custom_exercises` table with user-defined muscle targeting.

---

## 8. Key Features Implementation

### Rest Timer

The rest timer uses a foreground service (`RestTimerService.kt`) with `TimerManager.kt` for state:

```kotlin
data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)
```

Timer is controlled via callbacks:
```kotlin
onTimerPause: () -> Unit
onTimerResume: () -> Unit
onTimerSkip: () -> Unit
onTimerAddTime: () -> Unit      // +5 seconds
onTimerSubtractTime: () -> Unit // -5 seconds
```

### Effective Sets Calculation

The app uses fractional set counting:
- Primary muscle: 1.0 effective sets
- Auxiliary muscle: 0.5 effective sets

Settings control whether warmup/drop sets count.

### Previous Lift Display

When logging sets, the app shows previous performance. Configurable via settings:
- "Same Template" - only from workouts using the same template
- "Any Workout" - from any workout with that exercise

---

## 9. Common Tasks Guide

### Adding a New Screen

1. Create screen file in `ui/screens/`
2. Add route to `Screen` sealed class in `AppNavigation.kt`
3. Add `composable()` block in `NavHost`
4. Add navigation trigger (button, menu item, etc.)

### Adding a New Entity

1. Create entity in `data/entities/`
2. Create DAO in `data/dao/`
3. Add entity to `@Database(entities = [...])` in `AppDatabase.kt`
4. Increment database version
5. Add migration

### Adding a Shared Component

1. Create in `ui/components/shared/`
2. Add comprehensive KDoc documentation
3. Use `SharedConstants` for styling
4. Use string resources for text
5. Add accessibility (contentDescription)

### Modifying Existing UI

1. Check if shared components can be reused
2. Maintain existing patterns (Scaffold, LazyColumn, etc.)
3. Use Material 3 components
4. Add/update accessibility descriptions
5. Test with different data states (empty, loading, error)

---

## 10. Testing Checklist

When making changes, verify:

- [ ] Build succeeds: `./gradlew assembleDebug`
- [ ] No lint errors: `./gradlew lint`
- [ ] UI renders correctly with empty state
- [ ] UI handles loading state
- [ ] Navigation works correctly
- [ ] Database migrations work (if schema changed)
- [ ] Accessibility labels present on interactive elements

---

## 11. Code Style Guidelines

### Naming Conventions
- Files: PascalCase (`WorkoutScreen.kt`)
- Functions: camelCase (`onSetComplete`)
- Constants: SCREAMING_SNAKE_CASE in objects, PascalCase for vals
- Parameters: camelCase, callbacks start with `on`

### Import Organization
```kotlin
// Standard library
import kotlin.collections.*

// Android framework
import android.content.*
import androidx.compose.*

// Project imports
import com.workout.app.*
```

### Compose Best Practices
- Parameters: required first, callbacks, then optional with defaults
- State hoisting: lift state to caller when needed for coordination
- Side effects: use `LaunchedEffect`, `DisposableEffect` appropriately
- Keys: use stable keys for `remember` and list items

---

## 12. Quick Reference

### File Locations by Feature

| Feature | Primary Files |
|---------|---------------|
| Workout logging | `ActiveWorkoutScreen.kt`, `ActiveWorkoutState.kt`, `ExerciseCard.kt`, `SetRow.kt` |
| Templates | `TemplateEditorScreen.kt`, `WorkoutScreen.kt` |
| Progress/charts | `ProgressScreen.kt` |
| Body tracking | `BodyScreen.kt`, `WeightScreen.kt` |
| Training phases | `TrainingPhasesScreen.kt` |
| Rest timer | `RestTimerService.kt`, `TimerManager.kt`, `RestTimerDisplay.kt` |
| Exercise selection | `ExercisePicker.kt`, `ExerciseLibrary.kt` |
| Settings | `SettingsScreen.kt`, `SettingsManager.kt` |
| Data export | `WorkoutExporter.kt` |

### Important Constants

```kotlin
// SharedConstants.kt
SharedConstants.StandardSpacing = 16.dp
SharedConstants.SmallCornerRadius = 8.dp
SharedConstants.SubtleAlpha = 0.5f
SharedConstants.DisabledAlpha = 0.2f

// RestTimeConstants
RestTimeConstants.Presets = listOf(30, 60, 90, 120, 180)
RestTimeConstants.MinRestTimeSeconds = 15
RestTimeConstants.MaxRestTimeSeconds = 600
```

### Gradle Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew lint                   # Run lint checks
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests
```

---

## 13. Appendix: Database Schema (v12)

```sql
-- Templates
CREATE TABLE workout_templates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    folderId INTEGER REFERENCES workout_folders(id) ON DELETE SET NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

CREATE TABLE template_exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    templateId INTEGER NOT NULL REFERENCES workout_templates(id) ON DELETE CASCADE,
    exerciseName TEXT NOT NULL,
    targetSets INTEGER NOT NULL DEFAULT 3,
    restSeconds INTEGER,
    showRpe INTEGER NOT NULL DEFAULT 0,
    orderIndex INTEGER NOT NULL,
    note TEXT
);

CREATE TABLE template_sets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    templateExerciseId INTEGER NOT NULL REFERENCES template_exercises(id) ON DELETE CASCADE,
    setNumber INTEGER NOT NULL,
    targetWeight INTEGER,
    targetReps INTEGER,
    restSeconds INTEGER,
    setType TEXT NOT NULL DEFAULT 'REGULAR'
);

-- Sessions (actual workouts)
CREATE TABLE workout_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    templateId INTEGER,
    templateName TEXT,
    startedAt INTEGER NOT NULL,
    completedAt INTEGER,
    isCompleted INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE exercise_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sessionId INTEGER NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exerciseName TEXT NOT NULL,
    showRpe INTEGER NOT NULL DEFAULT 0,
    orderIndex INTEGER NOT NULL
);

CREATE TABLE set_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    exerciseLogId INTEGER NOT NULL REFERENCES exercise_logs(id) ON DELETE CASCADE,
    setNumber INTEGER NOT NULL,
    reps INTEGER,
    weightLbs REAL,
    restSeconds INTEGER,
    rpe REAL,
    setType TEXT NOT NULL DEFAULT 'REGULAR',
    completedAt INTEGER
);

-- Other tables
CREATE TABLE workout_folders (...);
CREATE TABLE custom_exercises (...);
CREATE TABLE training_phases (...);
CREATE TABLE weight_entries (...);
```

---

*Last updated: January 2026*
