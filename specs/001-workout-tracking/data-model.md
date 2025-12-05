# Data Model: Workout Tracking App

**Feature**: 001-workout-tracking  
**Date**: 2025-12-03  
**Database**: Room (SQLite)

## Entity Relationship Diagram

```
┌─────────────────────┐       ┌──────────────────────┐
│  WorkoutTemplate    │       │  TemplateExercise    │
├─────────────────────┤       ├──────────────────────┤
│ id: Long (PK)       │──┐    │ id: Long (PK)        │
│ name: String        │  │    │ templateId: Long (FK)│──┐
│ createdAt: Long     │  └───<│ exerciseName: String │  │
│ updatedAt: Long     │       │ targetSets: Int      │  │
└─────────────────────┘       │ restSeconds: Int?    │  │
                              │ orderIndex: Int      │  │
                              └──────────────────────┘  │
                                                        │
┌─────────────────────┐       ┌──────────────────────┐  │
│  WorkoutSession     │       │  ExerciseLog         │  │
├─────────────────────┤       ├──────────────────────┤  │
│ id: Long (PK)       │──┐    │ id: Long (PK)        │  │
│ templateId: Long?   │──│───>│ sessionId: Long (FK) │──│
│ templateName: String│  │    │ exerciseName: String │  │
│ startedAt: Long     │  └───<│ orderIndex: Int      │  │
│ completedAt: Long?  │       └──────────────────────┘  │
│ isCompleted: Boolean│                │                │
└─────────────────────┘                │                │
                                       ▼                │
                              ┌──────────────────────┐  │
                              │  SetLog              │  │
                              ├──────────────────────┤  │
                              │ id: Long (PK)        │  │
                              │ exerciseLogId: Long  │──┘
                              │ setNumber: Int       │
                              │ reps: Int?           │
                              │ weightLbs: Float?    │
                              │ restSeconds: Int?    │
                              │ completedAt: Long?   │
                              └──────────────────────┘
```

## Entities

### WorkoutTemplate

Reusable workout blueprint created by the user.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Primary key, auto-generated |
| `name` | String | No | Template name (e.g., "Push Day", "Leg Day") |
| `createdAt` | Long | No | Unix timestamp of creation |
| `updatedAt` | Long | No | Unix timestamp of last modification |

**Constraints**:
- `name` must be non-empty
- `name` should be unique (soft constraint, UI warning)

---

### TemplateExercise

Single exercise within a template.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Primary key, auto-generated |
| `templateId` | Long | No | Foreign key to WorkoutTemplate |
| `exerciseName` | String | No | Exercise name from library or custom |
| `targetSets` | Int | No | Number of sets to perform (default: 3) |
| `restSeconds` | Int | Yes | Default rest duration after each set (null = no timer) |
| `orderIndex` | Int | No | Display order within template (0-based) |

**Constraints**:
- `targetSets` must be >= 1
- `restSeconds` must be > 0 if specified
- `orderIndex` must be unique within a template
- CASCADE DELETE when parent template is deleted

---

### WorkoutSession

A single workout instance (completed or in-progress).

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Primary key, auto-generated |
| `templateId` | Long | Yes | FK to template used (null if custom workout) |
| `templateName` | String | No | Snapshot of template name at workout start |
| `startedAt` | Long | No | Unix timestamp when workout started |
| `completedAt` | Long | Yes | Unix timestamp when workout finished (null if in-progress) |
| `isCompleted` | Boolean | No | False during workout, true when finished |

**Constraints**:
- `templateName` is stored to preserve history if template is later deleted
- Only one session with `isCompleted = false` should exist at a time
- `templateId` is nullable to support future "empty workout" feature

---

### ExerciseLog

Record of an exercise performed during a session.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Primary key, auto-generated |
| `sessionId` | Long | No | Foreign key to WorkoutSession |
| `exerciseName` | String | No | Exercise performed |
| `orderIndex` | Int | No | Display order within workout (0-based) |

**Constraints**:
- `orderIndex` must be unique within a session
- CASCADE DELETE when parent session is deleted

---

### SetLog

Single set performed within an exercise.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Primary key, auto-generated |
| `exerciseLogId` | Long | No | Foreign key to ExerciseLog |
| `setNumber` | Int | No | Set number within exercise (1-based) |
| `reps` | Int | Yes | Reps completed (null = not yet logged) |
| `weightLbs` | Float | Yes | Weight in pounds (null or 0 for bodyweight) |
| `restSeconds` | Int | Yes | Rest timer duration for this set (null = no timer) |
| `completedAt` | Long | Yes | Unix timestamp when set was logged (null = not done) |

**Constraints**:
- `setNumber` must be >= 1
- `reps` must be >= 0 if specified
- `weightLbs` must be >= 0 if specified
- `restSeconds` must be > 0 if specified
- CASCADE DELETE when parent exercise log is deleted

---

## Static Data

### Exercise Library

Not stored in database—hardcoded in `ExerciseLibrary.kt`.

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Exercise name (e.g., "Bench Press") |
| `category` | String | Muscle group category |
| `isBodyweight` | Boolean | True if typically done without weight |

**Categories**:
- Chest
- Back
- Shoulders
- Biceps
- Triceps
- Legs
- Core
- Full Body
- Cardio

---

## Queries (DAO Methods)

### TemplateDao

```kotlin
// Get all templates ordered by name
fun getAllTemplates(): Flow<List<WorkoutTemplate>>

// Get template with exercises
fun getTemplateWithExercises(templateId: Long): Flow<TemplateWithExercises>

// Insert/update template
suspend fun upsertTemplate(template: WorkoutTemplate): Long

// Delete template (exercises cascade)
suspend fun deleteTemplate(templateId: Long)

// Update exercise order
suspend fun updateExerciseOrder(exercises: List<TemplateExercise>)
```

### SessionDao

```kotlin
// Get in-progress workout (for crash recovery)
fun getActiveSession(): Flow<WorkoutSession?>

// Get all completed sessions (history, reverse chronological)
fun getCompletedSessions(): Flow<List<WorkoutSession>>

// Get session with full exercise/set data
fun getSessionWithDetails(sessionId: Long): Flow<SessionWithDetails>

// Insert new session
suspend fun insertSession(session: WorkoutSession): Long

// Update session (for completion, auto-save)
suspend fun updateSession(session: WorkoutSession)

// Bulk upsert exercise logs and set logs
suspend fun saveWorkoutProgress(exercises: List<ExerciseLog>, sets: List<SetLog>)
```

---

## Migration Strategy

**Version 1**: Initial schema (all tables above)

Future migrations will use Room's `@Migration` annotations if schema changes are needed. For a personal app, destructive migration (wipe and recreate) is acceptable during development.
