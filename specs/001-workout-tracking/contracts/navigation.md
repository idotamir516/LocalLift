# Screen Navigation Contracts

**Feature**: 001-workout-tracking  
**Date**: 2025-12-03

## Navigation Graph

```
                    ┌─────────────────────────────────────────────┐
                    │              MainActivity                    │
                    │  ┌─────────────────────────────────────────┐ │
                    │  │         Bottom Navigation Bar           │ │
                    │  │  [Start Workout] [Templates] [History]  │ │
                    │  └─────────────────────────────────────────┘ │
                    └─────────────────────────────────────────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              ▼                          ▼                          ▼
    ┌─────────────────┐      ┌─────────────────┐        ┌─────────────────┐
    │ StartWorkout    │      │   Templates     │        │    History      │
    │    Screen       │      │    Screen       │        │    Screen       │
    │ (template list) │      │ (template list) │        │ (session list)  │
    └────────┬────────┘      └────────┬────────┘        └────────┬────────┘
             │                        │                          │
             │ select template        │ tap template             │ tap session
             ▼                        ▼                          ▼
    ┌─────────────────┐      ┌─────────────────┐        ┌─────────────────┐
    │ ActiveWorkout   │      │ TemplateEditor  │        │ WorkoutDetail   │
    │    Screen       │      │    Screen       │        │    Screen       │
    │ (logging sets)  │      │ (edit template) │        │ (view past)     │
    └─────────────────┘      └─────────────────┘        └─────────────────┘
```

---

## Screen Contracts

### 1. StartWorkoutScreen

**Route**: `start_workout`

**Purpose**: Display list of templates to start a workout from.

**Entry Points**:
- Bottom navigation tab (default/home)
- App launch with no active workout

**Inputs**: None

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Tap template | → `active_workout/{templateId}` | Template ID |
| Resume workout | → `active_workout/{sessionId}?resume=true` | Session ID |

**UI Elements**:
- List of workout templates (name, exercise count)
- "Create Template" button (→ Templates tab)
- Resume banner if in-progress workout exists

**Empty State**: "No templates yet. Create one in the Templates tab."

---

### 2. ActiveWorkoutScreen

**Route**: `active_workout/{templateId}` or `active_workout/{sessionId}?resume=true`

**Purpose**: Execute a workout—log sets, manage timer, finish workout.

**Entry Points**:
- StartWorkoutScreen (new workout from template)
- App launch with active session (crash recovery)

**Inputs**:
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `templateId` | Long | No | Template to start workout from |
| `sessionId` | Long | No | Existing session to resume |
| `resume` | Boolean | No | True if resuming crashed session |

*One of templateId or sessionId is required.*

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Finish workout | → `history` (back to tab) | None (data saved) |
| Cancel workout | → `start_workout` (confirm dialog) | None |

**UI Elements**:
- Workout name header with elapsed time
- Scrollable list of exercises (ExerciseCard components)
  - Each exercise: name, set rows
  - Each set row: reps input, weight input (inline)
  - Drag handle for reordering
- Add Exercise FAB
- Rest timer overlay (when active)
- Finish Workout button

**Behaviors**:
- Auto-save on every set completion
- Rest timer auto-starts after set logged (if configured)
- Timer shows countdown, plays audio on completion

---

### 3. TemplatesScreen

**Route**: `templates`

**Purpose**: View, create, edit, delete workout templates.

**Entry Points**:
- Bottom navigation tab

**Inputs**: None

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Tap template | → `template_editor/{templateId}` | Template ID |
| Create new | → `template_editor/new` | None |

**UI Elements**:
- List of templates (name, exercise count, last modified)
- FAB: "New Template"
- Swipe-to-delete with confirmation
- Empty state if no templates

---

### 4. TemplateEditorScreen

**Route**: `template_editor/{templateId}` or `template_editor/new`

**Purpose**: Create or edit a workout template.

**Entry Points**:
- TemplatesScreen (tap existing or create new)

**Inputs**:
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `templateId` | Long | No | Template to edit (null = new) |

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Save | ← Back to `templates` | None (data saved) |
| Cancel | ← Back to `templates` | None |

**UI Elements**:
- Template name text field
- List of exercises (reorderable)
  - Exercise name (from picker or typed)
  - Target sets stepper (+/-)
  - Rest timer input (optional, seconds)
  - Delete button
- Add Exercise button (opens ExercisePicker)
- Save button
- Delete Template button (edit mode only, with confirmation)

---

### 5. HistoryScreen

**Route**: `history`

**Purpose**: View past completed workouts.

**Entry Points**:
- Bottom navigation tab

**Inputs**: None

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Tap session | → `workout_detail/{sessionId}` | Session ID |

**UI Elements**:
- List of completed workouts grouped by date
  - Each item: workout name, date, duration, exercise count
- Empty state if no history

---

### 6. WorkoutDetailScreen

**Route**: `workout_detail/{sessionId}`

**Purpose**: View details of a past workout (read-only).

**Entry Points**:
- HistoryScreen (tap completed workout)

**Inputs**:
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | Long | Yes | Session to view |

**Outputs**:
| Action | Navigation | Data Passed |
|--------|------------|-------------|
| Back | ← Back to `history` | None |

**UI Elements**:
- Workout name, date, duration header
- List of exercises performed
  - Each exercise: name, sets performed
  - Each set: reps × weight lbs
- No editing capability (history is read-only)

---

## Shared Components

### ExercisePicker (Bottom Sheet/Dialog)

**Purpose**: Select exercise from static library or enter custom name.

**Inputs**: Current exercise name (for edit) or null

**Outputs**: Selected exercise name (String)

**UI Elements**:
- Search/filter field
- Category tabs or filter chips
- Scrollable exercise list
- "Custom Exercise" option at bottom
- Cancel / Select buttons

---

### RestTimer (Overlay)

**Purpose**: Countdown timer between sets.

**Inputs**: Duration in seconds

**Outputs**: Timer completion event

**UI Elements**:
- Large countdown display (MM:SS or just SS)
- Skip button
- Visual progress indicator (optional)

**Behaviors**:
- Plays audio cue on completion
- Auto-dismisses after completion (brief delay)
- Tapping outside does not dismiss (modal)
