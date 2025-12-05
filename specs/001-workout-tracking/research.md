# Research: Workout Tracking App

**Feature**: 001-workout-tracking  
**Date**: 2025-12-03  
**Purpose**: Resolve technical decisions and validate approach before design phase

## Research Tasks Completed

### 1. Jetpack Compose for Modern UI

**Decision**: Use Jetpack Compose with Material Design 3

**Rationale**: 
- Compose is the modern Android UI toolkit, officially recommended by Google
- Material Design 3 (Material You) provides modern, clean aesthetics out of the box
- Declarative UI reduces boilerplate significantly vs XML layouts
- Built-in support for theming, animations, and responsive layouts
- Large tap targets and touch-friendly components available by default

**Alternatives Considered**:
- XML layouts with View system: More verbose, harder to maintain, older technology
- Flutter: Requires learning Dart, overkill for single-platform personal project

### 2. Room for Local Persistence

**Decision**: Use Room database with SQLite

**Rationale**:
- Room is Android's official persistence library (part of Jetpack)
- Compile-time SQL verification prevents runtime errors
- Built-in support for Kotlin coroutines and Flow for reactive updates
- Simple annotation-based entity definitions
- Handles schema migrations gracefully
- No external dependencies required

**Alternatives Considered**:
- Raw SQLite: More code, no compile-time safety
- Realm: Third-party dependency, more complex than needed
- SharedPreferences: Not suitable for relational workout data

### 3. Navigation Architecture

**Decision**: Single Activity with Compose Navigation

**Rationale**:
- Compose Navigation handles all screen transitions
- Bottom navigation bar with 3 destinations maps directly to FR-013
- Type-safe navigation with route arguments
- No Fragment complexity

**Navigation Structure**:
```
Bottom Tabs:
├── Start Workout → Template picker → Active Workout
├── Templates → Template List → Template Editor  
└── History → Workout List → Workout Detail
```

### 4. Rest Timer Implementation

**Decision**: Use `CountDownTimer` with `MediaPlayer` for audio

**Rationale**:
- `CountDownTimer` is built-in Android class, simple to use
- `MediaPlayer` handles audio playback for timer completion sound
- No background service needed—timer runs in foreground during active workout
- Timer state saved with workout auto-save for crash recovery

**Alternatives Considered**:
- WorkManager: Overkill for foreground countdown
- Handler/Runnable loops: More code, same result
- Third-party timer libraries: Unnecessary dependency

### 5. Drag-and-Drop for Exercise Reordering

**Decision**: Use Compose's built-in `LazyColumn` with drag-to-reorder

**Rationale**:
- `LazyColumn` with `animateItemPlacement` handles smooth reordering
- Simple drag handle icon per item
- State updates on drop, auto-saves to Room
- Works for both template editing and active workout modification

**Implementation Notes**:
- Use `rememberReorderableLazyListState` from accompanist library OR
- Simple manual implementation with `detectDragGestures`
- Keep it simple—if built-in doesn't work well, use a single well-maintained library

### 6. Static Exercise Library

**Decision**: Hardcode exercise list as Kotlin data in `ExerciseLibrary.kt`

**Rationale**:
- Static data (~50-100 exercises) doesn't need database storage
- Faster load time than database query
- Easy to edit and maintain
- Categories: Chest, Back, Shoulders, Arms, Legs, Core, Cardio
- Each exercise has: name, primary muscle group, optional secondary muscle group

**Alternatives Considered**:
- JSON file in assets: Extra parsing step, no benefit for static data
- Room table: Overkill for read-only data that never changes
- Remote API: Violates offline-only constraint

### 7. Auto-Save Strategy

**Decision**: Save workout state to Room on every set completion

**Rationale**:
- Set completion is natural save point (user just finished an action)
- Room operations are fast enough for single-record updates
- On crash recovery: Load incomplete workout from Room, offer to resume
- Simple boolean flag `isCompleted` differentiates in-progress vs finished workouts

**Implementation**:
- `WorkoutSession` entity has `isCompleted: Boolean` field
- Active workout queries for `isCompleted = false` on app start
- Finishing workout sets `isCompleted = true`

### 8. Weight Input UX

**Decision**: Numeric keyboard with quick-increment buttons

**Rationale**:
- Show numeric soft keyboard for weight/reps input
- Add +/- buttons for common increments (5 lbs for weight, 1 for reps)
- Large touch targets for gym environment (sweaty hands)
- No decimal input needed for pounds (whole numbers sufficient)

## Open Questions (None)

All technical decisions resolved. Ready for Phase 1 design.

## Dependencies Summary

| Dependency | Purpose | Justification |
|------------|---------|---------------|
| Jetpack Compose | UI framework | Modern, less code, built-in |
| Compose Navigation | Screen navigation | Part of Compose, standard approach |
| Room | Local database | Standard Android persistence |
| Material Design 3 | Theming/Components | Modern look, built-in |
| (Optional) Accompanist | Drag-reorder | Only if built-in Compose insufficient |

**Total third-party libraries**: 0-1 (all others are Jetpack/Google official)
