# Implementation Plan: Workout Tracking App

**Branch**: `001-workout-tracking` | **Date**: 2025-12-03 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-workout-tracking/spec.md`

## Summary

Build a personal Android workout tracking app that allows creating workout templates, executing workouts with set/rep/weight logging, viewing history, and using rest timers with audio cues. The app uses modern Material Design 3, stores all data locally with Room, and prioritizes simplicity and reliability over architectural elegance.

## Technical Context

**Language/Version**: Kotlin 1.9+ with Android SDK  
**Primary Dependencies**: Jetpack Compose (UI), Room (database), Material Design 3 (theming)  
**Storage**: Room with SQLite (local only, no cloud sync)  
**Testing**: Manual testing primary; optional unit tests for timer/calculation logic  
**Target Platform**: Android API 26+ (Android 8.0 Oreo and above)  
**Project Type**: Mobile (single Android app)  
**Performance Goals**: App launch < 3 seconds, set logging < 5 seconds (per SC-002, SC-003)  
**Constraints**: Fully offline, single user, data must survive app crashes  
**Scale/Scope**: ~6 screens, 365+ workout records (1 year), static exercise library (~50-100 exercises)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| **I. Simplicity First** | Favor simplest solution; YAGNI; prefer built-in components | ✅ PASS |
| **II. It Just Works** | Offline-capable; reliable persistence; crash-free; usable with sweaty hands | ✅ PASS |
| **III. Minimal Testing** | Manual testing OK; automated tests optional | ✅ PASS |

**Gate Evaluation**:

1. **Simplicity First**: 
   - Using Jetpack Compose (modern, less boilerplate than XML)
   - Room is the standard Android persistence solution (built-in)
   - No complex architecture patterns (no Clean Architecture, no multi-module)
   - Single activity with Compose navigation
   - ✅ No violations

2. **It Just Works**:
   - Room provides reliable local persistence
   - Auto-save during workout addresses crash recovery (FR-010)
   - Inline editing with large tap targets for sweaty hands
   - Offline-only by design (FR-011)
   - ✅ No violations

3. **Minimal Testing**:
   - Plan relies on manual testing during development
   - Optional unit tests only for timer logic if needed
   - No UI tests planned
   - ✅ No violations

**Result**: All gates pass. No complexity justification needed.

## Project Structure

### Documentation (this feature)

```text
specs/001-workout-tracking/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (screen navigation contracts)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/workout/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── WorkoutApp.kt
│   │   │   ├── data/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── entities/
│   │   │   │   │   ├── WorkoutTemplate.kt
│   │   │   │   │   ├── TemplateExercise.kt
│   │   │   │   │   ├── WorkoutSession.kt
│   │   │   │   │   ├── ExerciseLog.kt
│   │   │   │   │   └── SetLog.kt
│   │   │   │   ├── dao/
│   │   │   │   │   ├── TemplateDao.kt
│   │   │   │   │   └── SessionDao.kt
│   │   │   │   └── ExerciseLibrary.kt      # Static exercise data (hardcoded, no DAO)
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── AppNavigation.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── StartWorkoutScreen.kt
│   │   │   │   │   ├── ActiveWorkoutScreen.kt
│   │   │   │   │   ├── TemplatesScreen.kt
│   │   │   │   │   ├── TemplateEditorScreen.kt
│   │   │   │   │   ├── HistoryScreen.kt
│   │   │   │   │   └── WorkoutDetailScreen.kt
│   │   │   │   └── components/
│   │   │   │       ├── ExerciseCard.kt
│   │   │   │       ├── SetRow.kt
│   │   │   │       ├── RestTimer.kt
│   │   │   │       └── ExercisePicker.kt
│   │   │   └── util/
│   │   │       ├── TimerManager.kt
│   │   │       └── AudioPlayer.kt
│   │   ├── res/
│   │   │   ├── raw/
│   │   │   │   └── timer_complete.mp3      # Audio cue
│   │   │   └── values/
│   │   │       └── strings.xml
│   │   └── AndroidManifest.xml
│   └── test/                               # Optional unit tests
│       └── java/com/workout/app/
│           └── TimerManagerTest.kt
├── build.gradle.kts
└── gradle/
```

**Structure Decision**: Single Android app module with Jetpack Compose. No multi-module architecture—keeps build simple and iteration fast. All code in one `app/` module following standard Android project layout.

## Complexity Tracking

> No violations to justify. All decisions align with constitution principles.
