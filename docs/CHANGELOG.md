# Changelog

All notable changes to the Workout Tracking App beyond the original MVP tasks.

## Post-MVP Enhancements

### Exercise Library Expansion
- Expanded from 9 categories to **17 muscle categories**: Chest, Back, Lower Back, Trapezius, Shoulders, Biceps, Triceps, Forearms, Quads, Hamstrings, Glutes, Calves, Abductors, Adductors, Core, Full Body, Cardio
- Added **auxiliary muscle groups** to exercises - each exercise now has a `primaryMuscle` and optional `auxiliaryMuscles` list (e.g., Bench Press targets Chest primarily, with Triceps and Shoulders as auxiliary)
- Added `getByName()` helper method for exercise lookup

### Set Types (RPE, Warmup, Drop Sets)
- Added **SetType enum**: REGULAR, WARMUP, DROP_SET
- Sets display as numbers (1, 2, 3) for regular sets, "W" for warmup, "D" for drop sets
- Tappable set number badge cycles through types
- Color coding: Cyan for regular, Orange for warmup, Red for drop sets

### RPE (Rate of Perceived Exertion) Tracking
- Added optional RPE field (6.0-10.0 scale) per set
- RPE can be enabled/disabled per exercise in templates
- Visual RPE input with half-point increments

### Navigation & UX Fixes
- Fixed navigation bug where completing a workout incorrectly redirected back to workout screen instead of History
- **Empty workout support**: Users can start a workout without selecting a template ("Quick Workout")
- Made `templateName` nullable in database (schema version 5)
- Removed FAB from Workout screen (kept only on Templates screen)

### Save Workout as Template
- Added ability to save a completed workout from History as a new template
- Download/save icon in WorkoutDetailScreen top bar

### CSV Data Export
- Export all workout data to CSV for external analysis (spreadsheets, fractional set counting)
- **Two export options**: Share (via Android share sheet) or Save to Files (direct file system save)
- CSV format: `date, workout_name, exercise, primary_muscle, auxiliary_muscles, set_number, weight, reps, rpe, set_type`
- Added FileProvider configuration for secure file sharing

### Scrollable Category Filter
- Changed exercise category chips from Row to **LazyRow** for horizontal scrolling (needed with 17 categories)

### Rest Timer Improvements
- **Triggers for all set types**: Fixed bug where warmup sets didn't trigger timer (ActiveSet wasn't getting restSeconds passed through)
- **Media stream audio**: Timer completion sound now plays through media stream instead of notification stream, bypassing Do Not Disturb and vibrate mode
- **Short notification sound**: Changed from alarm (long/looping) to notification sound with 2.5s max duration
- **Long-press repeat on +5/-5 buttons**: Hold to rapidly add/subtract time (400ms initial delay, then 100ms repeat interval)

### Workout Duration Timer
- Added elapsed time display in ActiveWorkoutScreen top bar
- Shows `M:SS` format (or `H:MM:SS` for workouts over an hour)
- Updates every second during active workout
- Subtle pill-shaped badge with clock icon next to workout title

---

## Technical Changes

### Database Changes
- **Version 5**: `templateName` made nullable in `WorkoutSession` entity
- Added `getAllCompletedSessionsWithDetails()` to SessionDao for CSV export

### New Files Created
- `app/src/main/java/com/workout/app/util/WorkoutExporter.kt` - CSV export utility
- `app/src/main/res/xml/file_paths.xml` - FileProvider paths configuration
- `docs/CHANGELOG.md` - This file

### Manifest Changes
- Added FileProvider for secure file sharing

### Modified Files
- `ExerciseLibrary.kt` - Expanded categories, auxiliary muscles, getByName()
- `SetLog.kt` - Added SetType enum
- `ExerciseLog.kt` - Added showRpe field
- `TemplateExercise.kt` - Added showRpe field
- `WorkoutSession.kt` - Made templateName nullable
- `AppDatabase.kt` - Version bumped to 5
- `SessionDao.kt` - Added export query
- `ActiveWorkoutState.kt` - Fixed restSeconds passing, set type support
- `ActiveWorkoutScreen.kt` - Added workout duration timer
- `HistoryScreen.kt` - Added export menu (Share/Save to Files)
- `WorkoutDetailScreen.kt` - Added save as template button
- `StartWorkoutScreen.kt` - Added empty workout support, removed FAB
- `ExercisePicker.kt` - Horizontal scrolling categories
- `SetRow.kt` - Set type cycling, RPE input
- `ExerciseCard.kt` - Set type and RPE support
- `RestTimerDisplay.kt` - Long-press repeat on +5/-5 buttons
- `AudioPlayer.kt` - Media stream audio, short notification sound
- `AppNavigation.kt` - Navigation fixes, export wiring, save as template
- `AndroidManifest.xml` - FileProvider configuration
