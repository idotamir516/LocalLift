# Workout Tracking App - Architecture & Implementation Guide

This document provides an in-depth explanation of how the Workout Tracking App works. It's designed to help developers quickly understand the codebase and make modifications.

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Data Model](#data-model)
5. [Navigation](#navigation)
6. [Core Flows](#core-flows)
7. [Key Components](#key-components)
8. [Utilities](#utilities)
9. [State Management](#state-management)
10. [Common Patterns](#common-patterns)

---

## Overview

The Workout Tracking App is an Android application for logging gym workouts. Users can:
- Create reusable workout templates
- Execute workouts (from templates or empty "quick workouts")
- Log sets with weight, reps, RPE, and set types
- Use a rest timer between sets
- View workout history
- Export data to CSV

The app prioritizes simplicity and reliability over architectural elegance. All data is stored locally using Room.

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose with Material 3 |
| Database | Room (SQLite) |
| Navigation | Jetpack Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 26 (Android 8.0) |

### Key Dependencies (see `app/build.gradle.kts`)
- `androidx.room:room-runtime` / `room-ktx` - Database
- `androidx.navigation:navigation-compose` - Navigation
- `androidx.compose.material3:material3` - UI components
- `androidx.core:core-ktx` - FileProvider for exports

---

## Project Structure

```
app/src/main/java/com/workout/app/
├── MainActivity.kt              # Entry point, sets up Compose theme
├── WorkoutApp.kt               # Application class with database singleton
├── data/
│   ├── AppDatabase.kt          # Room database definition
│   ├── ExerciseLibrary.kt      # Static list of ~100 exercises
│   ├── entities/               # Room entities (tables)
│   │   ├── WorkoutTemplate.kt
│   │   ├── TemplateExercise.kt
│   │   ├── TemplateSet.kt
│   │   ├── WorkoutSession.kt
│   │   ├── ExerciseLog.kt
│   │   └── SetLog.kt
│   └── dao/                    # Data Access Objects
│       ├── TemplateDao.kt
│       └── SessionDao.kt
├── ui/
│   ├── theme/                  # Material 3 theme (colors, typography)
│   ├── navigation/
│   │   └── AppNavigation.kt    # All routes and navigation logic
│   ├── screens/
│   │   ├── StartWorkoutScreen.kt
│   │   ├── TemplatesScreen.kt
│   │   ├── TemplateEditorScreen.kt
│   │   ├── ActiveWorkoutScreen.kt
│   │   ├── ActiveWorkoutState.kt   # State holder for active workout
│   │   ├── HistoryScreen.kt
│   │   └── WorkoutDetailScreen.kt
│   └── components/
│       ├── ExerciseCard.kt
│       ├── SetRow.kt
│       ├── ExercisePicker.kt
│       └── RestTimerDisplay.kt
└── util/
    ├── AudioPlayer.kt          # Plays timer completion sound
    ├── TimerManager.kt         # Countdown timer logic
    └── WorkoutExporter.kt      # CSV export functionality
```

---

## Data Model

### Entity Relationship Diagram

```
WorkoutTemplate (1) ──────< TemplateExercise (N)
                                    │
                                    └──────< TemplateSet (N)

WorkoutSession (1) ──────< ExerciseLog (N)
                                  │
                                  └──────< SetLog (N)
```

### Templates (Blueprints)

Templates are reusable workout blueprints created by the user.

**WorkoutTemplate**
```kotlin
data class WorkoutTemplate(
    val id: Long,
    val name: String,           // e.g., "Push Day"
    val createdAt: Long,
    val updatedAt: Long
)
```

**TemplateExercise** - An exercise within a template
```kotlin
data class TemplateExercise(
    val id: Long,
    val templateId: Long,       // FK to WorkoutTemplate
    val exerciseName: String,   // e.g., "Bench Press"
    val targetSets: Int,        // Default number of sets
    val restSeconds: Int?,      // Default rest timer (null = no timer)
    val showRpe: Boolean,       // Whether to show RPE input
    val orderIndex: Int         // Display order
)
```

**TemplateSet** - Pre-configured set within a template exercise
```kotlin
data class TemplateSet(
    val id: Long,
    val templateExerciseId: Long,  // FK to TemplateExercise
    val setNumber: Int,
    val targetWeight: Int?,
    val targetReps: Int?,
    val restSeconds: Int?,         // Per-set rest override
    val setType: SetType           // REGULAR, WARMUP, or DROP_SET
)
```

### Sessions (Actual Workouts)

Sessions are actual workout instances (in-progress or completed).

**WorkoutSession**
```kotlin
data class WorkoutSession(
    val id: Long,
    val templateId: Long?,      // Optional - null for "quick workouts"
    val templateName: String?,  // Snapshot of template name at workout time
    val startedAt: Long,
    val completedAt: Long?,     // null if in-progress
    val isCompleted: Boolean
)
```

**ExerciseLog** - An exercise performed during a session
```kotlin
data class ExerciseLog(
    val id: Long,
    val sessionId: Long,        // FK to WorkoutSession
    val exerciseName: String,
    val showRpe: Boolean,
    val orderIndex: Int
)
```

**SetLog** - A single set performed
```kotlin
data class SetLog(
    val id: Long,
    val exerciseLogId: Long,    // FK to ExerciseLog
    val setNumber: Int,
    val reps: Int?,             // null = not logged yet
    val weightLbs: Float?,      // Weight in pounds
    val restSeconds: Int?,      // Rest timer duration
    val rpe: Float?,            // 6.0 - 10.0 scale
    val setType: SetType,       // REGULAR, WARMUP, DROP_SET
    val completedAt: Long?      // null = not completed
)
```

### SetType Enum
```kotlin
enum class SetType {
    REGULAR,   // Normal working set - displays as number (1, 2, 3...)
    WARMUP,    // Warmup set - displays as "W"
    DROP_SET   // Drop set - displays as "D"
}
```

### Key Design Decisions

1. **Template name is snapshotted**: When a workout starts, `templateName` is copied to the session. This preserves history even if the template is later renamed or deleted.

2. **Nullable templateId/templateName**: Supports "quick workouts" started without a template.

3. **Cascade deletes**: Deleting a template cascades to its exercises/sets. Deleting a session cascades to its logs.

4. **Weight stored in pounds**: All weights are stored as `Float` in pounds (lbs).

---

## Navigation

### Routes

The app uses Jetpack Navigation Compose with these routes:

| Route | Screen | Description |
|-------|--------|-------------|
| `start_workout` | StartWorkoutScreen | Template picker + quick workout |
| `templates` | TemplatesScreen | List of templates |
| `history` | HistoryScreen | List of completed workouts |
| `active_workout/{sessionId}` | ActiveWorkoutScreen | Active workout logging |
| `template_editor/{templateId}` | TemplateEditorScreen | Create/edit template |
| `workout_detail/{sessionId}` | WorkoutDetailScreen | Read-only view of past workout |

### Bottom Navigation

Three tabs:
1. **Workout** (`start_workout`) - Start a new workout
2. **Templates** (`templates`) - Manage templates
3. **History** (`history`) - View past workouts

### Navigation Flow

```
StartWorkoutScreen
    ├── Select template → ActiveWorkoutScreen
    ├── "Empty Workout" → ActiveWorkoutScreen (no template)
    └── Resume in-progress → ActiveWorkoutScreen

TemplatesScreen
    ├── + FAB → TemplateEditorScreen (new)
    └── Tap template → TemplateEditorScreen (edit)

HistoryScreen
    └── Tap workout → WorkoutDetailScreen

ActiveWorkoutScreen
    ├── Finish → HistoryScreen (navigates to tab)
    └── Cancel → Previous screen

WorkoutDetailScreen
    └── Save as Template → TemplateEditorScreen (pre-populated)
```

### Navigation Implementation

All navigation logic is in `AppNavigation.kt`. Key patterns:

```kotlin
// Navigate to active workout
navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))

// Navigate to history tab after finishing workout
navController.navigate(Screen.History.route) {
    popUpTo(Screen.StartWorkout.route) { inclusive = false }
}
```

---

## Core Flows

### 1. Starting a Workout from Template

**Location**: `AppNavigation.kt` (onStartWorkout lambda)

1. User taps template on StartWorkoutScreen
2. Create new `WorkoutSession` with templateId and templateName snapshot
3. For each `TemplateExercise`:
   - Create `ExerciseLog` linked to session
   - For each `TemplateSet`:
     - Create `SetLog` with pre-filled weight/reps from template
4. Navigate to `ActiveWorkoutScreen` with sessionId

### 2. Logging a Set

**Location**: `ActiveWorkoutState.kt`

1. User enters weight and reps in SetRow
2. `updateSetWeight()` / `updateSetReps()` called
3. SetLog updated in database immediately (auto-save)
4. User taps checkmark to complete set
5. `completeSet()` called:
   - Sets `completedAt` timestamp
   - If `restSeconds > 0`, starts rest timer

### 3. Rest Timer

**Location**: `TimerManager.kt`, `RestTimerDisplay.kt`

1. Timer starts automatically after set completion
2. `TimerState` exposed as `StateFlow` for reactive UI
3. User can:
   - Pause/resume timer
   - Add/subtract 5 seconds (with long-press repeat)
   - Skip timer entirely
4. On completion:
   - `AudioPlayer.playNotificationSound()` plays through media stream
   - Timer UI disappears

### 4. Finishing a Workout

**Location**: `ActiveWorkoutState.kt`, `AppNavigation.kt`

1. User taps "Finish" button
2. Confirmation dialog shown
3. `finishWorkout()` called:
   - Sets `isCompleted = true`
   - Sets `completedAt` timestamp
4. Navigate to History tab

### 5. Crash Recovery

**Location**: `StartWorkoutScreen.kt`, `SessionDao.kt`

1. On app start, `getActiveSession()` queries for incomplete sessions
2. If found, resume banner shown on StartWorkoutScreen
3. Tapping banner navigates to ActiveWorkoutScreen with existing sessionId
4. All previously logged sets are preserved

### 6. CSV Export

**Location**: `WorkoutExporter.kt`, `HistoryScreen.kt`

1. User taps export icon in History screen
2. Dropdown shows "Share" or "Save to Files"
3. `getAllCompletedSessionsWithDetails()` fetches all data
4. `generateCsv()` creates denormalized CSV (one row per set)
5. For Share: Opens Android share sheet
6. For Save: Opens system file picker via `ActivityResultContracts.CreateDocument`

---

## Key Components

### ExerciseCard (`ui/components/ExerciseCard.kt`)

Displays a single exercise with its sets during a workout.

**Props**:
- `exerciseName: String`
- `sets: List<SetData>` - Set information
- `onSetWeightChange`, `onSetRepsChange`, etc. - Callbacks
- `showRpe: Boolean` - Whether to show RPE input

**Features**:
- Collapsible (tap header to expand/collapse)
- Move up/down buttons for reordering
- Add/remove set buttons

### SetRow (`ui/components/SetRow.kt`)

A single set row with weight, reps, and completion checkbox.

**Features**:
- Set number badge (tappable to cycle through REGULAR/WARMUP/DROP_SET)
- Weight input field (numeric)
- Reps input field (numeric)
- Optional RPE input
- Completion checkbox
- Rest timer time display (tappable to change)
- Previous workout values shown as placeholders

### ExercisePicker (`ui/components/ExercisePicker.kt`)

Bottom sheet for selecting exercises from the library.

**Features**:
- Search field
- Horizontal scrolling category filter chips
- Exercise list filtered by search and category
- Tap to select, returns exercise to parent

### RestTimerDisplay (`ui/components/RestTimerDisplay.kt`)

Circular countdown timer overlay.

**Features**:
- Circular progress indicator
- Time remaining in center
- -5s / +5s buttons with long-press repeat
- Pause/resume button
- Skip (X) button

---

## Utilities

### TimerManager (`util/TimerManager.kt`)

Manages countdown timer state using Kotlin coroutines.

```kotlin
class TimerManager(
    private val scope: CoroutineScope,
    private val onComplete: (() -> Unit)? = null
) {
    val timerState: StateFlow<TimerState>
    
    fun start(seconds: Int)
    fun pause()
    fun resume()
    fun cancel()
    fun addTime(seconds: Int)
    fun subtractTime(seconds: Int)
    fun skip()
}
```

**TimerState**:
```kotlin
data class TimerState(
    val isRunning: Boolean,
    val remainingSeconds: Int,
    val totalSeconds: Int
) {
    val progress: Float      // 0.0 to 1.0
    val isComplete: Boolean
    val formattedTime: String  // "1:30"
}
```

### AudioPlayer (`util/AudioPlayer.kt`)

Plays timer completion sounds.

**Key features**:
- Uses `MediaPlayer` with `USAGE_MEDIA` to bypass Do Not Disturb
- Plays system notification sound (short, ~2 seconds)
- Auto-stops after 2.5 seconds maximum
- Proper cleanup to prevent memory leaks

### WorkoutExporter (`util/WorkoutExporter.kt`)

Exports workout data to CSV.

**CSV Format**:
```
date,workout_name,exercise,primary_muscle,auxiliary_muscles,set_number,weight,reps,rpe,set_type
2024-01-15,Push Day,Bench Press,Chest,Triceps|Shoulders,1,185,8,7.5,REGULAR
```

**Methods**:
- `generateCsv(sessions: List<SessionWithDetails>): String`
- `generateFileName(): String` - e.g., "workout_export_20241215_143022.csv"
- `createCsvFile(context, csvContent): File`
- `createShareIntent(context, file): Intent`

---

## State Management

### ActiveWorkoutState (`ui/screens/ActiveWorkoutState.kt`)

The most complex state holder in the app. Manages all state for an active workout.

```kotlin
class ActiveWorkoutState(
    private val sessionId: Long,
    private val database: AppDatabase,
    private val scope: CoroutineScope,
    private val context: Context
) {
    // Exposed state
    val session: StateFlow<WorkoutSession?>
    val exercises: StateFlow<List<ActiveExercise>>
    val timerState: StateFlow<TimerState>
    val showExercisePicker: StateFlow<Boolean>
    
    // Actions
    fun updateSetWeight(exerciseIndex, setNumber, weight)
    fun updateSetReps(exerciseIndex, setNumber, reps)
    fun updateSetRest(exerciseIndex, setNumber, restSeconds)
    fun updateSetType(exerciseIndex, setNumber, setType)
    fun updateSetRpe(exerciseIndex, setNumber, rpe)
    fun completeSet(exerciseIndex, setNumber)
    fun addSet(exerciseIndex)
    fun removeSet(exerciseIndex, setNumber)
    fun addExercise(exercise)
    fun reorderExercises(fromIndex, toIndex)
    fun finishWorkout()
    // Timer controls
    fun pauseTimer()
    fun resumeTimer()
    fun skipTimer()
    fun addTimerTime()
    fun subtractTimerTime()
}
```

### Data Flow

```
User Action → State Update → Database Write → UI Recomposition
                 ↓
         StateFlow.value = newState
                 ↓
         Compose collects Flow
                 ↓
         UI updates automatically
```

### ActiveExercise / ActiveSet

In-memory representations used during active workout:

```kotlin
data class ActiveExercise(
    val exerciseLog: ExerciseLog,
    val exerciseName: String,
    val restSeconds: Int?,
    val showRpe: Boolean,
    val sets: List<ActiveSet>,
    val isExpanded: Boolean
)

data class ActiveSet(
    val setLog: SetLog,         // Underlying database entity
    val setNumber: Int,
    val weight: Int?,
    val reps: Int?,
    val rpe: Float?,
    val isCompleted: Boolean,
    val previousWeight: Int?,   // From last workout (for placeholders)
    val previousReps: Int?,
    val restSeconds: Int?,
    val setType: SetType
)
```

---

## Common Patterns

### 1. Database Access

Always access database through DAOs. Database instance from `WorkoutApp`:

```kotlin
val database = (context.applicationContext as WorkoutApp).database
val sessions = database.sessionDao().getCompletedSessions()
```

### 2. Coroutine Scope

Use `rememberCoroutineScope()` in Compose for launching coroutines:

```kotlin
val scope = rememberCoroutineScope()
scope.launch {
    database.sessionDao().updateSetLog(setLog)
}
```

### 3. State in Compose

Use `collectAsState()` to collect Flows:

```kotlin
val sessions by database.sessionDao().getCompletedSessions().collectAsState(initial = emptyList())
```

### 4. Navigation with Arguments

```kotlin
// Define route with argument
object ActiveWorkout : Screen("active_workout/{sessionId}") {
    fun createRoute(sessionId: Long) = "active_workout/$sessionId"
}

// Navigate
navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))

// Extract argument
composable(
    route = Screen.ActiveWorkout.route,
    arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
) { backStackEntry ->
    val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
}
```

### 5. Confirmation Dialogs

```kotlin
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Confirm") },
        text = { Text("Are you sure?") },
        confirmButton = {
            TextButton(onClick = { /* action */ }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

### 6. Room Transactions

For operations that span multiple tables:

```kotlin
@Transaction
suspend fun startWorkoutFromTemplate(templateId: Long): Long {
    // Create session
    // Create exercise logs
    // Create set logs
}
```

---

## Exercise Library

The static exercise library is in `ExerciseLibrary.kt`. Key points:

- ~100 exercises across 17 muscle categories
- Each exercise has:
  - `name: String`
  - `primaryMuscle: String` - Main muscle worked
  - `auxiliaryMuscles: List<String>` - Secondary muscles
  - `isBodyweight: Boolean`

```kotlin
val exercises: List<Exercise> = listOf(
    Exercise("Bench Press", "Chest", listOf("Triceps", "Shoulders")),
    Exercise("Pull-ups", "Back", listOf("Biceps", "Forearms"), isBodyweight = true),
    // ...
)
```

**Helper methods**:
- `getByCategory(category)` - Filter by primary muscle
- `getByMuscle(muscle)` - Filter by any muscle (primary or auxiliary)
- `getByName(name)` - Exact name lookup
- `search(query)` - Fuzzy name search

---

## Database Versioning

Current version: **5**

If you need to modify the schema:

1. Update the entity classes
2. Increment `version` in `AppDatabase.kt`
3. Add migration or use `fallbackToDestructiveMigration()` (dev only)

```kotlin
@Database(
    entities = [...],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // ...
}
```

---

## Debugging Tips

1. **Database inspection**: Use Android Studio's Database Inspector
2. **Flow debugging**: Add `.onEach { Log.d("TAG", it.toString()) }` before collecting
3. **Recomposition tracking**: Use Layout Inspector or add log statements
4. **Timer issues**: Check that `restSeconds` is properly passed to `ActiveSet`

---

## Future Considerations

Areas that could be improved:

1. **Unit tests**: Currently no automated tests
2. **ViewModel**: Could extract state from Navigation into proper ViewModels
3. **Dependency injection**: Could add Hilt/Koin for better testability
4. **Backup/sync**: Cloud backup or cross-device sync
5. **Charts/analytics**: Visual progress tracking over time
6. **Exercise customization**: Allow users to add custom exercises
