# Tasks: Workout Tracking App

**Input**: Design documents from `/specs/001-workout-tracking/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/ ✓

**Tests**: Tests are OPTIONAL per constitution (Principle III: Minimal Testing). Manual testing during development is acceptable.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Base path: `app/src/main/java/com/workout/app/`

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Create Android project and configure dependencies

- [x] T001 Create Android project in Android Studio with Compose template (name: WorkoutTracker, package: com.workout.app, minSdk: 26)
- [x] T002 Update app/build.gradle.kts with Room, Navigation, and Material3 dependencies per quickstart.md
- [x] T003 [P] Create directory structure: data/entities/, data/dao/, ui/theme/, ui/navigation/, ui/screens/, ui/components/, util/
- [x] T004 [P] Add timer_complete.mp3 audio file to app/src/main/res/raw/ (use short chime sound, ~1 second, from freesound.org CC0 or generate with Android ToneGenerator as fallback)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Entities

- [x] T005 [P] Create WorkoutTemplate entity in app/src/main/java/com/workout/app/data/entities/WorkoutTemplate.kt
- [x] T006 [P] Create TemplateExercise entity in app/src/main/java/com/workout/app/data/entities/TemplateExercise.kt
- [x] T007 [P] Create WorkoutSession entity in app/src/main/java/com/workout/app/data/entities/WorkoutSession.kt
- [x] T008 [P] Create ExerciseLog entity in app/src/main/java/com/workout/app/data/entities/ExerciseLog.kt
- [x] T009 [P] Create SetLog entity in app/src/main/java/com/workout/app/data/entities/SetLog.kt

### DAOs and Database

- [x] T010 Create TemplateDao with CRUD operations in app/src/main/java/com/workout/app/data/dao/TemplateDao.kt
- [x] T011 Create SessionDao with CRUD operations in app/src/main/java/com/workout/app/data/dao/SessionDao.kt
- [x] T012 Create AppDatabase with all entities and DAOs in app/src/main/java/com/workout/app/data/AppDatabase.kt

### Static Data

- [x] T013 [P] Create ExerciseLibrary with ~50 exercises across 9 categories (Chest, Back, Shoulders, Biceps, Triceps, Legs, Core, Full Body, Cardio) in app/src/main/java/com/workout/app/data/ExerciseLibrary.kt. Include Exercise data class with name, category, and isBodyweight fields.

### Theme and Navigation Shell

- [x] T014 [P] Create Material3 theme (Color.kt, Type.kt, Theme.kt) in app/src/main/java/com/workout/app/ui/theme/
- [x] T015 Create AppNavigation with bottom nav (3 tabs) and all routes in app/src/main/java/com/workout/app/ui/navigation/AppNavigation.kt
- [x] T016 Update MainActivity.kt to use AppNavigation with bottom navigation bar
- [x] T017 [P] Create WorkoutApp.kt Application class with database singleton in app/src/main/java/com/workout/app/WorkoutApp.kt

### Utilities

- [x] T018 [P] Create AudioPlayer utility for timer sounds in app/src/main/java/com/workout/app/util/AudioPlayer.kt
- [x] T019 [P] Create TimerManager for countdown timer logic in app/src/main/java/com/workout/app/util/TimerManager.kt

**Checkpoint**: Foundation ready - build and run app, verify bottom nav with 3 placeholder screens appears

---

## Phase 3: User Story 1 - Execute Workout from Template (Priority: P1) 🎯 MVP

**Goal**: User can select a template, log sets with reps/weight, and finish workout with data persisted

**Independent Test**: Start workout from template → log 2-3 sets → finish → restart app → verify workout in history

**Note**: For MVP testing, seed one hardcoded template in database or use Phase 4 (Templates) first

### Shared Components

- [x] T020 [P] [US1] Create SetRow component (inline reps/weight fields) in app/src/main/java/com/workout/app/ui/components/SetRow.kt
- [x] T021 [P] [US1] Create ExerciseCard component (exercise name + list of SetRows) in app/src/main/java/com/workout/app/ui/components/ExerciseCard.kt
- [x] T022 [P] [US1] Create RestTimer overlay component in app/src/main/java/com/workout/app/ui/components/RestTimer.kt

### Screens

- [x] T023 [US1] Create StartWorkoutScreen (template picker with "Create Template" button linking to Templates tab) in app/src/main/java/com/workout/app/ui/screens/StartWorkoutScreen.kt. Show resume banner if in-progress workout exists.
- [x] T024 [US1] Create ActiveWorkoutScreen with exercise list and set logging in app/src/main/java/com/workout/app/ui/screens/ActiveWorkoutScreen.kt

### Core Functionality

- [x] T025 [US1] Implement start workout flow: create WorkoutSession from template, populate ExerciseLogs and SetLogs
- [x] T026 [US1] Implement set logging: update reps/weight in SetLog, auto-save to Room immediately on each set completion (FR-010). Handle null reps as skipped set (save but don't trigger timer).
- [x] T027 [US1] Implement rest timer: auto-start after set logged (FR-021), countdown with audio cue on finish (FR-022). Allow user to tap timer to modify duration mid-countdown (FR-020). Skip button dismisses timer early.
- [x] T028 [US1] Implement finish workout: set isCompleted=true, completedAt timestamp, navigate to history
- [x] T029 [US1] Implement crash recovery: detect in-progress session on app start, show resume banner

**Checkpoint**: User Story 1 complete - can track a full workout with sets, timer, and persistence

---

## Phase 4: User Story 2 - Create and Manage Templates (Priority: P2)

**Goal**: User can create, edit, delete, and reorder exercises in workout templates

**Independent Test**: Create template with 3 exercises → edit (rename, add exercise, reorder) → delete → verify CRUD works

### Shared Components

- [x] T030 [P] [US2] Create ExercisePicker component (search/select from library) in app/src/main/java/com/workout/app/ui/components/ExercisePicker.kt

### Screens

- [x] T031 [US2] Create TemplatesScreen (list of templates with swipe-to-delete) in app/src/main/java/com/workout/app/ui/screens/TemplatesScreen.kt
- [x] T032 [US2] Create TemplateEditorScreen (name, exercises, sets, rest timer per exercise) in app/src/main/java/com/workout/app/ui/screens/TemplateEditorScreen.kt

### Core Functionality

- [x] T033 [US2] Implement create template: save name and exercises to Room
- [x] T034 [US2] Implement edit template: load existing, modify, save changes
- [x] T035 [US2] Implement delete template: confirm dialog, cascade delete exercises. Verify: past workouts using this template retain their data (templateName snapshot preserved).
- [x] T036 [US2] Implement exercise reordering: drag-and-drop in TemplateEditorScreen
- [x] T037 [US2] Implement rest timer configuration: optional seconds input per exercise

**Checkpoint**: User Story 2 complete - full template CRUD with reordering works

---

## Phase 5: User Story 3 - View Workout History (Priority: P3)

**Goal**: User can view past workouts in a list and drill into details

**Independent Test**: Complete 2 workouts → view history list → tap one → see all exercises/sets/weights

### Screens

- [x] T038 [US3] Create HistoryScreen (list of completed workouts, reverse chronological) in app/src/main/java/com/workout/app/ui/screens/HistoryScreen.kt
- [x] T039 [US3] Create WorkoutDetailScreen (read-only view of past workout) in app/src/main/java/com/workout/app/ui/screens/WorkoutDetailScreen.kt

### Core Functionality

- [x] T040 [US3] Implement history list: query completed sessions, display date/name/duration
- [x] T041 [US3] Implement workout detail: load session with exercises and sets, display read-only

**Checkpoint**: User Story 3 complete - can browse and view all past workout details

---

## Phase 6: User Story 4 - Quick Add Exercise During Workout (Priority: P4)

**Goal**: User can add exercises on-the-fly during an active workout

**Independent Test**: Start workout → add new exercise not in template → log sets → finish → verify it saved

### Core Functionality

- [x] T042 [US4] Add "Add Exercise" FAB to ActiveWorkoutScreen
- [x] T043 [US4] Implement add exercise flow: open ExercisePicker, create new ExerciseLog with empty sets
- [x] T044 [US4] Implement exercise reordering during active workout (drag-and-drop)
- [x] T045 [US4] Implement add/remove sets during active workout (per exercise)

**Checkpoint**: User Story 4 complete - full workout flexibility with on-the-fly modifications

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T046 [P] Add empty state UI for StartWorkoutScreen (no templates)
- [x] T047 [P] Add empty state UI for HistoryScreen (no workouts)
- [x] T048 [P] Add empty state UI for TemplatesScreen (no templates)
- [x] T049 Add confirmation dialogs for destructive actions (delete template, cancel workout)
- [x] T050 Polish UI: consistent spacing, typography, colors per Material3
- [ ] T051 Verify all screens work with keyboard (numeric inputs don't cover fields)
- [ ] T052 Test with large data: 365+ workout sessions, verify performance
- [ ] T053 Update app icon and splash screen (optional)

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup ─────────────────────┐
                                    ▼
Phase 2: Foundational ──────────────┤ (BLOCKS all user stories)
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
Phase 3: US1 (P1)          Phase 4: US2 (P2)          Phase 5: US3 (P3)
Execute Workout            Manage Templates           View History
         │                          │                          │
         └──────────────────────────┼──────────────────────────┘
                                    ▼
                           Phase 6: US4 (P4)
                           Quick Add Exercise
                                    │
                                    ▼
                           Phase 7: Polish
```

### Recommended Order (Solo Developer)

1. **Phase 1 + 2**: Setup and Foundation (~1-2 hours)
2. **Phase 4 (US2)**: Templates first—needed to test US1 properly
3. **Phase 3 (US1)**: Execute workout—core value
4. **Phase 5 (US3)**: History—verify persistence works
5. **Phase 6 (US4)**: Quick add—flexibility feature
6. **Phase 7**: Polish—as time permits

### Parallel Opportunities

Within each phase, tasks marked [P] can be done in parallel:
- T005-T009: All 5 entities in parallel
- T014, T017-T019: Theme, App class, utilities in parallel
- T020-T022: All 3 components in parallel

---

## Parallel Example: Foundation Entities

```
# These 5 entity files can all be created simultaneously:
T005: WorkoutTemplate.kt
T006: TemplateExercise.kt
T007: WorkoutSession.kt
T008: ExerciseLog.kt
T009: SetLog.kt
```

---

## Implementation Strategy

### MVP (Minimum Viable Product)

Complete these phases for a working app:
1. ✅ Phase 1: Setup
2. ✅ Phase 2: Foundation
3. ✅ Phase 4: Templates (US2) - need templates before workouts
4. ✅ Phase 3: Execute Workout (US1) - core tracking

**MVP Delivers**: Create templates, run workouts, log sets with timer

### Full Feature Set

Add remaining phases:
5. Phase 5: History (US3)
6. Phase 6: Quick Add (US4)
7. Phase 7: Polish

---

## Notes

- All file paths relative to `app/src/main/java/com/workout/app/`
- Manual testing after each phase checkpoint
- Commit after completing each phase
- Constitution: Favor simplicity, no over-engineering
- If stuck on a task, simplify and move on—iterate later
